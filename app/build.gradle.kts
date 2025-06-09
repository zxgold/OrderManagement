//import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.manager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.manager"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/gradle/incremental.annotation.processors"
        }
    }
}

// **恢复 kapt { ... } 配置块**
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    correctErrorTypes = true // 对于 Kapt 通常建议开启
}

dependencies {
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Core & Lifecycle & Activity & Compose BOM & UI Essentials
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx) // 主要是 collectAsStateWithLifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose) // 主要是 hiltViewModel (虽然它来自 hilt-navigation-compose) 和 viewModel()
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // **重要：Compose BOM 管理许多 Compose 相关库的版本**
    // 以下通常由 BOM 管理，可以直接使用，无需在 libs.versions.toml 中单独指定版本 (除非BOM版本太旧)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics) // BOM 通常包含
    implementation(libs.androidx.ui.tooling.preview) // BOM 通常包含
    implementation(libs.androidx.material3) // BOM 通常包含
    // implementation(libs.androidx.foundation) // Foundation 通常是 ui 的一部分或由 BOM 引入
    // implementation(libs.androidx.ui.text) // 通常也是 ui 的一部分或由 BOM 引入
    // implementation(libs.androidx.compose.runtime.livedata) // 仅当明确使用 LiveData 转 State 时需要

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Compose 测试也用 BOM
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    // implementation(libs.androidx.datastore.core) // 通常由 datastore-preferences 传递依赖

    // Navigation
    implementation(libs.androidx.navigation.compose)
    // implementation(libs.androidx.navigation.fragment.ktx) // 如果不用 Fragment Navigation，移除

    // Material Icons
    implementation(libs.androidx.compose.material.icons.extended) // 通常这个就够了，它会依赖 core

    // Room数据库
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler) // **核心修正：Room Compiler 用 ksp**

    // Lifecycle (ViewModel KTX and LiveData KTX) - 这些通常被其他库传递依赖
    // 可以尝试注释掉，如果编译通过则说明它们是传递依赖的，可以移除以保持简洁
    // implementation(libs.androidx.lifecycle.livedata.ktx)
    // implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}