plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "br.com.despensa"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.com.despensa"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "2.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    // Room schema exportado para versionamento (NFR-002)
    ksp { arg("room.schemaLocation", "$projectDir/schemas") }
    // Testes usam JUnit5 (jupiter): sem isso, testDebugUnitTest roda 0 testes.
    testOptions { unitTests.all { it.useJUnitPlatform() } }
    sourceSets { getByName("androidTest").assets.srcDirs("$projectDir/schemas") }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    debugImplementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.compose.icons)
    implementation(libs.compose.icons.extended)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.coroutines.core)
    implementation(libs.serialization.json)
    implementation(libs.work.runtime)
    implementation(libs.datastore.preferences)
    ksp(libs.room.compiler)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit5.api)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.3")
    // #4: teste de migração instrumentado (compila aqui; roda em aparelho via connectedDebugAndroidTest).
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.test.junit)
}
