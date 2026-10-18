"""Boot a real dedicated server with a built xCraft jar and check the multipliers over RCON.

    python3 smoke_prod.py --loader fabric|neoforge|forge|legacy-fabric --mc 1.21.7 --jar build/libs/.../xcraft-...jar

Installs the official server for that loader and Minecraft version (Fabric server launcher + Fabric API,
or the NeoForge/Forge installer), sets furnace x8 and mobs x3, then checks that a furnace smelts far faster
than vanilla and that a killed cow drops beef in multiples of 3. Exits non-zero on failure.
Legacy Fabric (1.7-1.13) cannot read item NBT over commands on every version, so there the mod runs with
-Dxcraft.debug=true and the test checks its debug log for the furnace and mob loot multiplications.
Java is taken from JAVA_HOME_<N>_X64 (set by actions/setup-java) or JAVA_<N>_HOME, falling back to `java`.
"""
import argparse
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
from pathlib import Path

UA = {"User-Agent": "xcraft-ci-smoke (github.com/polvallverdu/xcraft)"}
RCON_PORT, RCON_PASSWORD = 25575, "xcraft"


def key(v):
    return [int(x) for x in v.split(".")]


def get(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=60) as r:
        return r.read()


def download(url, dest):
    dest.write_bytes(get(url))
    return dest


def java_for(mc):
    v = key(mc)
    major = 8 if v < [1, 17] else 17 if v < [1, 20, 5] else 21 if v[0] == 1 else 25
    for var in (f"JAVA_HOME_{major}_X64", f"JAVA_HOME_{major}_ARM64", f"JAVA_{major}_HOME"):
        if os.environ.get(var):
            return str(Path(os.environ[var]) / "bin" / "java")
    return "java"


def install_fabric(mc, root, java):
    loader = next(l["version"] for l in json.loads(get("https://meta.fabricmc.net/v2/versions/loader")) if l["stable"])
    installer = next(i["version"] for i in json.loads(get("https://meta.fabricmc.net/v2/versions/installer")) if i["stable"])
    download(f"https://meta.fabricmc.net/v2/versions/loader/{mc}/{loader}/{installer}/server/jar", root / "server.jar")
    versions = json.loads(get(
        "https://api.modrinth.com/v2/project/fabric-api/version?game_versions=" + urllib.request.quote(json.dumps([mc]))
        + "&loaders=" + urllib.request.quote('["fabric"]')))
    api = versions[0]["files"][0]
    download(api["url"], root / "mods" / api["filename"])
    return [java, "-Xmx2G", "-jar", "server.jar", "nogui"]


def install_legacy_fabric(mc, root, java):
    download(f"https://meta.legacyfabric.net/v2/versions/loader/{mc}/0.20.0/1.1.1/server/jar", root / "fabric-server-launch.jar")
    manifest = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
    version = json.loads(get(next(v["url"] for v in manifest["versions"] if v["id"] == mc)))
    download(version["downloads"]["server"]["url"], root / "server.jar")
    return [java, "-Xmx2G", "-Dxcraft.debug=true", "-jar", "fabric-server-launch.jar", "nogui"]


def legacy_checks(r, mc, root):
    """Old versions: place a filled furnace and drop a cow to its death, then read xCraft's debug log."""
    items = '[{Slot:0b,id:"minecraft:iron_ore",Count:16b},{Slot:1b,id:"minecraft:coal",Count:4b}]'
    if key(mc) >= [1, 13]:
        r.cmd(f"setblock 0 5 0 minecraft:furnace{{Items:{items}}}")
    else:
        r.cmd(f"setblock 0 5 0 minecraft:furnace 0 replace {{Items:{items}}}")
    cow = "cow" if key(mc) >= [1, 11] else "Cow"
    r.cmd(f"summon {cow} 3 45 3")  # dies from the fall; there are no @e selectors to /kill it before 1.8
    time.sleep(15)
    log = (root / "server.log").read_text(errors="replace")
    furnace = len(re.findall(r"\[xcraft-debug\] furnace x8", log))
    loot = re.findall(r"\[xcraft-debug\] mob loot x3: (\d+) -> (\d+)", log)
    if furnace < 3:
        raise RuntimeError(f"furnace not sped up ({furnace} boosted ticks)")
    if not loot:
        raise RuntimeError("mob drops not multiplied (no loot debug line)")
    return f"furnace boosted {furnace} ticks, loot={loot}"


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
        version = f"{mc}-{fv}"
        url = f"https://maven.minecraftforge.net/net/minecraftforge/forge/{version}/forge-{version}-installer.jar"
        args_glob = f"libraries/net/minecraftforge/forge/{version}/unix_args.txt"
    download(url, root / "installer.jar")
    subprocess.run([java, "-jar", "installer.jar", "--installServer"], cwd=root, check=True,
                   stdout=subprocess.DEVNULL, stderr=subprocess.STDOUT, timeout=900)
    print(f"  installed {loader} {version}")
    return [java, "-Xmx2G", "@user_jvm_args.txt", "@" + args_glob, "nogui"]


