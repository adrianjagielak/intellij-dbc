plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(providers.gradleProperty("platformVersion").get())
    }
}

tasks {
    compileJava {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    wrapper {
        gradleVersion = "8.10"
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.github.adrianjagielak.intellijdbc"
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = "241"
            untilBuild = provider { null }
        }
        vendor {
            name = "Adrian Jagielak"
            url = "https://github.com/adrianjagielak"
        }
    }
}
