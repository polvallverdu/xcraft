plugins {
	id("legacy-looming") // applies Fabric Loom (remap) itself
}

val mc = stonecutter.current.version
val modVersion = property("mod.version") as String

version = "$modVersion-fabric+$mc"
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
