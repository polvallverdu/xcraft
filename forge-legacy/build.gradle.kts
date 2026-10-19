import lzma.sdk.lzma.Decoder
import lzma.streams.LzmaInputStream
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
	java
	id("xyz.wagyourtail.unimined")
	id("me.modmuss50.mod-publish-plugin")
}

val mc = stonecutter.current.version
val modVersion = property("mod.version") as String
val forge = property("forge.$mc") as String
// Unimined can't set up Forge 1.6.1 (its universal jar predates the version.json and binary patch format Unimined
// reads), so 1.6.1 compiles against 1.6.2 and the jar is renamed to 1.6.1's obfuscated names (renameTo161 below)
val devMc = if (mc == "1.6.1") "1.6.2" else mc
val devForge = property("forge.$devMc") as String
// Forge before 1.7 (FML relauncher, @ForgeSubscribe, no Log4j or Gson) has its own sources and Forge's bundled MCP names
val ancient = stonecutter.eval(mc, "<1.7")
// FML (LaunchWrapper, mcmod.info, CommandBase) up to 1.12.2; ModLauncher (mods.toml, Brigadier) from 1.13
val fml = stonecutter.eval(mc, "<1.13")

version = "$modVersion-forge+$mc"
base.archivesName = "xcraft"

repositories {
	// macOS's java-objc-bridge natives are only on Mojang's library server (Maven Central has the jar, not the natives)
	exclusiveContent {
		forRepository { maven("https://libraries.minecraft.net/") { name = "Minecraft" } }
		filter { includeGroup("ca.weblite") }
	}
	// 1.7.2's LWJGL nightly: its POM points at a parent that was never published, so use the jars alone
	maven("https://libraries.minecraft.net/") {
		name = "MinecraftLwjglNightly"
		metadataSources { artifact() }
		content { includeVersionByRegex("org\\.lwjgl\\.lwjgl", ".*", ".*-nightly-2013.*") }
	}
	mavenCentral()
	// Old Minecraft libraries that never reached Maven Central
	maven("https://libraries.minecraft.net/") { name = "MinecraftLibraries" }
	maven("https://maven.minecraftforge.net/") {
		name = "Forge"
		content { excludeGroup("net.minecraft") }
	}
}

unimined.minecraft {
	version(devMc)
	if (ancient) {
		// MCP names from Forge's own src zip (no MCP releases on Maven for these); production stays obfuscated
		minecraftRemapper.config { ignoreConflicts(true) }
	} else {
		val (mcpChannel, mcpVersion) = (property("mcp.$mc") as String).split(':', limit = 2)
		mappings {
			searge()
			mcp(mcpChannel, mcpVersion)
		}
	}
	minecraftForge {
		loader(devForge)
		// Run Forge's setup tools on the build JVM instead of a JDK matching each release
		useToolchains = false
		// Unimined assumes the old dotted AT format below 1.7.10, but Forge 1.7.2's ATs already use spaces
		if (!ancient) legacyATFormat = false
	}
	// The CI server tests boot the official Forge server instead (.github/scripts/smoke_prod.py)
	runs { off = true }
	defaultRemapJar = true
}

java {
	toolchain.languageVersion = JavaLanguageVersion.of(8)
	// Forge 1.7.2 and older scan mod classes with ASM 4, which can't read Java 8 class files
	val bytecode = if (stonecutter.eval(mc, "<1.7.10")) JavaVersion.VERSION_1_7 else JavaVersion.VERSION_1_8
	sourceCompatibility = bytecode
	targetCompatibility = bytecode
	withSourcesJar()
}

// Before 1.7 the jar is also a coremod (ore drops before Forge 1.6.2 hook Block.harvestBlock, see ancient/core)
if (ancient) tasks.jar {
	manifest.attributes(
		"FMLCorePlugin" to "dev.polv.xcraft.forge.ancient.core.XcraftCorePlugin",
		"FMLCorePluginContainsFMLMod" to "true",
	)
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.compilerArgs.add("-Xlint:-options")
}

