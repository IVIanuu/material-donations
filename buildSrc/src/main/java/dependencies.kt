@file:Suppress("ClassName", "unused")

object Versions {
    // android
    const val compileSdk = 28
    const val minSdk = 17
    const val minSdkSample = 21
    const val targetSdk = 28
    const val versionCode = 1
    const val versionName = "1.0"

    const val androidGradlePlugin = "3.1.3"

    const val billingX = "master"

    const val constraintLayout = "1.1.2"

    const val epoxy = "2.12.0"

    const val kotlin = "1.2.41"

    const val mavenGradle = "2.1"
    const val materialDialogs = "0.9.6.0"

    const val playBilling = "1.1"

    const val support = "28.0.0-alpha3"
}

object Deps {
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"

    const val billingX = "com.github.pixiteapps:billingx:${Versions.billingX}"

    const val constraintLayout = "com.android.support.constraint:constraint-layout:${Versions.constraintLayout}"

    const val epoxy = "com.airbnb.android:epoxy:${Versions.epoxy}"

    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jre7:${Versions.kotlin}"

    const val mavenGradlePlugin = "com.github.dcendents:android-maven-gradle-plugin:${Versions.mavenGradle}"

    const val materialDialogs = "com.afollestad.material-dialogs:core:${Versions.materialDialogs}"

    const val playBilling = "com.android.billingclient:billing:${Versions.playBilling}"

    const val supportAppCompat = "com.android.support:appcompat-v7:${Versions.support}"
}