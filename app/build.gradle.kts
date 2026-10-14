plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

import java.util.Base64

// 固定 debug 签名钥匙：云构建每台机器随机生成 debug 钥匙，会导致各版本签名不一致、无法覆盖安装。
// 钥匙以 base64 存在仓库根目录 debug.keystore.b64，配置阶段解码后用于签名，保证所有版本签名一致。
val fixedDebugKeystore = run {
    val b64 = rootProject.file("debug.keystore.b64").readText().filterNot { it.isWhitespace() }
    val f = layout.buildDirectory.file("fixed-debug.keystore").get().asFile
    f.parentFile.mkdirs()
    f.writeBytes(Base64.getDecoder().decode(b64))
    f
}

android {
    namespace = "com.cyingshi.app"
    signingConfigs {
        // 注：不用 getByName("debug") { } 的 lambda 写法，Kotlin DSL 下有重载歧义会导致脚本编译失败
        val debugCfg = getByName("debug")
        debugCfg.storeFile = fixedDebugKeystore
        debugCfg.storePassword = "cyingshi123"
        debugCfg.keyAlias = "cyingshi"
        debugCfg.keyPassword = "cyingshi123"
    }
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cyingshi.app"
        minSdk = 21
        targetSdk = 34
        versionCode = 5
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

