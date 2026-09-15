import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun versionCodeDate(): String {
    val fmt = SimpleDateFormat("yyyyMMddHH").apply { timeZone = TimeZone.getTimeZone("GMT+8") }
    return fmt.format(Date())
}

fun versionNameDate(): String {
    val fmt = SimpleDateFormat("yyyyMMdd.HH").apply { timeZone = TimeZone.getTimeZone("GMT+8") }
    return fmt.format(Date())
}

android {
    compileSdk = 37
    namespace = "com.yuyan"
    defaultConfig {
        applicationId = "com.yuyan.pinyin"
        minSdk = 23
        targetSdk = 37
        versionCode = versionCodeDate().toInt()
        versionName = versionNameDate()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf("AROUTER_MODULE_NAME" to project.name))
            }
        }

        lint {
            abortOnError = false
        }
    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore/keystore.properties")
            val keystoreProperties = Properties().apply {
                if (keystorePropertiesFile.exists()) {
                    keystorePropertiesFile.inputStream().use { load(it) }
                }
            }
            val keystoreFilePath = System.getenv("RELEASE_STORE_FILE") ?: keystoreProperties.getProperty("storeFile")
            val keystorePassword = System.getenv("RELEASE_STORE_PASSWORD") ?: keystoreProperties.getProperty("storePassword")
            val keyAliasName = System.getenv("RELEASE_KEY_ALIAS") ?: keystoreProperties.getProperty("keyAlias")
            val keyPasswordValue = System.getenv("RELEASE_KEY_PASSWORD") ?: keystoreProperties.getProperty("keyPassword")
            if (keystoreFilePath != null) {
                val keystoreFile = rootProject.file(keystoreFilePath)
                if (keystoreFile.exists() && keystorePassword != null && keyAliasName != null && keyPasswordValue != null) {
                    storeFile = keystoreFile
                    storePassword = keystorePassword
                    keyAlias = keyAliasName
                    keyPassword = keyPasswordValue
                    isV1SigningEnabled = true
                    isV2SigningEnabled = true
                }
            }
        }
    }

    flavorDimensions += "default"
    productFlavors {
        create("offline") {
            dimension = "default"
            applicationIdSuffix = ".offline"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            applicationIdSuffix = ".release"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard.cfg")
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":yuyansdk"))
}