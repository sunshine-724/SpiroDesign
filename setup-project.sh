#!/bin/bash

echo "SpiroDesign プロジェクト構造を作成中..."

# ディレクトリ構造の作成
echo "ディレクトリ構造を作成中..."
mkdir -p shared/src/commonMain/kotlin/org/example/shared
mkdir -p shared/src/commonTest/kotlin/org/example/shared
mkdir -p shared/src/jvmMain/kotlin/org/example/shared
mkdir -p shared/src/jvmTest/kotlin/org/example/shared
mkdir -p shared/src/jsMain/kotlin/org/example/shared
mkdir -p shared/src/androidMain/kotlin/org/example/shared

mkdir -p core/src/main/java/org/example/core
mkdir -p core/src/main/kotlin/org/example/core
mkdir -p core/src/test/java/org/example/core
mkdir -p core/src/test/kotlin/org/example/core

mkdir -p backend/src/main/kotlin/org/example/backend
mkdir -p backend/src/main/resources
mkdir -p backend/src/test/kotlin/org/example/backend

mkdir -p web-frontend/src/main/kotlin/org/example/web
mkdir -p web-frontend/src/main/resources
mkdir -p web-frontend/src/test/kotlin/org/example/web

mkdir -p android/src/main/kotlin/org/example/spirodesign
mkdir -p android/src/main/res/layout
mkdir -p android/src/main/res/values
mkdir -p android/src/main/res/drawable
mkdir -p android/src/androidTest/kotlin/org/example/spirodesign
mkdir -p android/src/test/kotlin/org/example/spirodesign

mkdir -p docs
mkdir -p scripts

echo "ディレクトリ構造の作成完了"

# 既存のMain.javaをcoreモジュールに移動
if [ -f "src/main/java/org/example/Main.java" ]; then
    echo "既存のMain.javaをcoreモジュールに移動中..."
    mv src/main/java/org/example/Main.java core/src/main/java/org/example/
    echo "Main.javaの移動完了"
fi

# settings.gradleの更新
echo "settings.gradleを更新中..."
cat > settings.gradle << 'EOF'
rootProject.name = 'SpiroDesign'

include ':core'
include ':backend'
include ':web-frontend'
include ':android'
include ':shared'
EOF

# ルートbuild.gradleの更新
echo "ルートbuild.gradleを更新中..."
cat > build.gradle << 'EOF'
buildscript {
    ext.kotlin_version = '1.9.20'
    ext.compose_version = '1.5.4'
    ext.spring_boot_version = '3.1.5'
    
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "org.springframework.boot:spring-boot-gradle-plugin:$spring_boot_version"
        classpath 'com.android.tools.build:gradle:8.1.2'
    }
}

plugins {
    id 'org.jetbrains.kotlin.multiplatform' version '1.9.20' apply false
    id 'org.jetbrains.kotlin.jvm' version '1.9.20' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.20' apply false
    id 'org.jetbrains.kotlin.js' version '1.9.20' apply false
    id 'org.jetbrains.compose' version '1.5.4' apply false
    id 'org.jetbrains.kotlin.plugin.serialization' version '1.9.20' apply false
    id 'org.jetbrains.kotlin.plugin.spring' version '1.9.20' apply false
    id 'org.springframework.boot' version '3.1.5' apply false
    id 'io.spring.dependency-management' version '1.1.3' apply false
    id 'com.android.application' version '8.1.2' apply false
}

allprojects {
    group = 'org.example'
    version = '1.0-SNAPSHOT'
    
    repositories {
        google()
        mavenCentral()
        maven { url 'https://maven.pkg.jetbrains.space/public/p/compose/dev' }
    }
}

