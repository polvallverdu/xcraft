pluginManagement {
	repositories {
		mavenLocal()
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
		maven("https://repo.legacyfabric.net/fabricmc/") { name = "FabricMirror" } // fallback when maven.fabricmc.net is down
		maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
		maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
		maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
		maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
	}
	includeBuild("build-logic")
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
	id("dev.kikugie.stonecutter") version "0.9.8"
	id("dev.kikugie.loom-back-compat") version "0.4.1"
}

stonecutter {
	create(rootProject) {
		// -Pxcraft.versions=1.20.1,1.21.4 limits the build to those Minecraft versions (used by the CI matrix,
		// so each job only sets up its own Minecraft). The VCS version is always kept: it holds the active sources.
		val vcs = "1.21.1-fabric"
		val only = providers.gradleProperty("xcraft.versions").orNull
			?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.toSet()

		fun match(version: String, vararg loaders: String) =
			loaders.forEach {
				val id = "$version-$it"
				if (only == null || version in only || id == vcs)
					version(id, version).buildscript = "build.$it.gradle.kts"
			}

		match("26.3", "fabric", "neoforge")
		match("26.2", "fabric", "neoforge")
		match("26.1.2", "fabric", "neoforge")
		match("1.21.11", "fabric", "neoforge")
		match("1.21.10", "fabric", "neoforge")
		match("1.21.8", "fabric", "neoforge")
		match("1.21.5", "fabric", "neoforge")
		match("1.21.4", "fabric", "neoforge")
		match("1.21.3", "fabric", "neoforge")
		match("1.21.1", "fabric", "neoforge")
		match("1.20.6", "fabric", "neoforge")
		match("1.20.4", "fabric", "neoforge")
		match("1.20.2", "fabric")
		match("1.20.1", "fabric", "forge")
		match("1.19.4", "fabric", "forge")
		match("1.19.2", "fabric", "forge")
		match("1.18.2", "fabric", "forge")
		match("1.18.1", "fabric", "forge")
		match("1.17.1", "fabric", "forge")
		match("1.16.5", "fabric")
		match("1.15.2", "fabric")
		match("1.14.4", "fabric")

		vcsVersion = vcs
	}
}


rootProject.name = "xcraft"