sourceSets.main {
	java.exclude(listOfNotNull(
		"dev/polv/xcraft/forge/ancient/**".takeIf { !ancient },
		"dev/polv/xcraft/forge/fml/**".takeIf { ancient || !fml },
		"dev/polv/xcraft/forge/ml/**".takeIf { fml },
	))
}

// Pack format of the mod's resources (1.13+ only)
val packFormat = when {
	stonecutter.eval(mc, ">=1.16.2") -> 6
	stonecutter.eval(mc, ">=1.15") -> 5
	else -> 4
}

tasks.processResources {
	val props = mapOf(
		"version" to modVersion,
		"minecraft" to mc,
		"forge_major" to forge.substringBefore('.'),
		"pack_format" to packFormat,
	)
	inputs.properties(props)
	filesMatching(listOf("mcmod.info", "META-INF/mods.toml", "pack.mcmeta")) { expand(props) }
	if (fml) exclude("META-INF/mods.toml", "pack.mcmeta") else exclude("mcmod.info")
}

/**
 * 1.6.1's jar: the 1.6.2-compiled jar with 1.6.2's obfuscated names replaced by 1.6.1's. Both versions' Forge
 * universal jars carry FML's runtime mappings (obfuscated name -> SRG/MCP name), which pair the names up.
 */
fun renameTo161(input: File, output: File, universals: Set<File>, minecraft: File) {
	fun mappings(version: String): List<List<String>> {
		val jar = universals.first { it.name.contains("-$version-") }
		val bytes = ZipFile(jar).use { zip ->
			zip.getInputStream(zip.getEntry("deobfuscation_data-$version.lzma")).use { LzmaInputStream(it, Decoder()).readBytes() }
		}
		return bytes.decodeToString().lines().map { it.split(' ') }
	}
	// "CL: obf mcp", "FD: obf/name mcpOwner/srg", "MD: obf/name obfDesc mcpOwner/srg mcpDesc"
	val classes161 = HashMap<String, String>()
	val members161 = HashMap<String, String>()
	for (line in mappings("1.6.1")) when (line[0]) {
		"CL:" -> classes161[line[2]] = line[1]
		"FD:" -> members161[line[2]] = line[1].substringAfterLast('/')
		"MD:" -> members161[line[3] + line[4]] = line[1].substringAfterLast('/')
	}
	// 1.6.2 obfuscated name -> 1.6.1 obfuscated name; members keyed "owner/name" (fields) or "owner/name+desc"
	val classes = HashMap<String, String>()
	val members = HashMap<String, String>()
	for (line in mappings("1.6.2")) when (line[0]) {
		"CL:" -> classes161[line[2]]?.let { classes[line[1]] = it }
		"FD:" -> members161[line[2]]?.let { members[line[1]] = it }
		"MD:" -> members161[line[3] + line[4]]?.let { members[line[1] + line[2]] = it }
	}
	// Class hierarchy (game and mod), to find where an inherited member referenced through a subclass is declared
	val supers = HashMap<String, List<String>>()
	fun readHierarchy(jar: File) = ZipFile(jar).use { zip ->
		for (entry in zip.entries()) if (entry.name.endsWith(".class")) {
			val reader = ClassReader(zip.getInputStream(entry).readBytes())
			supers[reader.className] = listOfNotNull(reader.superName) + reader.interfaces
		}
	}
	readHierarchy(minecraft)
	readHierarchy(input)
	fun resolve(owner: String, key: (String) -> String): String? {
		val queue = ArrayDeque(listOf(owner))
		while (queue.isNotEmpty()) {
			val type = queue.removeFirst()
			members[key(type)]?.let { return it }
			queue.addAll(supers[type].orEmpty())
		}
		return null
	}
	val remapper = object : Remapper() {
		override fun map(internalName: String) = classes[internalName] ?: internalName
		override fun mapFieldName(owner: String, name: String, descriptor: String) = resolve(owner) { "$it/$name" } ?: name
		override fun mapMethodName(owner: String, name: String, descriptor: String) =
			if (name.startsWith("<")) name else resolve(owner) { "$it/$name$descriptor" } ?: name
	}
	output.parentFile.mkdirs()
	ZipFile(input).use { zip ->
		ZipOutputStream(output.outputStream()).use { out ->
			for (entry in zip.entries()) {
				if (entry.isDirectory) continue
				var bytes = zip.getInputStream(entry).readBytes()
				if (entry.name.endsWith(".class")) {
					val writer = ClassWriter(0)
					ClassReader(bytes).accept(ClassRemapper(writer, remapper), 0)
					bytes = writer.toByteArray()
				}
				out.putNextEntry(ZipEntry(entry.name))
				out.write(bytes)
			}
		}
	}
}

