plugins {
	id("dev.kikugie.stonecutter")
	// Ploceus extends Fabric Loom and uses its internals: keep Loom on Ploceus' 1.18 line
	id("fabric-loom") version "1.18.4" apply false
	id("ploceus") version "1.18.1" apply false
	id("me.modmuss50.mod-publish-plugin") version "2.1.1" apply false
}

stonecutter active file(".sc_active_version")

stonecutter parameters {
	swaps["minecraft"] = "\"${current.version}\";"
}
