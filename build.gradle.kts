// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false   // Hilt usa KAPT
    alias(libs.plugins.ksp) apply false           // Room usa KSP
    alias(libs.plugins.hilt) apply false
}

