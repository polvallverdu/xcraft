@file:Suppress("unused", "DuplicatedCode")

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import kotlinx.serialization.json.Json
import me.modmuss50.mpp.ReleaseType
import net.peanuuutz.tomlkt.Toml
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.maven
import java.util.*

val Project.sc: StonecutterBuildExtension
	get() = extensions.getByType<StonecutterBuildExtension>()

fun Project.prop(name: String): String = (project.sc.properties.get<String>(name))

fun Project.propsList(vararg segments: String): List<String> = runCatching {
	sc.properties.raw(*segments).asList().map { it.toString() }
}.getOrElse { error("Missing or malformed '${segments.joinToString(".")}' in stonecutter.properties.toml") }

fun Project.env(variable: String): String? {
	providers.environmentVariable(variable).orNull?.let { return it }
	return rootProject.file(".env").takeIf { it.exists() }?.let { f ->
		Properties().apply { f.inputStream().use(::load) }.getProperty(variable)
	}
}

fun Project.envTrue(variable: String): Boolean = env(variable)?.toDefaultLowerCase() == "true"

fun releaseTypeFromChannelTag(channelTag: String): ReleaseType =
	ReleaseType.of(channelTag.substringAfter('-').substringBefore('.').ifEmpty { "stable" })

fun RepositoryHandler.strictMaven(
	url: String, vararg groups: String, configure: MavenArtifactRepository.() -> Unit = {}
) = exclusiveContent {
	forRepository { maven(url) { configure() } }
	filter { groups.forEach(::includeGroup) }
}

val JSON = Json { prettyPrint = true; encodeDefaults = true; explicitNulls = false }
val TOML = Toml { explicitNulls = false }

/** Minecraft versions this target's jar is published for: its own plus `publish.additionalVersions`. */
fun Project.supportedMcVersions(): List<String> =
	(listOf(prop("deps.minecraft")) + sc.properties.rawOrNull("publish", "additionalVersions")?.to<List<String>>().orEmpty())
		.sortedWith(compareBy<String, List<Int>>(McVersionComparator) { v -> v.split('.').map(String::toInt) })

private object McVersionComparator : Comparator<List<Int>> {
	override fun compare(a: List<Int>, b: List<Int>): Int {
		for (i in 0 until maxOf(a.size, b.size)) {
			val c = a.getOrElse(i) { 0 }.compareTo(b.getOrElse(i) { 0 })
			if (c != 0) return c
		}
		return 0
	}
}

/** fabric.mod.json predicate covering every supported version, e.g. `>=1.21.6 <=1.21.8`. */
fun Project.fabricMcRange(): String = supportedMcVersions().let { v ->
	if (v.size == 1) v.single() else ">=${v.first()} <=${v.last()}"
}

/** (Neo)Forge maven range covering every supported version, e.g. `[1.21.6,1.21.8]`. */
fun Project.forgeMcRange(): String = supportedMcVersions().let {
	// Forge 1.17 rejects a range with identical bounds, so a single version is written as [v]
	if (it.size == 1) "[${it.single()}]" else "[${it.first()},${it.last()}]"
}

/** NeoForge release line of a Minecraft version (1.21.6 -> 21.6, 1.21 -> 21.0, 26.1 -> 26.1). */
fun neoForgeLine(mc: String): String {
	val p = mc.split('.')
	return if (p[0] == "1") "${p[1]}.${p.getOrElse(2) { "0" }}" else mc
}