subprojects {
    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
EOF

# shared/build.gradleの作成
echo "shared/build.gradleを作成中..."
cat > shared/build.gradle << 'EOF'
plugins {
    id 'org.jetbrains.kotlin.multiplatform'
    id 'org.jetbrains.kotlin.plugin.serialization' version '1.9.20'
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = '17'
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    sourceSets {
        commonMain {
            dependencies {
                implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
                implementation 'org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0'
                implementation 'org.jetbrains.kotlinx:kotlinx-datetime:0.4.1'
            }
        }
        
        commonTest {
            dependencies {
                implementation 'org.jetbrains.kotlin:kotlin-test'
            }
        }
        
        jvmMain {
            dependencies {
                implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3'
            }
        }
        
        jvmTest {
            dependencies {
                implementation 'org.junit.jupiter:junit-jupiter:5.10.0'
            }
        }
        
        jsMain {
            dependencies {
                implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.7.3'
            }
        }
        
        androidMain {
            dependencies {
                implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
            }
        }
    }
}

android {
    namespace 'org.example.shared'
    compileSdk 34

    defaultConfig {
        minSdk 24
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
EOF

# core/build.gradleの作成
echo "core/build.gradleを作成中..."
cat > core/build.gradle << 'EOF'
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm'
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation project(':shared')
    
    // Java dependencies
    implementation 'org.apache.commons:commons-math3:3.6.1'
    implementation 'com.google.guava:guava:32.1.3-jre'
    
    // Kotlin dependencies
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
    
    // Logging
    implementation 'org.slf4j:slf4j-api:2.0.9'
    implementation 'ch.qos.logback:logback-classic:1.4.11'
    
    // Testing
    testImplementation platform('org.junit:junit-bom:5.10.0')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testImplementation "org.jetbrains.kotlin:kotlin-test-junit5:$kotlin_version"
    testImplementation 'org.mockito:mockito-core:5.6.0'
}

test {
    useJUnitPlatform()
}

compileKotlin {
    kotlinOptions {
        jvmTarget = "17"
    }
}

compileTestKotlin {
    kotlinOptions {
        jvmTarget = "17"
    }
}
EOF

# backend/build.gradleの作成
echo "backend/build.gradleを作成中..."
cat > backend/build.gradle << 'EOF'
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
    id 'org.jetbrains.kotlin.plugin.spring'
}

dependencies {
    implementation project(':core')
    implementation project(':shared')
    
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    
    // Kotlin
    implementation "org.jetbrains.kotlin:kotlin-reflect"
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    implementation 'com.fasterxml.jackson.module:jackson-module-kotlin'
    
    // Database
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'org.postgresql:postgresql'
    
    // API Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
    
    // Development
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

bootJar {
    archiveFileName = 'spiro-design-backend.jar'
}

tasks.named('test') {
    useJUnitPlatform()
}
EOF

# web-frontend/build.gradleの作成
echo "web-frontend/build.gradleを作成中..."
cat > web-frontend/build.gradle << 'EOF'
plugins {
    id 'org.jetbrains.kotlin.js'
    id 'org.jetbrains.compose'
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
                devServer = devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()
                devServer.port = 3000
            }
        }
    }
}

dependencies {
    implementation project(':shared')
    
    // Compose for Web
    implementation compose.web.core
    implementation compose.web.svg
    implementation compose.runtime
    
    // Kotlin coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.7.3'
    
    // HTTP client
    implementation 'io.ktor:ktor-client-core:2.3.5'
    implementation 'io.ktor:ktor-client-js:2.3.5'
    implementation 'io.ktor:ktor-client-content-negotiation:2.3.5'
    implementation 'io.ktor:ktor-serialization-kotlinx-json:2.3.5'
    
    // Testing
    testImplementation 'org.jetbrains.kotlin:kotlin-test-js'
}

// Configure webpack to serve static files
afterEvaluate {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        versions.webpackDevServer.version = "4.0.0"
    }
}
EOF

# android/build.gradleの作成
echo "android/build.gradleを作成中..."
cat > android/build.gradle << 'EOF'
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    namespace 'org.example.spirodesign'
    compileSdk 34

    defaultConfig {
        applicationId "org.example.spirodesign"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary true
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = '17'
    }
    
    buildFeatures {
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = '1.5.4'
    }
    
    packagingOptions {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
        }
    }
}

dependencies {
    implementation project(':shared')
    implementation project(':core')
    
    // Android Core
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.activity:activity-compose:1.8.1'
    
    // Compose BOM
    implementation platform('androidx.compose:compose-bom:2023.10.01')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    
    // Navigation
    implementation 'androidx.navigation:navigation-compose:2.7.4'
    
    // ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    
    // HTTP client
    implementation 'io.ktor:ktor-client-android:2.3.5'
    implementation 'io.ktor:ktor-client-content-negotiation:2.3.5'
    implementation 'io.ktor:ktor-serialization-kotlinx-json:2.3.5'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation platform('androidx.compose:compose-bom:2023.10.01')
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
}
EOF

# Android Manifestの作成
echo "AndroidManifest.xmlを作成中..."
cat > android/src/main/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SpiroDesign"
        tools:targetApi="31">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.SpiroDesign">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
EOF

# Androidリソースファイルの作成
echo "Androidリソースファイルを作成中..."
cat > android/src/main/res/values/strings.xml << 'EOF'
<resources>
    <string name="app_name">SpiroDesign</string>
</resources>
EOF

cat > android/src/main/res/values/themes.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.SpiroDesign" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
EOF

# ProGuard設定ファイルの作成
touch android/proguard-rules.pro

# 完了メッセージ
echo ""
echo "✅ SpiroDesignプロジェクト構造の作成が完了しました！"
echo ""
echo "作成されたモジュール:"
echo "  📦 shared    - Kotlin Multiplatform共通コード"
echo "  📦 core      - JavaとKotlinビジネスロジック"
echo "  📦 backend   - Spring Boot REST API"
echo "  📦 web-frontend - Compose for Web"
echo "  📦 android   - Androidアプリ"
echo ""
echo "次のステップ:"
echo "  1. ./gradlew build でビルドテスト"
echo "  2. ./gradlew :backend:bootRun でバックエンド起動"
echo "  3. ./gradlew :web-frontend:jsBrowserDevelopmentRun でWeb開発サーバー起動"
echo ""
echo "Happy coding! 🚀"
EOF