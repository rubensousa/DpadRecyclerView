plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kover) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.androidx.navigation.safeargs) apply false
    alias(libs.plugins.kotlin.dokka) apply true
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.1"
}

val versions = mapOf(
    "minSdkVersion" to 21,
    "targetSdkVersion" to 36,
    "compileSdkVersion" to 36
)

extra.set("versions", versions)

apiValidation {
    ignoredProjects.addAll(listOf("sample", "dpadrecyclerview-test-fixtures"))
    ignoredClasses.add("com.rubensousa.dpadrecyclerview.BuildConfig")
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

dependencies {
    dokka(project(":dpadrecyclerview:"))
    dokka(project(":dpadrecyclerview-compose:"))
    dokka(project(":dpadrecyclerview-testing:"))
}
