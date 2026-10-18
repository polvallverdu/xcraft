// xCraft for Minecraft 1.7.10 - 1.13.2 on Legacy Fabric.
// A separate build from the root one: these versions have no Mojang mappings, so they use Legacy Yarn
// names (one mapping family for all of them) and Legacy Looming instead of Fabric Loom 1.19.
pluginManagement {
	repositories {
		maven("https://repo.legacyfabric.net/legacyfabric/") { name = "LegacyFabric" }
		// Mirror of maven.fabricmc.net (Fabric Loom, Loader, Mixin), served by Legacy Fabric
		maven("https://repo.legacyfabric.net/fabricmc/") { name = "FabricMirror" }
		maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
		maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
	id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
	create(rootProject) {
		// -Pxcraft.versions=1.8.9 limits the build to those versions (CI matrix); the VCS version is always kept
		val vcs = "1.12.2"
		val only = providers.gradleProperty("xcraft.versions").orNull
			?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.toSet()
		listOf("1.13.2", "1.12.2", "1.11.2", "1.10.2", "1.9.4", "1.8.9", "1.7.10").forEach {
			if (only == null || it in only || it == vcs) version(it, it)
		}
		vcsVersion = vcs
	}
}

rootProject.name = "xcraft-legacy"
