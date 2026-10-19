"""Boot a real dedicated server with a built xCraft jar and check the multipliers over RCON.

    python3 smoke_prod.py --loader fabric|neoforge|forge|legacy-fabric|ornithe --mc 1.21.7 --jar build/libs/.../xcraft-...jar

Installs the official server for that loader and Minecraft version (Fabric server launcher,
or the NeoForge/Forge installer), sets furnace x8 and mobs x3, then checks that a furnace smelts far faster
than vanilla and that a killed cow drops beef in multiples of 3. Exits non-zero on failure.
Legacy Fabric, Ornithe and Forge before 1.13 cannot read item NBT over commands on every version, so there the mod runs with
-Dxcraft.debug=true and the test checks its debug log for the furnace and mob loot multiplications.
Java is taken from JAVA_HOME_<N>_X64 (set by actions/setup-java) or JAVA_<N>_HOME, falling back to `java`.
"""
import argparse
import gzip
import io
import json
import os
import re
import shutil
import socket
import struct
import subprocess
import sys
import time
import urllib.request
import zipfile
from pathlib import Path

UA = {"User-Agent": "xcraft-ci-smoke (github.com/polvallverdu/xcraft)"}
# XCRAFT_SMOKE_PORT_OFFSET shifts the game and RCON ports so several test servers can run side by side
PORT_OFFSET = int(os.environ.get("XCRAFT_SMOKE_PORT_OFFSET", "0"))
RCON_PORT, RCON_PASSWORD = 25575 + PORT_OFFSET, "xcraft"
# Vanilla 1.14-1.14.2 give command-placed furnaces no block entity, so the furnace can't be set up from RCON
# there. The same jar's furnace hook is verified on 1.14.3 and 1.14.4.
FURNACE_UNTESTABLE = {"1.14", "1.14.1", "1.14.2"}


def key(v):
    return [int(x) for x in v.split(".")]


def get(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=60) as r:
        return r.read()


def download(url, dest):
    dest.write_bytes(get(url))
    return dest


def java_for(mc, loader=""):
    v = key(mc)
    major = 8 if v < [1, 17] else 17 if v < [1, 20, 5] else 21 if v[0] == 1 else 25
    # Forge 1.7.2 ships LaunchWrapper 1.9, which crashes on Java 8 (ConcurrentModificationException at launch)
    if loader == "forge" and [1, 7] <= v < [1, 7, 10]:
        major = 7
    # Forge 1.16.4's ModLauncher calls a JDK-internal constructor removed in 8u321 (NoSuchMethodError at launch)
    if loader == "forge" and mc == "1.16.4" and os.environ.get("JAVA_8_OLD_HOME"):
        return str(Path(os.environ["JAVA_8_OLD_HOME"]) / "bin" / "java")
    for var in (f"JAVA_HOME_{major}_X64", f"JAVA_HOME_{major}_ARM64", f"JAVA_{major}_HOME"):
        if os.environ.get(var):
            return str(Path(os.environ[var]) / "bin" / "java")
    return "java"


def install_fabric(mc, root, java):
    loader = next(l["version"] for l in json.loads(get("https://meta.fabricmc.net/v2/versions/loader")) if l["stable"])
    installer = next(i["version"] for i in json.loads(get("https://meta.fabricmc.net/v2/versions/installer")) if i["stable"])
    download(f"https://meta.fabricmc.net/v2/versions/loader/{mc}/{loader}/{installer}/server/jar", root / "server.jar")
    # No Fabric API: xCraft has no dependencies on Fabric
    return [java, "-Xmx2G", "-jar", "server.jar", "nogui"]


def install_legacy_fabric(mc, root, java):
    download(f"https://meta.legacyfabric.net/v2/versions/loader/{mc}/0.20.0/1.1.1/server/jar", root / "fabric-server-launch.jar")
    manifest = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
    version = json.loads(get(next(v["url"] for v in manifest["versions"] if v["id"] == mc)))
    download(version["downloads"]["server"]["url"], root / "server.jar")
    return [java, "-Xmx2G", "-Dxcraft.debug=true", "-jar", "fabric-server-launch.jar", "nogui"]


