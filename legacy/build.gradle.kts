plugins {
	id("legacy-looming") // applies Fabric Loom (remap) itself
	id("me.modmuss50.mod-publish-plugin")
}

val mc = stonecutter.current.version
val modVersion = property("mod.version") as String

version = "$modVersion-legacy-fabric+$mc"
base.archivesName = "xcraft"

// Loom adds maven.fabricmc.net on its own; use Legacy Fabric's mirror of it instead
repositories.removeIf { it is MavenArtifactRepository && it.url.host == "maven.fabricmc.net" }
repositories.maven("https://repo.legacyfabric.net/fabricmc/") { name = "FabricMirror" }

dependencies {
	minecraft("com.mojang:minecraft:$mc")
	// Legacy Yarn on generation-1 intermediary (the Legacy Looming default)
	mappings("net.legacyfabric:yarn:$mc+build.${if (mc == "1.7.10") 603 else 604}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("fabric.loader")}")
	// No Legacy Fabric API: there is none for 1.13.2, and mixins cover everything
}

java {
	toolchain.languageVersion = JavaLanguageVersion.of(8)
	withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }

loom {
	runs.named("server") {
		runDir("run")
		vmArg("-Dxcraft.debug=true")
	}
}

// Minecraft versions this jar is for: its own plus the hotfixes listed in gradle.properties
val hotfixes = (findProperty("hotfixes.$mc") as String?)?.split(',')?.map(String::trim).orEmpty()
val mcRange = if (hotfixes.isEmpty()) mc else {
	val all = (hotfixes + mc).sortedWith(compareBy({ it.split('.')[1].toInt() }, { it.split('.').getOrNull(2)?.toInt() ?: 0 }))
	">=${all.first()} <=${all.last()}"
}

tasks.processResources {
	val props = mapOf("version" to modVersion, "minecraft" to mcRange)
	inputs.properties(props)
	filesMatching("fabric.mod.json") { expand(props) }
}

// Same layout as the root build: build/libs/<version>/
val buildAndCollect by tasks.registering(Copy::class) {
	group = "build"
	from(tasks.named("remapJar"), tasks.named("remapSourcesJar"))
	into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
	dependsOn("build")
}

// Publishing: same switches and secrets as the root build (see CONTRIBUTING.md)
fun env(name: String): String? = providers.environmentVariable(name).orNull?.takeIf(String::isNotBlank)
fun envTrue(name: String) = env(name).equals("true", ignoreCase = true)

publishMods {
	file = tasks.named<org.gradle.jvm.tasks.Jar>("remapJar").flatMap { it.archiveFile }
	additionalFiles.from(tasks.named<org.gradle.jvm.tasks.Jar>("remapSourcesJar").flatMap { it.archiveFile })
	type = STABLE
	version = project.version.toString()
	displayName = "xCraft $modVersion Legacy Fabric $mc"
	changelog = rootProject.file("../CHANGELOG.md").takeIf { it.exists() }?.readText() ?: ""
	dryRun = envTrue("PUB_DRY_RUN") || !envTrue("PUB_MODS_ENABLE")
	val gameVersions = listOf(mc) + hotfixes

	if (env("PUB_MODRINTH_TOKEN") != null) modrinth {
		accessToken = env("PUB_MODRINTH_TOKEN")
		projectId = env("PUB_MODRINTH_PROJECT_ID") ?: "v8gHddm5"
		modLoaders.add("legacy-fabric")
		minecraftVersions.addAll(gameVersions)
	}
	if (env("PUB_CURSEFORGE_TOKEN") != null && env("PUB_CURSEFORGE_PROJECT_ID") != null) curseforge {
		accessToken = env("PUB_CURSEFORGE_TOKEN")
		projectId = env("PUB_CURSEFORGE_PROJECT_ID")
		// CurseForge has no Legacy Fabric loader tag
		modLoaders.add("fabric")
		minecraftVersions.addAll(gameVersions)
		serverRequired = true
		clientRequired = false
	}
}
