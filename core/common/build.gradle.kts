import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
}

val properties = Properties()
val propertiesFile = project.rootProject.file("local.properties")
if (propertiesFile.exists()) {
    properties.load(propertiesFile.inputStream())
}

val bibliaDigitalToken: String =
    (
        properties.getProperty(
            "BIBLIA_DIGITAL_TOKEN",
        ) ?: project.findProperty("BIBLIA_DIGITAL_TOKEN") as? String
    )?.takeIf {
        it.isNotBlank()
    } ?: ""

android {
    namespace = "digital.tonima.bibliadigital.core.common"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BIBLIA_DIGITAL_TOKEN", "\"$bibliaDigitalToken\"")
        buildConfigField("String", "APPLICATION_ID", "\"digital.tonima.bibliadigital\"")
        buildConfigField("String", "VERSION_NAME", "\"1.16\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(libs.room.runtime)

    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)
    implementation(libs.timber)
    implementation(platform(libs.androidx.compose.bom))

    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)
}