def world_spawn(root):
    """SpawnX/SpawnZ from level.dat (old consoles run commands at 0,0,0, which may not be loaded)."""
    data = gzip.decompress((root / "world" / "level.dat").read_bytes())
    def int_tag(name):
        i = data.index(b"\x03" + struct.pack(">H", len(name)) + name.encode()) + 3 + len(name)
        return struct.unpack(">i", data[i:i + 4])[0]
    return int_tag("SpawnX"), int_tag("SpawnZ")


def install_ornithe(mc, root, java):
    """Ornithe has no server-launcher jar endpoint: build the classpath from its launch profile."""
    profile = json.loads(get(f"https://meta.ornithemc.net/v3/versions/gen2/fabric-loader/{mc}/0.20.0/server/json"))
    libs = root / "libraries"
    libs.mkdir()
    for lib in profile["libraries"]:
        group, artifact, version = lib["name"].split(":")[:3]
        path = f"{group.replace('.', '/')}/{artifact}/{version}/{artifact}-{version}.jar"
        base = lib.get("url", "https://libraries.minecraft.net/").rstrip("/") + "/"
        dest = libs / f"{artifact}-{version}.jar"
        try:
            download(base + path, dest)
        except Exception:  # noqa: BLE001 - maven.fabricmc.net outages: use Legacy Fabric's mirror of it
            download(base.replace("https://maven.fabricmc.net/", "https://repo.legacyfabric.net/fabricmc/") + path, dest)
    manifest = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
    version = json.loads(get(next(v["url"] for v in manifest["versions"] if v["id"] == mc)))
    download(version["downloads"]["server"]["url"], root / "server.jar")
    main = profile["mainClass"]["server"] if isinstance(profile["mainClass"], dict) else profile["mainClass"]
    return [java, "-Xmx2G", "-Dxcraft.debug=true", "-Dfabric.gameJarPath=server.jar", f"-Dfabric.gameVersion={mc}",
            "-Dfabric.fixPackageAccess=true", "-cp", os.pathsep.join(str(p.relative_to(root)) for p in sorted(libs.glob("*.jar"))),
            main, "nogui"]


def legacy_checks(r, mc, root):
    """Old versions: place a filled furnace and drop a cow to its death, then read xCraft's debug log."""
    # Only when needed: before 1.12 RCON runs commands off the server thread, and a save-all racing the chunk
    # unloader can crash the server (ConcurrentModificationException in ChunkProviderServer, seen on 1.10.2/1.11.2)
    if not (root / "world" / "level.dat").exists():
        r.cmd("save-all")
        time.sleep(2)
    x, z = world_spawn(root)
    # CookTimeTotal: only set when an item is inserted normally, so an NBT-placed furnace would never finish
    # Item ids in NBT are numeric before 1.8 (15 = iron ore, 263 = coal)
    ore, coal = ('"minecraft:iron_ore"', '"minecraft:coal"') if key(mc) >= [1, 8] else ("15s", "263s")
    nbt = f'Items:[{{Slot:0b,id:{ore},Count:16b}},{{Slot:1b,id:{coal},Count:4b}}],CookTimeTotal:200s'
    if key(mc) >= [1, 13]:
        r.cmd(f"setblock {x} 5 {z} minecraft:furnace{{{nbt}}}")
    else:
        r.cmd(f"setblock {x} 5 {z} minecraft:furnace 0 replace {{{nbt}}}")
    cow = "cow" if key(mc) >= [1, 11] else "Cow"
    r.cmd(f"summon {cow} {x + 3} 45 {z + 3}")  # dies from the fall; there are no @e selectors before 1.8
    time.sleep(15)
    log = (root / "server.log").read_text(errors="replace")
    furnace = len(re.findall(r"\[xcraft-debug\] furnace x8", log))
    loot = re.findall(r"\[xcraft-debug\] mob loot x3: (\d+) -> (\d+)", log)
    if furnace < 3:
        raise RuntimeError(f"furnace not sped up ({furnace} boosted ticks)")
    if not loot:
        raise RuntimeError("mob drops not multiplied (no loot debug line)")
    return f"furnace boosted {furnace} ticks, loot={loot}"


