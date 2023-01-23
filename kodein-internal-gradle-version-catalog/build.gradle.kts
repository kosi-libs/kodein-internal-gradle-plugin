plugins {
    `version-catalog`
    `maven-publish`
}

catalog.versionCatalog {
    from(files("$rootDir/gradle/globals.versions.toml"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}
