plugins {
	id("dev.kikugie.stonecutter")
	id("legacy-looming") version "1.16.1" apply false
}

stonecutter active file(".sc_active_version")

stonecutter parameters {
	swaps["minecraft"] = "\"${current.version}\";"
}