def selftest_checks(r, warmup=False):
    """Before 1.7 vanilla has no /setblock or /summon: the mod's dev-only /xcraft test builds the scenarios itself.

    With warmup (Forge up to 1.12.2), a first run places a furnace that the world then ticks for 12 s; the second
    run reports it. Forge 1.13+ reports furnace=-1: the server test checks its furnace in the world there.
    """
    r.cmd("xcraft ores 2")
    r.cmd("xcraft xp 5")
    if warmup:
        r.cmd("xcraft test")
        time.sleep(12)
    out = re.sub("[\u00a7\ufffd].", "", r.cmd("xcraft test"))  # colour codes (RCON may mangle the \u00a7)
    # furnace=-1: the server test checks furnaces in the world instead (Forge 1.7+)
    m = re.search(r"xcraft-test furnace=(-?\d+) beef=(\S+) leather=(\S+) ore=(\S+) xp=(-?\d+) \(orb (\d+)\)", out)
    if not m:
        raise RuntimeError(f"no xcraft-test result: {out.strip()[:200]}")
    furnace, beef, leather, ore, xp, orb = m.groups()
    counts = lambda s: [] if s == "none" else [int(c) for c in s.split(",")]
    # 200 furnace ticks smelt 1 item in vanilla; at x8 about 8
    if 0 <= int(furnace) < 3:
        raise RuntimeError(f"furnace not sped up (smelted {furnace})")
    if not counts(beef) or any(c % 3 for c in counts(beef) + counts(leather)):
        raise RuntimeError(f"mob drops not multiplied (beef={beef} leather={leather})")
    if not counts(ore) or any(c % 2 for c in counts(ore)):
        raise RuntimeError(f"ore drops not multiplied (coal={ore})")
    if int(xp) != int(orb) * 5:
        raise RuntimeError(f"xp not multiplied ({xp} from a {orb} orb)")
    return f"furnace={furnace} beef={beef} leather={leather} ore={ore} xp={xp}"


def install_forge_merged(mc, root, java):
    """Forge 1.3.2-1.5.2 has no working server installer: patch the vanilla server jar with the universal zip.

    FML downloads its libraries at launch from files.minecraftforge.net/fmllibs, which is gone: pre-fill lib/ from
    Prism Launcher's mirror of it (same files, FML checks their hashes).
    """
    promos = json.loads(get("https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json"))["promos"]
    version = f"{mc}-{promos.get(f'{mc}-recommended') or promos[f'{mc}-latest']}"
    universal = zipfile.ZipFile(io.BytesIO(get(
        f"https://maven.minecraftforge.net/net/minecraftforge/forge/{version}/forge-{version}-universal.zip")))
    manifest = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
    meta = json.loads(get(next(v["url"] for v in manifest["versions"] if v["id"] == mc)))
    vanilla = zipfile.ZipFile(io.BytesIO(get(meta["downloads"]["server"]["url"])))
    with zipfile.ZipFile(root / "minecraft_server.jar", "w", zipfile.ZIP_DEFLATED) as out:
        forge_names = set(universal.namelist())
        for source, names in ((vanilla, [n for n in vanilla.namelist() if n not in forge_names]), (universal, universal.namelist())):
            for name in names:
                if name.endswith("/") or re.match(r"META-INF/(MOJANG|.*\.(SF|RSA|DSA)$)", name):
                    continue
                out.writestr(name, source.read(name))
    libs = re.findall(rb"[\w.-]+\.jar", universal.read("cpw/mods/fml/relauncher/CoreFMLLibraries.class"))
    names = [l.decode() for l in libs]
    if b"deobfuscation" in universal.read("fmlversion.properties"):
        names.append(f"deobfuscation_data_{mc}.zip")
    (root / "lib").mkdir()
    for name in names:
        download(f"https://files.prismlauncher.org/fmllibs/{name}", root / "lib" / name)
    print(f"  installed forge {version} (merged into the vanilla server, libs: {', '.join(names)})")
    return [java, "-Xmx2G", "-Dxcraft.debug=true", "-jar", "minecraft_server.jar", "nogui"]


