// xCraft for Forge 1.3.2 - 1.16.5. A separate build from the root one: these Forge versions officially need
// ForgeGradle 1-5 (Gradle 2-7), so this build uses Unimined, which sets up every Forge era on current Gradle.
// One mapping family (MCP) for every version, and event-based code (no mixins).
pluginManagement {
	repositories {
		maven("https://maven.wagyourtail.xyz/releases") { name = "WagYourTail" }
		maven("https://maven.wagyourtail.xyz/snapshots") { name = "WagYourTail Snapshots" }
		// Mirror of maven.fabricmc.net (tiny-remapper, used by Unimined), served by Legacy Fabric
		maven("https://repo.legacyfabric.net/fabricmc/") { name = "FabricMirror" }
		maven("https://maven.minecraftforge.net/") { name = "Forge" }
		maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
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
		listOf(
			"1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1", "1.15.2", "1.15.1", "1.15", "1.14.4", "1.14.3", "1.14.2",
			"1.13.2", "1.12.2", "1.12.1", "1.12", "1.11.2", "1.11", "1.10.2", "1.10", "1.9.4", "1.9", "1.8.9", "1.8.8",
			"1.8", "1.7.10", "1.7.2", "1.6.4", "1.6.2", "1.6.1", "1.5.2", "1.5.1", "1.5", "1.4.7", "1.4.6", "1.4.5",
			"1.4.4", "1.4.2", "1.3.2",
		).forEach {
			if (only == null || it in only || it == vcs) version(it, it)
		}
		vcsVersion = vcs
	}
}

rootProject.name = "xcraft-forge-legacy"
