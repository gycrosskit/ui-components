plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

group = rootProject.group
version = rootProject.version

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    ohosArm64()
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            // 只取依赖坐标并推荐编译基线，组件不向消费者传播宿主的强版本约束。
            api(libs.kuikly.core.get().module.toString()) {
                version { prefer(libs.versions.kuikly.get()) }
            }
            api(libs.kuikly.compose.get().module.toString()) {
                version { prefer(libs.versions.kuikly.get()) }
            }
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        androidUnitTest.dependencies {
            implementation("org.robolectric:robolectric:4.16.1")
        }
    }
}

android {
    namespace = "com.dgtang.live.ui.components"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
publishing { repositories.maven { name = "staging"; url = uri(rootProject.layout.buildDirectory.dir("maven")) } }