def install_forge_like(mc, root, java, loader):
    if loader == "neoforge":
        p = mc.split(".")
        line = f"{p[1]}.{p[2] if len(p) > 2 else 0}." if p[0] == "1" else mc + ("." if mc.count(".") == 2 else ".0.")
        meta = get("https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml").decode()
        candidates = [v for v in re.findall(r"<version>([^<]+)</version>", meta) if v.startswith(line)]
        version = max(candidates, key=lambda v: [int(x) if x.isdigit() else -1 for x in re.split(r"[.-]", v)])
        url = f"https://maven.neoforged.net/releases/net/neoforged/neoforge/{version}/neoforge-{version}-installer.jar"
        args_glob = f"libraries/net/neoforged/neoforge/{version}/unix_args.txt"
    else:
        promos = json.loads(get("https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json"))["promos"]
        fv = promos.get(f"{mc}-recommended") or promos[f"{mc}-latest"]
        # Old builds carry a branch suffix in their Maven version (1.7.10-10.13.4.1614-1.7.10, -mc172, ...)
        meta = get("https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml").decode()
        versions = re.findall(r"<version>([^<]+)</version>", meta)
        version = next((v for v in versions if v == f"{mc}-{fv}"), None) or \
            next(v for v in versions if v.startswith(f"{mc}-{fv}-"))
        url = f"https://maven.minecraftforge.net/net/minecraftforge/forge/{version}/forge-{version}-installer.jar"
        args_glob = f"libraries/net/minecraftforge/forge/{version}/unix_args.txt"
    download(url, root / "installer.jar")
    for attempt in range(3):
        # The installer always runs on the version's usual Java (Java 7 can't reach today's TLS endpoints)
        subprocess.run([java_for(mc), "-Djava.awt.headless=true", "-jar", "installer.jar", "--installServer"], cwd=root,
                       stdout=subprocess.DEVNULL, stderr=subprocess.STDOUT, timeout=900)
        # Installers can report success after a failed library download: check for something to launch
        if (root / args_glob).exists():
            print(f"  installed {loader} {version}")
            return [java, "-Xmx2G", "@user_jvm_args.txt", "@" + args_glob, "nogui"]
        # Forge 1.6 installs "minecraftforge-universal-*.jar", 1.7-1.12.1 a "-universal" jar, 1.12.2-1.16.5 a plain
        # one, 1.20.3 only a shim
        jars = sorted((j for j in [*root.glob("forge-*.jar"), *root.glob("minecraftforge-*.jar")] if "installer" not in j.name),
                      key=lambda j: len(j.name))
        shim = [j for j in jars if j.name.endswith("-shim.jar")]
        if shim or jars:
            print(f"  installed {loader} {version}")
            debug = ["-Dxcraft.debug=true"] if key(mc) < [1, 17] else []
            return [java, "-Xmx2G", *debug, "-jar", (shim or jars)[0].name, "nogui"]
    raise RuntimeError(f"{loader} {version} installer produced nothing to launch")


