plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun versionCodeDate(): String =
    java.text.SimpleDateFormat("yyyyMMddHH", java.util.TimeZone.getTimeZone("GMT+8")).format(java.util.Date())

fun versionNameDate(): String =
    java.text.SimpleDateFormat("yyyyMMdd.HH", java.util.TimeZone.getTimeZone("GMT+8")).format(java.util.Date())

android {
    compileSdk = 36
    namespace = "com.yuyan"
    defaultConfig {
        applicationId = "com.yuyan.pinyin"
        minSdk = 23
        targetSdk = 36
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
            val keystoreProperties = java.util.Properties().apply {
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

    applicationVariants.configureEach {
        outputs.configureEach {
            outputFileName = "yuyanIme_${versionCodeDate()}_${this@configureEach.buildType.name}.apk"
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":yuyansdk"))
}