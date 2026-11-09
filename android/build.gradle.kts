// Kotlin 2.4 metadata requires R8 >= 9.1.29. Pin the standalone stable R8
// while keeping the established AGP 8.13 build model.
buildscript {
    repositories { google(); mavenCentral() }
    dependencies { classpath("com.android.tools:r8:9.5.29") }
}
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.4.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}