class Rcon:
    def __init__(self):
        # The server logs "RCON running" just before it accepts connections
        for attempt in range(30):
            try:
                self.s = socket.create_connection(("127.0.0.1", RCON_PORT), timeout=30)
                break
            except ConnectionRefusedError:
                if attempt == 29:
                    raise
                time.sleep(1)
        self._send(3, RCON_PASSWORD)

    def _send(self, kind, body):
        data = body.encode() + b"\x00\x00"
        self.s.sendall(struct.pack("<iii", len(data) + 8, 1, kind) + data)
        n = struct.unpack("<i", self._read(4))[0]
        return self._read(n)[8:-2].decode(errors="replace")

    def _read(self, n):
        buf = b""
        while len(buf) < n:
            chunk = self.s.recv(n - len(buf))
            if not chunk:
                raise ConnectionError("RCON closed")
            buf += chunk
        return buf

    def cmd(self, c):
        try:
            out = self._send(2, c)
        except (ConnectionError, OSError):
            # Minecraft 1.11's RCON sometimes drops the connection after a command: reconnect once
            self.s.close()
            self.__init__()
            out = self._send(2, c)
        print(f"  > {c}\n    {out.strip()[:300]}")
        return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--loader", required=True, choices=["fabric", "neoforge", "forge", "legacy-fabric", "ornithe"])
    ap.add_argument("--mc", required=True)
    ap.add_argument("--jar", required=True)
    ap.add_argument("--workdir", default="smoke-servers")
    a = ap.parse_args()

    root = Path(a.workdir) / f"{a.loader}-{a.mc}"
    shutil.rmtree(root, ignore_errors=True)
    (root / "mods").mkdir(parents=True)
    java = java_for(a.mc, a.loader)
    print(f"== {a.loader} {a.mc} ({java})")
    if a.loader == "fabric":
        cmd = install_fabric(a.mc, root, java)
    elif a.loader == "legacy-fabric":
        cmd = install_legacy_fabric(a.mc, root, java)
    elif a.loader == "ornithe":
        cmd = install_ornithe(a.mc, root, java)
    elif a.loader == "forge" and key(a.mc) < [1, 6]:
        cmd = install_forge_merged(a.mc, root, java)
    else:
        cmd = install_forge_like(a.mc, root, java, a.loader)
    # Forge before 1.6 loads coremods (xCraft's ore hook) only from the coremods folder; FML also finds the mod there
    target = root / ("coremods" if a.loader == "forge" and key(a.mc) < [1, 6] else "mods")
    target.mkdir(exist_ok=True)
    shutil.copy(a.jar, target)
    (root / "eula.txt").write_text("eula=true\n")
    (root / "server.properties").write_text(
        f"online-mode=false\nenable-rcon=true\nrcon.port={RCON_PORT}\nrcon.password={RCON_PASSWORD}\n"
        f"server-port={25565 + PORT_OFFSET}\n"
        "spawn-protection=0\nbroadcast-rcon-to-ops=false\nmax-tick-time=-1\n"
        # Flat world: random terrain near spawn (water, caves) can carry the test drops away
        + ("level-type=minecraft:flat\n" if key(a.mc) >= [1, 19] else "level-type=FLAT\n"))

    # Before 1.7 the vanilla server writes its own server.log in the working directory
    console = root / ("console.log" if key(a.mc) < [1, 7] else "server.log")
    log = open(console, "w")
    proc = subprocess.Popen(cmd, cwd=root, stdout=log, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL)
    ok, reason = False, "server did not start"
    try:
        deadline = time.time() + 900
        while time.time() < deadline and proc.poll() is None:
            text = console.read_text(errors="replace")
            if "RCON running" in text:
                break
            time.sleep(2)
        text = console.read_text(errors="replace")
        if "RCON running" not in text:
            raise RuntimeError(reason)
        r = Rcon()
        # The /xcraft reply proves the mod loaded (some Forge versions drop the mod's SLF4J log lines)
        if "Set furnace multiplier" not in r.cmd("xcraft furnace 8"):
            raise RuntimeError("xCraft did not load (no /xcraft command)")
        r.cmd("xcraft mobs 3")
        # Forge before 1.13 has the same old commands and no item NBT readout: use the debug log as well
        if a.loader in ("legacy-fabric", "ornithe") or (a.loader == "forge" and key(a.mc) < [1, 13]):
            # Forge: its /xcraft test covers all four multipliers through the game's own code, on the server
            # thread. Changing the world over RCON (/setblock, /summon, save-all) runs on RCON's thread before 1.12
            # and can crash the server (ConcurrentModificationException), seen on Forge 1.8 and 1.12.2.
            if a.loader == "forge":
                reason = selftest_checks(r, warmup=True)
            elif key(a.mc) < [1, 7]:
                reason = selftest_checks(r)
            else:
                reason = legacy_checks(r, a.mc, root)
            r.cmd("xcraft reload")
            r.cmd("stop")
            ok = True
            raise StopIteration
        r.cmd("forceload add ~ ~")  # 1.21.9+ no longer keeps spawn chunks loaded
        time.sleep(2)
        if key(a.mc) >= [1, 17]:
            r.cmd("setblock ~ ~3 ~ furnace")
            r.cmd("item replace block ~ ~3 ~ container.0 with iron_ore 16")
            r.cmd("item replace block ~ ~3 ~ container.1 with coal 4")
        else:
            # A plain /setblock furnace gets no block entity on 1.14-1.14.2; placing it with NBT does
            r.cmd('setblock ~ ~3 ~ furnace{Items:[{Slot:0b,id:"minecraft:iron_ore",Count:16b},'
                  '{Slot:1b,id:"minecraft:coal",Count:4b}],CookTimeTotal:200s}')
        # Only our tagged cow and the items next to it: worlds can hold other drops (e.g. mineshaft loot)
        r.cmd('summon cow ~ ~1 ~4 {NoAI:1b,Tags:["xcraft"]}')
        time.sleep(3)
        r.cmd("kill @e[tag=xcraft]")
        time.sleep(7)
        furnace = r.cmd("data get block ~ ~3 ~ Items")
        # NoAI cows don't fall; it stands right above the ground, so its drops land next to it
        items = r.cmd("execute positioned ~ ~1 ~4 as @e[type=item,distance=..8] run data get entity @s Item")
        # forge-legacy (up to 1.16.5) also has /xcraft test, for ores (real harvest) and XP
        selftest = selftest_checks(r) if a.loader == "forge" and key(a.mc) < [1, 17] else None
        r.cmd("xcraft reload")
        r.cmd("stop")

        ingots = re.search(r'iron_ingot"?, [Cc]ount: (\d+)|[Cc]ount: (\d+)[^}]*iron_ingot', furnace)
        ingots = int(next(g for g in ingots.groups() if g)) if ingots else 0
        beef = [int(next(g for g in m if g)) for m in re.findall(r'beef"?, [Cc]ount: (\d+)|[Cc]ount: (\d+)[^}]*beef', items)]
        # Vanilla smelts 1 item per 10 s; at x8 roughly 7 in 7 s
        if ingots < 3 and a.mc not in FURNACE_UNTESTABLE:
            raise RuntimeError(f"furnace not sped up (iron_ingot={ingots})")
        if not beef or any(b % 3 for b in beef):
            raise RuntimeError(f"mob drops not multiplied (beef={beef})")
        ok, reason = True, f"furnace={ingots if a.mc not in FURNACE_UNTESTABLE else 'n/a (vanilla bug)'} beef={beef}"
        if selftest:
            reason += f"; self-test {selftest}"
    except StopIteration:
        pass
    except Exception as e:  # noqa: BLE001 - report any failure with the server log tail
        reason = str(e)
    finally:
        try:
            proc.wait(timeout=60)
        except subprocess.TimeoutExpired:
            proc.kill()
        log.close()

    text = console.read_text(errors="replace")
    for line in text.splitlines():
        if re.search(r"Mixin apply|InvalidInjection|Exception in|FATAL|Incompatible mod", line):
            print("  !", line[:240])
    print(f"{'PASS' if ok else 'FAIL'} {a.loader} {a.mc}: {reason}")
    if not ok:
        print("\n".join(text.splitlines()[-40:]))
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
