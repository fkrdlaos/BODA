buildscript {

    val nav_version by extra("2.5.0")
    dependencies {
        classpath(libs.google.services)
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}