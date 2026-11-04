plugins {
    kotlin("multiplatform") version "2.2.21-1.0.0"
    kotlin("plugin.compose") version "2.2.21-1.0.0"
    id("com.android.application") version "8.10.1"
}
val uiComponentsVersion = providers.gradleProperty("uiComponentsVersion").orElse("1.0.0").get()
val kuiklyRenderFrameworkDir = providers.gradleProperty("kuiklyRenderFrameworkDir").orNull
kotlin {
    androidTarget { compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
    iosArm64()
    iosX64 { binaries.framework {
        baseName = "UiComponentsConsumer"
        isStatic = true
        kuiklyRenderFrameworkDir?.let { linkerOpts("-F$it", "-framework", "OpenKuiklyIOSRender") }
    } }
    iosSimulatorArm64 { binaries.framework {
        baseName = "UiComponentsConsumer"
        isStatic = true
        kuiklyRenderFrameworkDir?.let { linkerOpts("-F$it", "-framework", "OpenKuiklyIOSRender") }
    } }
    ohosArm64()
    sourceSets.commonMain.dependencies {
        api("com.github.gycrosskit.ui-components:ui-components:$uiComponentsVersion")
    }
}
android {
    namespace = "io.github.gycrosskit.uicomponents.consumer"
    compileSdk = 36
    defaultConfig { applicationId = "io.github.gycrosskit.uicomponents.consumer"; minSdk = 24; targetSdk = 36 }
    buildTypes { getByName("release") {
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
    } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_11; targetCompatibility = JavaVersion.VERSION_11 }
}
tasks.register("verifyNoCmpUi") {
    doLast {
        val resolution = configurations.getByName("debugRuntimeClasspath").incoming.resolutionResult
        check(resolution.allDependencies.none { it is org.gradle.api.artifacts.result.UnresolvedDependencyResult }) { "All dependencies must resolve before checking UI isolation" }
        val deps = resolution.allComponents.mapNotNull { it.moduleVersion }
        val forbidden = deps.filter {
            it.group.startsWith("org.jetbrains.compose.ui") || it.group.startsWith("org.jetbrains.compose.foundation") ||
                it.group.startsWith("org.jetbrains.compose.material") || it.group.startsWith("androidx.compose.ui") ||
                it.group.startsWith("androidx.compose.foundation") || it.group.startsWith("androidx.compose.material")
        }
        check(forbidden.isEmpty()) { "Second CMP UI found: $forbidden" }
        check(deps.any { it.group == "com.tencent.kuikly-open" && it.name.startsWith("compose") })
        val components = deps.filter { it.group == "com.github.gycrosskit.ui-components" }
        check(components.isNotEmpty() && components.all { it.version == uiComponentsVersion }) { "Wrong component version: $components" }
        println("PASS exact UI component version and Kuikly Compose without second CMP UI")
    }
}
