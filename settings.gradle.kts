rootProject.name = "kotlin-union"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/versions.toml"))
        }
    }
}

includeBuild("convention-plugins")
