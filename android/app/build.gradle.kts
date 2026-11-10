plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
android {
    namespace = "br.com.ourtalk.app"
    compileSdk = 36
    defaultConfig { applicationId = "br.com.ourtalk.app"; minSdk = 26; targetSdk = 36; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"; versionCode = 4; versionName = "0.4.0" }
    buildFeatures { compose = true; buildConfig = true }
    buildTypes { getByName("debug") { applicationIdSuffix = ".dev"; versionNameSuffix = "-dev" }; getByName("release") { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
kotlin { jvmToolchain(17) }
dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    implementation(project(":core"))
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-nearby:19.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation(platform("androidx.compose:compose-bom:2025.10.01"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
