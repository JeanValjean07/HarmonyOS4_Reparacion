plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.android.gms.oss-licenses-plugin")

}

android {
    namespace = "com.suming.cpa"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.suming.cpa"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        ndk{
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86_64")
        }
    }

    buildTypes {

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

        }
    }

    packagingOptions{
        jniLibs{
            useLegacyPackaging = true
        }
        dex{
            useLegacyPackaging = true
        }
        resources{
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("fonts/**")
        }
    }



    /*
    aaptOptions {
        cruncherEnabled = false
        useNewCruncher = false
        // 启用WebP压缩
        additionalParameters(
            "--auto-convert-bitmaps", "--enable-webp"
        )
    }*/

    /*
    splits {
        abi {
            enable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            universalApk = false
        }
    }*/









    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }


    kotlinOptions {
       jvmTarget = "21"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

}




    dependencies {
        implementation("com.google.android.gms:play-services-oss-licenses:17.2.2")
        implementation(platform("androidx.compose:compose-bom:2023.10.01"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.material:material")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.activity:activity-compose:1.8.0")
        implementation("androidx.core:core-ktx:1.12.0")

        implementation("androidx.constraintlayout:constraintlayout:2.1.4")


        // Compose
        implementation("androidx.compose.foundation:foundation:1.8.3")
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")
        implementation("androidx.compose.material3:material3:1.2.0")


        // Compose 测试库
        //androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.8.3")


        // AndroidX
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.ui.android)
        implementation(libs.androidx.material3.android)

        // Testing
        //testImplementation(libs.junit)
        //androidTestImplementation(libs.androidx.junit)


        // AndroidX Material3
        //implementation(libs.androidx.material3)
        implementation(libs.androidx.compose.material)
        implementation(libs.androidx.ui)
        implementation(libs.androidx.runtime)
        //implementation(libs.androidx.animation)
        implementation(libs.androidx.foundation.layout)
        //implementation(libs.androidx.compose.testing)
    }

