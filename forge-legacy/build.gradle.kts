plugins {
	java
	id("xyz.wagyourtail.unimined")
	id("me.modmuss50.mod-publish-plugin")
}

val mc = stonecutter.current.version
val modVersion = property("mod.version") as String
val forge = property("forge.$mc") as String
val (mcpChannel, mcpVersion) = (property("mcp.$mc") as String).split(':', limit = 2)
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
	version(mc)
	mappings {
		searge()
		mcp(mcpChannel, mcpVersion)
	}
	minecraftForge {
		loader(forge)
		// Run Forge's setup tools on the build JVM instead of a JDK matching each release
		useToolchains = false
		// Unimined assumes the old dotted AT format below 1.7.10, but Forge 1.7.2's ATs already use spaces
		legacyATFormat = false
	}
	// The CI server tests boot the official Forge server instead (.github/scripts/smoke_prod.py)
	runs { off = true }
	defaultRemapJar = true
}

java {
	toolchain.languageVersion = JavaLanguageVersion.of(8)
	// Forge 1.7.2 scans mod classes with ASM 4, which can't read Java 8 class files
	val bytecode = if (stonecutter.eval(mc, "<1.7.10")) JavaVersion.VERSION_1_7 else JavaVersion.VERSION_1_8
	sourceCompatibility = bytecode
	targetCompatibility = bytecode
	withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.compilerArgs.add("-Xlint:-options")
}

sourceSets.main {
	java.exclude(if (fml) "dev/polv/xcraft/forge/ml/**" else "dev/polv/xcraft/forge/fml/**")
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

// Same layout as the root build: build/libs/<version>/
val buildAndCollect by tasks.registering(Copy::class) {
	group = "build"
	from(tasks.named("remapJar"), tasks.named("sourcesJar"))
	into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
	dependsOn("build")
}

// Publishing: same switches and secrets as the root build (see CONTRIBUTING.md)
fun env(name: String): String? = providers.environmentVariable(name).orNull?.takeIf(String::isNotBlank)
fun envTrue(name: String) = env(name).equals("true", ignoreCase = true)

publishMods {
	file = tasks.named<org.gradle.jvm.tasks.Jar>("remapJar").flatMap { it.archiveFile }
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
