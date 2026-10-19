import net.minecraftforge.renamer.gradle.RenameJar

// Forge 1.20.2+ (ModDevGradle's legacyforge stops at 1.20.1). 1.20.2-1.20.4 run on SRG names,
// so their jar is renamed with Renamer and gets a mixin refmap; 1.20.6+ runs on Mojang names.
plugins {
	id("mod-platform")
	alias(libs.plugins.forgegradle)
	alias(libs.plugins.forgerenamer)
}

stonecutter {
	replacements.string(current.parsed >= "1.21.11") {
		replace("ResourceLocation", "Identifier")
	}
}

val mcv = sc.current.version
val srg = sc.current.parsed < "1.20.5"

platform {
	loader = "forge"
	dependencies {
		required("minecraft") {
			forgeLikeVersionRange = forgeMcRange()
		}
		required("forge") {
			// The Minecraft range above is the real constraint; Forge builds differ per Minecraft version
			forgeLikeVersionRange.set("[0,)")
		}
	}
}

minecraft {
	// 26.x is unobfuscated: no mappings
	if (sc.current.parsed < "26") mappings("official", mcv)

	runs {
		register("server") {
			workingDir = layout.projectDirectory.dir("run")
			args("--nogui")
		}
	}
}

repositories {
	mavenCentral()
	maven(fg.forgeMaven)
	maven(fg.minecraftLibsMaven)
	minecraft.mavenizer(this)
}

dependencies {
	implementation(minecraft.dependency("net.minecraftforge:forge:${prop("deps.minecraft")}-${prop("deps.forge")}"))
	if (srg) annotationProcessor("org.spongepowered:mixin:${libs.versions.mixin.get()}:processor")
}

tasks.named("compileJava") {
	dependsOn(tasks.named("stonecutterGenerate"))
}

if (srg) {
	renamer {
		mappings(minecraft.dependency.toSrg)
		enableMixinRefmaps {
			config("${prop("mod.id")}.mixins.json")
			source(sourceSets["main"]) {
				refMap = "${prop("mod.id")}.mixins.refmap.json"
			}
			jar(tasks.named<Jar>("jar"))
		}
		classes(tasks.named<Jar>("jar")) {
			mappings(renamer.mixin.generatedMappings)
			archiveClassifier = "srg"
		}
	}
}

// No tests; their classpath would pull Minecraft's client natives, some of which are missing on macOS
tasks.named<Test>("test") {
	enabled = false
	classpath = files()
}
