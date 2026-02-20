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
    namespace = "com.rubensousa.dpadrecyclerview"
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
        buildConfig = true
    }

    testOptions {
        targetSdk = versions["targetSdkVersion"]
        unitTests.isReturnDefaultValues = true
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
    api(libs.androidx.recyclerview)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.fragment)

    // Required for dependency resolution
    debugImplementation(libs.guava)
    debugImplementation(libs.androidx.fragment.testing.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(project(":dpadrecyclerview-test-fixtures"))

    androidTestImplementation(libs.androidx.fragment.testing)
    androidTestImplementation(libs.carioca.report)
    androidTestImplementation(libs.carioca.rules)
    androidTestImplementation(project(":dpadrecyclerview-testing"))
    androidTestImplementation(project(":dpadrecyclerview-test-fixtures"))
    androidTestUtil(libs.androidx.test.services)
}

allureReport {
    outputDir = rootProject.file("build/outputs/allure-results")
}

