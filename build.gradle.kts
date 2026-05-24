plugins {
    application
}

group = "portaltool"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("portaltool.PortalToolApp")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "portaltool.PortalToolApp"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
