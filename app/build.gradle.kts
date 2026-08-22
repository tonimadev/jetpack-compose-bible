import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.spotless)
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

android {
    namespace = "digital.tonima.bibliadigital"
    compileSdk = 37

    defaultConfig {
        applicationId = "digital.tonima.bibliadigital"
        minSdk = 23
        targetSdk = 37
        versionCode = 16
        versionName = "1.16"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("boolean", "DEBUG", "false")
            buildConfigField("String", "BIBLIA_DIGITAL_TOKEN", "\"$bibliaDigitalToken\"")
        }
        debug {
            buildConfigField("boolean", "DEBUG", "true")
            buildConfigField("String", "BIBLIA_DIGITAL_TOKEN", "\"$bibliaDigitalToken\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=com.google.accompanist.navigation.animation.ExperimentalAnimationApi",
        )
    }
}

dependencies {
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    // Hilt
    implementation(libs.hilt.android)
    // Misc
    implementation(libs.timber)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    // Retrofit & OkHttp
    implementation(libs.retrofit)
    // Room
    implementation(libs.room.runtime)
    // Serialization & Gson
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.gson)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.room.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))
    implementation(project(":feature:bible:bridge"))
    implementation(project(":feature:bible:impl"))

    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)

    // Test
    testImplementation(libs.junit)
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

apply(from = "../spotless.gradle")