class Rcon:
    def __init__(self):
        self.s = socket.create_connection(("127.0.0.1", RCON_PORT), timeout=30)
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
        out = self._send(2, c)
        print(f"  > {c}\n    {out.strip()[:300]}")
        return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--loader", required=True, choices=["fabric", "neoforge", "forge", "legacy-fabric"])
    ap.add_argument("--mc", required=True)
    ap.add_argument("--jar", required=True)
    ap.add_argument("--workdir", default="smoke-servers")
    a = ap.parse_args()

    root = Path(a.workdir) / f"{a.loader}-{a.mc}"
    shutil.rmtree(root, ignore_errors=True)
    (root / "mods").mkdir(parents=True)
    java = java_for(a.mc)
    print(f"== {a.loader} {a.mc} ({java})")
    if a.loader == "fabric":
        cmd = install_fabric(a.mc, root, java)
    elif a.loader == "legacy-fabric":
        cmd = install_legacy_fabric(a.mc, root, java)
    else:
        cmd = install_forge_like(a.mc, root, java, a.loader)
    shutil.copy(a.jar, root / "mods")
    (root / "eula.txt").write_text("eula=true\n")
    (root / "server.properties").write_text(
        f"online-mode=false\nenable-rcon=true\nrcon.port={RCON_PORT}\nrcon.password={RCON_PASSWORD}\n"
        "spawn-protection=0\nbroadcast-rcon-to-ops=false\nmax-tick-time=-1\n"
        + ("level-type=FLAT\n" if a.loader == "legacy-fabric" else ""))

    log = open(root / "server.log", "w")
    proc = subprocess.Popen(cmd, cwd=root, stdout=log, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL)
    ok, reason = False, "server did not start"
    try:
        deadline = time.time() + 900
        while time.time() < deadline and proc.poll() is None:
            text = (root / "server.log").read_text(errors="replace")
            if "RCON running" in text:
                break
            time.sleep(2)
        text = (root / "server.log").read_text(errors="replace")
        if "RCON running" not in text:
            raise RuntimeError(reason)
        r = Rcon()
        # The /xcraft reply proves the mod loaded (some Forge versions drop the mod's SLF4J log lines)
        if "Set furnace multiplier" not in r.cmd("xcraft furnace 8"):
            raise RuntimeError("xCraft did not load (no /xcraft command)")
        r.cmd("xcraft mobs 3")
        if a.loader == "legacy-fabric":
            reason = legacy_checks(r, a.mc, root)
            r.cmd("xcraft reload")
            r.cmd("stop")
            ok = True
            raise StopIteration
        r.cmd("forceload add ~ ~")  # 1.21.9+ no longer keeps spawn chunks loaded
        time.sleep(2)
        r.cmd("setblock ~ ~3 ~ furnace")
        fill = "item replace block ~ ~3 ~ {} with {} {}" if key(a.mc) >= [1, 17] else "replaceitem block ~ ~3 ~ {} {} {}"
        r.cmd(fill.format("container.0", "iron_ore", 16))
        r.cmd(fill.format("container.1", "coal", 4))
        # Only our tagged cow and the items next to it: worlds can hold other drops (e.g. mineshaft loot)
        r.cmd('summon cow ~ ~3 ~4 {NoAI:1b,Tags:["xcraft"]}')
        time.sleep(3)
        r.cmd("kill @e[tag=xcraft]")
        time.sleep(7)
        furnace = r.cmd("data get block ~ ~3 ~ Items")
        items = r.cmd("execute positioned ~ ~3 ~4 as @e[type=item,distance=..3] run data get entity @s Item")
        r.cmd("xcraft reload")
        r.cmd("stop")

        ingots = re.search(r'iron_ingot"?, [Cc]ount: (\d+)|[Cc]ount: (\d+)[^}]*iron_ingot', furnace)
        ingots = int(next(g for g in ingots.groups() if g)) if ingots else 0
        beef = [int(next(g for g in m if g)) for m in re.findall(r'beef"?, [Cc]ount: (\d+)|[Cc]ount: (\d+)[^}]*beef', items)]
        # Vanilla smelts 1 item per 10 s; at x8 roughly 7 in 7 s
        if ingots < 3:
            raise RuntimeError(f"furnace not sped up (iron_ingot={ingots})")
        if not beef or any(b % 3 for b in beef):
            raise RuntimeError(f"mob drops not multiplied (beef={beef})")
        ok, reason = True, f"furnace={ingots} beef={beef}"
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

    text = (root / "server.log").read_text(errors="replace")
    for line in text.splitlines():
        if re.search(r"Mixin apply|InvalidInjection|Exception in|FATAL|Incompatible mod", line):
            print("  !", line[:240])
    print(f"{'PASS' if ok else 'FAIL'} {a.loader} {a.mc}: {reason}")
    if not ok:
        print("\n".join(text.splitlines()[-40:]))
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
