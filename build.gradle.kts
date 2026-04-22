plugins {
    application
}

application {
    mainClass = "com.github.jglick.jkillthread.Main"
}

tasks.withType<Jar> {
    manifest {
        attributes["Manifest-Version"] = "1.0"
        attributes["Main-Class"] = "com.github.jglick.jkillthread.Main"
        attributes["Agent-Class"] = "com.github.jglick.jkillthread.Agent"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
