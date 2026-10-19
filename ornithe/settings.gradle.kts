// xCraft for the Minecraft releases Legacy Fabric has no support for (1.9-1.9.3, 1.10-1.10.1, 1.11-1.11.1,
// 1.12-1.12.1, 1.13-1.13.1), on Ornithe: Fabric Loom + Ploceus, Feather names on Calamus gen-2 intermediary.
pluginManagement {
	repositories {
		// Mirror of maven.fabricmc.net (Fabric Loom, Loader, Mixin), served by Legacy Fabric
		maven("https://repo.legacyfabric.net/fabricmc/") { name = "FabricMirror" }
		maven("https://maven.ornithemc.net/releases") { name = "Ornithe Releases" }
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
		// -Pxcraft.versions=1.9.2 limits the build to those versions (CI matrix); the VCS version is always kept
		val vcs = "1.12.1"
		val only = providers.gradleProperty("xcraft.versions").orNull
			?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.toSet()
		listOf("1.13.1", "1.13", "1.12.1", "1.12", "1.11.1", "1.11", "1.10.1", "1.10",
			"1.9.3", "1.9.2", "1.9.1", "1.9").forEach {
			if (only == null || it in only || it == vcs) version(it, it)
		}
		vcsVersion = vcs
	}
}

rootProject.name = "xcraft-ornithe"
