/*
 * Copyright 2024 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply(from = "${rootProject.projectDir}/gradle/coverage.gradle")

val versions = rootProject.extra["versions"] as Map<String, Int>

android {
    namespace = "com.rubensousa.dpadrecyclerview.testfixtures"
    compileSdk = versions["compileSdkVersion"]

    defaultConfig {
        minSdk = versions["minSdkVersion"]
        targetSdk = versions["targetSdkVersion"]

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":dpadrecyclerview"))
    implementation(libs.carioca.report)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.collection)
    implementation(libs.junit)
    implementation(libs.truth)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.test.runner)
}

