import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
        compilations.getByName("main") {
            dependencies {
                implementation(npm("firebase", "10.7.0"))
            }
        }
    }

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.firebase.auth)
                implementation(libs.firebase.firestore)
                implementation(libs.firebase.analytics)
                implementation(libs.koin.android)

                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.material3)
                implementation(compose.foundation)
                implementation(compose.runtime)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.coroutines.core)

                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

            }
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
        commonTest.dependencies {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val wasmJsMain by getting {
            abstract class GenerateFirebaseConfigTask : DefaultTask() {

                @OutputDirectory
                val outputDir = project.layout.buildDirectory.dir("generated/firebase/wasm")

                @TaskAction
                fun generate() {
                    val localProperties = Properties().apply {
                        val file = project.rootProject.file("local.properties")
                        if (file.exists()) load(file.inputStream())
                    }

                    val outputFile = outputDir.get().asFile.resolve("FirebaseConfig.kt")
                    outputFile.parentFile.mkdirs()
                    outputFile.writeText(
                        """
            package org.example.project.shared

            object FirebaseConfig {
             const val WEB_API_KEY = "${localProperties["WEB_API_KEY"] ?: ""}"
             const val WEB_AUTH_DOMAIN = "${localProperties["WEB_AUTH_DOMAIN"] ?: ""}"
             const val WEB_STORAGE_BUCKET = "${localProperties["WEB_STORAGE_BUCKET"] ?: ""}"
             const val SENDER_ID = "${localProperties["SENDER_ID"] ?: ""}"
             const val PROJECT_ID = "${localProperties["PROJECT_ID"] ?: ""}"
             const val APP_ID = "${localProperties["APP_ID"] ?: ""}"
           }
           """.trimIndent()
                    )
                }
            }
            val generateFirebaseConfig =
                tasks.register<GenerateFirebaseConfigTask>("generateFirebaseConfig")

            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                dependsOn(generateFirebaseConfig)
            }
            kotlin.srcDir(generateFirebaseConfig.flatMap { it.outputDir })
        }
    }
}

android {
    namespace = "org.example.project.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
