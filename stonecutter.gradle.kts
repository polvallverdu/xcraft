plugins {
	alias(libs.plugins.stonecutter)
	alias(libs.plugins.mod.publish.plugin)
	alias(libs.plugins.loom.back.compat).apply(false)
	alias(libs.plugins.neoforged.moddev).apply(false)
	alias(libs.plugins.legacyforge.moddev).apply(false)
	id("mod-root")
}

stonecutter active file(".sc_active_version")

stonecutter parameters {
	constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge", "forge")
	swaps["mod_version"] = "\"${properties.get<String>("mod.version")}\";"
	swaps["mod_id"] = "\"${properties.get<String>("mod.id")}\";"
	swaps["mod_name"] = "\"${properties.get<String>("mod.name")}\";"
	swaps["mod_group"] = "\"${properties.get<String>("mod.group")}\";"
	swaps["minecraft"] = "\"${current.version}\";"
	// MidnightLib has no build for these targets; the config-screen bridge is compiled out there
	constants["midnightlib"] = !(current.project.endsWith("-forge") && current.parsed > "1.20.1") && current.project !in setOf<String>("1.14.4-fabric", "1.15.2-fabric", "1.16.5-fabric", "1.17.1-forge", "1.18.1-fabric", "1.18.1-forge", "1.18.2-forge", "1.19.4-forge")
	}
