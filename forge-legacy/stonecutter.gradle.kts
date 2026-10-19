plugins {
	id("dev.kikugie.stonecutter")
	id("xyz.wagyourtail.unimined") version "1.4.2-SNAPSHOT" apply false
	id("me.modmuss50.mod-publish-plugin") version "2.1.1" apply false
}

stonecutter active file(".sc_active_version")

stonecutter parameters {
	swaps["minecraft"] = "\"${current.version}\";"
}
