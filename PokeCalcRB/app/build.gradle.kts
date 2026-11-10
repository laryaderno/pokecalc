plugins {
    id("com.android.application")
}

android {
    namespace = "br.ufms.pokecalcrb"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.ufms.pokecalcrb"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    // Interface
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.fragment:fragment:1.8.5")

    // Splash Screen API (sem Thread.sleep)
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")

    // Room (banco local)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // HTTP: Retrofit + Gson (PokéAPI)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Carregamento de imagens (sprites e foto de perfil)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Hash de senha com salt
    implementation("at.favre.lib:bcrypt:0.10.2")

    // Testes de unidade da fórmula (Java puro)
    testImplementation("junit:junit:4.13.2")
}
