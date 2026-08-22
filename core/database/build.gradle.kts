plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "digital.tonima.bibliadigital.core.database"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(project(":core:common"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)

    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)
}
