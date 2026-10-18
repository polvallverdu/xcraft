plugins {
	id("mod-platform")
	id("dev.kikugie.loom-back-compat")
}

stonecutter {
	replacements.string(current.parsed >= "1.21.11") {
		replace("ResourceLocation", "Identifier")
	}
	// MutableComponent arrived in 1.16; before that withStyle/append live on Component itself
	replacements.string(current.parsed < "1.16") {
		replace("MutableComponent", "Component")
	}
}

platform {
	loader = "fabric"
	dependencies {
		// Covers the hotfix versions this jar is also published for (publish.additionalVersions)
		required("minecraft") {
			fabricLikeVersionRange = fabricMcRange()
		}
		// Only Fabric API's command module is used. Depend on that module's id: it is stable across every
		// Fabric API release, while Fabric API itself was "fabric" (not "fabric-api") before 1.19.2.
		required("fabric-api") {
			modid = if (sc.current.parsed >= "1.19") "fabric-command-api-v2" else "fabric-command-api-v1"
			slug("fabric-api")
		}
		// Optional: adds the in-game config screen when installed
		if (hasProperty("deps.midnightlib")) optional("midnightlib") {
			slug("midnightlib")
			fabricLikeVersionRange = ">=${prop("deps.midnightlib").substringBefore('+').substringBefore('-')}"
		}
		required("fabricloader") {
			fabricLikeVersionRange = ">=0.14.0"
		}
	}
}

loom {
	val devJvmArgs = propsList("mod", "dev_jvm_args") +
		propsList("mod", "dev_jvm_args_mixin_debug")
	runs.named("client") {
		client()
		generateRunConfig.set(true)
		runDirectory.set(layout.projectDirectory.dir("run"))
		programArguments.add("--username=Dev")
		jvmArguments.addAll(devJvmArgs)
		displayName.set("Fabric Client")
	}
	runs.named("server") {
		server()
		generateRunConfig.set(true)
		runDirectory.set(layout.projectDirectory.dir("run"))
		jvmArguments.addAll(devJvmArgs)
		displayName.set("Fabric Server")
	}
}

// -Pxcraft.fabricMirror: fetch Fabric artifacts from Legacy Fabric's mirror of maven.fabricmc.net (for when it is unreachable)
if (providers.gradleProperty("xcraft.fabricMirror").isPresent) {
	repositories.removeIf { it is MavenArtifactRepository && it.url.host == "maven.fabricmc.net" }
	repositories.maven("https://repo.legacyfabric.net/fabricmc/") { name = "FabricMirror" }
}

repositories {
	mavenCentral()
	strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
	strictMaven("https://maven.terraformersmc.com/releases", "com.terraformersmc") { name = "TerraformersMC" }
}

configurations.all {
	resolutionStrategy {
		force("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
	if (sc.current.parsed < "26") {
		mappings(loom.layered {
			officialMojangMappings()
			if (hasProperty("deps.parchment"))
				parchment("org.parchmentmc.data:parchment-${prop("deps.parchment")}@zip")
		})
	}
	modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
	if (hasProperty("deps.midnightlib")) {
		modCompileOnly("maven.modrinth:midnightlib:${prop("deps.midnightlib")}")
		// Run dev with -PnoMidnightlib to test the plain JSON config path
		if (!hasProperty("noMidnightlib")) modLocalRuntime("maven.modrinth:midnightlib:${prop("deps.midnightlib")}")
	}
	// Mod Menu provides the "Config" button in dev runs; it's optional for players
	if (hasProperty("deps.modmenu")) modLocalRuntime("com.terraformersmc:modmenu:${prop("deps.modmenu")}") { isTransitive = false }
}