val renameTo161 = tasks.register("renameTo161") {
	val input = tasks.named<Jar>("remapJar").flatMap { it.archiveFile }
	val output = layout.buildDirectory.file("renamed/xcraft-$version.jar")
	// Two configurations: in one, Gradle would resolve the version conflict down to a single Forge
	val universals = files(
		configurations.detachedConfiguration(dependencies.create("net.minecraftforge:forge:1.6.1-$forge:universal@jar")),
		configurations.detachedConfiguration(dependencies.create("net.minecraftforge:forge:1.6.2-$devForge:universal@jar")),
	)
	val cache = gradle.gradleUserHomeDir.resolve("caches/unimined/net/minecraft/minecraft/1.6.2")
	inputs.file(input)
	inputs.files(universals)
	outputs.file(output)
	doLast {
		// The obfuscated 1.6.2 game with Forge, for the class hierarchy (Unimined built it while compiling)
		val minecraft = cache.walk().first { it.name.endsWith("-official.jar") && it.name.contains("+forge-1.6.2-") }
		renameTo161(input.get().asFile, output.get().asFile, universals.files, minecraft)
	}
}
// The jar that ships: remapJar's, or for 1.6.1 the renamed one
val modJar: Provider<RegularFile> = if (mc == "1.6.1") renameTo161.map { layout.buildDirectory.file("renamed/xcraft-$version.jar").get() }
	else tasks.named<Jar>("remapJar").flatMap { it.archiveFile }
if (mc == "1.6.1") tasks.named("build") { dependsOn(renameTo161) }

// Same layout as the root build: build/libs/<version>/
val buildAndCollect by tasks.registering(Copy::class) {
	group = "build"
	from(modJar, tasks.named("sourcesJar"))
	into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
	dependsOn("build")
}

// Publishing: same switches and secrets as the root build (see CONTRIBUTING.md)
fun env(name: String): String? = providers.environmentVariable(name).orNull?.takeIf(String::isNotBlank)
fun envTrue(name: String) = env(name).equals("true", ignoreCase = true)

publishMods {
	file = modJar
	additionalFiles.from(tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").flatMap { it.archiveFile })
	type = STABLE
	version = project.version.toString()
	displayName = "xCraft $modVersion Forge $mc"
	changelog = rootProject.file("../CHANGELOG.md").takeIf { it.exists() }?.readText() ?: ""
	dryRun = envTrue("PUB_DRY_RUN") || !envTrue("PUB_MODS_ENABLE")

	if (env("PUB_MODRINTH_TOKEN") != null) modrinth {
		accessToken = env("PUB_MODRINTH_TOKEN")
		projectId = env("PUB_MODRINTH_PROJECT_ID") ?: "v8gHddm5"
		modLoaders.add("forge")
		minecraftVersions.add(mc)
	}
	if (env("PUB_CURSEFORGE_TOKEN") != null && env("PUB_CURSEFORGE_PROJECT_ID") != null) curseforge {
		accessToken = env("PUB_CURSEFORGE_TOKEN")
		projectId = env("PUB_CURSEFORGE_PROJECT_ID")
		modLoaders.add("forge")
		minecraftVersions.add(mc)
		serverRequired = true
		clientRequired = false
	}
}
