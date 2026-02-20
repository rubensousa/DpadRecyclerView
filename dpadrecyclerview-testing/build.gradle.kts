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
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.carioca.allure)
    alias(libs.plugins.maven.publish)
}

apply(from = "${rootProject.projectDir}/gradle/coverage.gradle")

val versions = rootProject.extra["versions"] as Map<String, Int>

android {
    namespace = "com.rubensousa.dpadrecyclerview.testing"
    compileSdk = versions["compileSdkVersion"]

    defaultConfig {
        minSdk = versions["minSdkVersion"]
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["useTestStorageService"] = "true"
    }

    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        getByName("release") {
        }
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        targetSdk = versions["targetSdkVersion"]
    }

}

dependencies {
    implementation(project(":dpadrecyclerview"))
    debugImplementation(libs.androidx.fragment.testing.manifest)
    api(libs.androidx.test.runner)
    api(libs.androidx.test.core.ktx)
    api(libs.androidx.test.rules)
    api(libs.androidx.test.truth)
    api(libs.androidx.test.junit)
    api(libs.androidx.test.espresso.core)
    api(libs.androidx.test.espresso.idling)
    api(libs.androidx.test.espresso.contrib)
    api(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.androidx.fragment.testing)
    androidTestImplementation(project(":dpadrecyclerview-test-fixtures"))
    androidTestUtil(libs.androidx.test.services)
}

allureReport {
    outputDir = rootProject.file("build/outputs/allure-results")
}
