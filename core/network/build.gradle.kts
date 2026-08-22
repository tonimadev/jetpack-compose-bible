import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
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
    namespace = "digital.tonima.bibliadigital.core.network"
    compileSdk = 37

    defaultConfig {
        minSdk = 23

        buildConfigField("String", "BIBLIA_DIGITAL_TOKEN", "\"$bibliaDigitalToken\"")
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
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(project(":core:common"))
    implementation(project(":core:database"))

    ksp(libs.hilt.compiler)
}
