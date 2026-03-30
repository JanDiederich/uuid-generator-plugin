import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.Constants
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij.platform") version "2.13.1"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.5.0"
    // Gradle Qodana Plugin
    id("org.jetbrains.qodana") version "2025.3.2"
}

val platformVersion = properties("platformVersion")
val pluginVerifierExcludeFailureLevels = properties("pluginVerifierExcludeFailureLevels")
val pluginVerifierMutePluginProblems = properties("pluginVerifierMutePluginProblems")

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
    maven("https://repo1.maven.org/maven2/")
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("com.github.f4b6a3:ulid-creator:5.2.4")
    // Support UUIDv7
    implementation("com.github.f4b6a3:uuid-creator:6.1.0")
    implementation("cool.graph:cuid-java:0.1.1")

    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("com.willowtreeapps.assertk:assertk-jvm:${properties("assertk-jvm.version")}")

    // Needed for compatibility with test dependencies.
    testImplementation(kotlin("stdlib"))

    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
        bundledPlugins(
            properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty).toList()
        )

        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
        version = properties("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map { fileContent ->
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(fileContent.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }.let {
                val pluginVersion = properties("pluginVersion")
                val gitHubContentBasePath = "https://raw.githubusercontent.com/leomillon/uuid-generator-plugin"
                val gitHubRef = when {
                    pluginVersion.endsWith("-SNAPSHOT") -> "master"
                    else -> "v$pluginVersion"
                }
                // Replace local url with GitHub base url and set width to 500px
                it.replace(
                    """src="./""", """width="500" src="$gitHubContentBasePath/$gitHubRef/"""
                )
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.provider { version }.map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(properties("pluginVersion")) ?: getUnreleased()).withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        // See https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-migration.html#intellijdownloadsources
        // properties("platformDownloadSources")
        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            // Don't limit the plugin.
            // untilBuild = properties("pluginUntilBuild")
        }
    }

    pluginVerification {
        val pluginVerifierMutePluginProblems = pluginVerifierMutePluginProblems
        if (pluginVerifierMutePluginProblems.isNotEmpty()) {
            logger.lifecycle("Muting the following Plugin Verifier Problems: $pluginVerifierMutePluginProblems")
            freeArgs = listOf("-mute", pluginVerifierMutePluginProblems)
        }

        fun getFailureLevels(): EnumSet<VerifyPluginTask.FailureLevel> {
            val includeFailureLevels = EnumSet.allOf(VerifyPluginTask.FailureLevel::class.java)
            val desiredFailureLevels = pluginVerifierExcludeFailureLevels.split(",").map(String::trim)
                // Remove empty strings; this happens when user sets nothing (ie, `pluginVerifierExcludeFailureLevels =`)
                .filter { it.isNotBlank() && it != "null" }
                .forEach { failureLevel ->
                    when (failureLevel) {
                        "ALL" -> return EnumSet.allOf(VerifyPluginTask.FailureLevel::class.java)
                        "NONE" -> return EnumSet.noneOf(VerifyPluginTask.FailureLevel::class.java)
                        else -> {
                            try {
                                val enumFailureLevel = VerifyPluginTask.FailureLevel.valueOf(failureLevel)
                                includeFailureLevels.remove(enumFailureLevel)
                            } catch (ignored: Exception) {
                                val msg = "Failure Level \"$failureLevel\" is *NOT* valid. Please select from: ${
                                    EnumSet.allOf(VerifyPluginTask.FailureLevel::class.java)
                                }."
                                logger.error(msg)
                                throw Exception(msg)
                            }
                        }
                    }
                }

            return includeFailureLevels
        }

        val failureLevels = getFailureLevels()
        logger.debug("Using {} Failure Levels: {}", failureLevels.size, failureLevels)
        failureLevel.set(failureLevels)
        ides {
            logger.lifecycle("Verifying against IntelliJ $platformVersion")
            create(
                type = properties("platformType"),
                version = properties("platformVersion"),
            ) {
                useCache = true
            }

            recommended()
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

/* Configure Gradle Qodana Plugin - read more:
https://www.jetbrains.com/help/qodana/deploy-qodana.html#qodana+%7B+%7D+extension+configuration */
qodana {
    cachePath.set(projectDir.resolve(".qodana/cache").canonicalPath)
//    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
//    saveReport.set(true)
//    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            compilerOptions {
                /* Enforce a current Kotlin version so older plugins / dependencies don't trigger
                errors when using older Kotlin versions. */
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(it))
                apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
                // languageVersion controls which Kotlin language features are available.
                languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
            }
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    named("qodanaScan") {
        doFirst {
            // Docker requires the bind-mount source to exist before container creation.
            // mkdirs() is a no-op if the directory already exists.
            projectDir.resolve(".qodana/cache").mkdirs()
        }
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}

val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "-Drobot-server.port=8082",
                "-Dide.mac.message.dialogs.as.sheets=false",
                "-Djb.privacy.policy.text=<!--999.999-->",
                "-Djb.consents.confirmation.enabled=false",
            )
        }
    }

    plugins {
        robotServerPlugin(Constants.Constraints.LATEST_VERSION)
    }
}