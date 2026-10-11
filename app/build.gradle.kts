plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.android.compose.screenshot")
}

val releaseKeystoreFile = providers.environmentVariable("BUDGET_KEYSTORE_FILE").orNull
val releaseKeystorePassword = providers.environmentVariable("BUDGET_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("BUDGET_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("BUDGET_KEY_PASSWORD").orNull
val releaseSigningConfigured = listOf(
    releaseKeystoreFile,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "com.hmagr.budgetapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hmagr.budgetapp"
        minSdk = 30
        targetSdk = 36
        versionCode = providers.environmentVariable("BUDGET_VERSION_CODE").orNull?.toIntOrNull() ?: 1
        versionName = "0.1.0"
    }

    if (releaseSigningConfigured) {
        signingConfigs {
            create("budgetRelease") {
                storeFile = file(requireNotNull(releaseKeystoreFile))
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("budgetRelease")
            }
        }
    }

    buildFeatures {
        compose = true
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.09.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    screenshotTestImplementation("com.android.tools.screenshot:screenshot-validation-api:0.0.1-alpha16")
    testImplementation("junit:junit:4.13.2")
}
