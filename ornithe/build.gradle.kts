plugins {
	id("fabric-loom") // first: Ploceus reads Loom's extension when applied
	id("ploceus")
	id("me.modmuss50.mod-publish-plugin")
}

val mc = stonecutter.current.version
val modVersion = property("mod.version") as String

version = "$modVersion-ornithe+$mc"
base.archivesName = "xcraft"

// Loom adds maven.fabricmc.net on its own; use Legacy Fabric's mirror of it instead
repositories.removeIf { it is MavenArtifactRepository && it.url.host == "maven.fabricmc.net" }
repositories.maven("https://repo.legacyfabric.net/fabricmc/") { name = "FabricMirror" }

dependencies {
	minecraft("com.mojang:minecraft:$mc")
	// Feather on Calamus gen-2 intermediary (the Ploceus default); no raven/sparrow/nests exist for these versions
	mappings(ploceus.featherMappings(property("feather.build") as String))
	modImplementation("net.fabricmc:fabric-loader:${property("fabric.loader")}")
	// No Ornithe Standard Libraries: mixins cover everything
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

// Every Ornithe target is its own build (one Minecraft version per jar)
val hotfixes = emptyList<String>()
val mcRange = mc

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
	displayName = "xCraft $modVersion Ornithe $mc"
	changelog = rootProject.file("../CHANGELOG.md").takeIf { it.exists() }?.readText() ?: ""
	dryRun = envTrue("PUB_DRY_RUN") || !envTrue("PUB_MODS_ENABLE")
	val gameVersions = listOf(mc) + hotfixes

	if (env("PUB_MODRINTH_TOKEN") != null) modrinth {
		accessToken = env("PUB_MODRINTH_TOKEN")
		projectId = env("PUB_MODRINTH_PROJECT_ID") ?: "v8gHddm5"
		modLoaders.add("ornithe")
		minecraftVersions.addAll(gameVersions)
	}
	// CurseForge has no Ornithe loader tag, and a "Fabric" tag would mislead players: not published there
}
