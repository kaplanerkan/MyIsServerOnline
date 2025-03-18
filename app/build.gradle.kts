plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.erkankaplan.isserveronline"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.erkankaplan.isserveronline"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }


    buildFeatures {
        viewBinding = true
    }

}


tasks.withType<JavaCompile> {
     options.compilerArgs.add("-Xlint:unchecked")
     options.compilerArgs.add("-Werror")
      options.compilerArgs.add("-Xlint:deprecation")

}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.toasty)
}