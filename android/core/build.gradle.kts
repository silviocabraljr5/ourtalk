plugins { `java-library` }
java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
tasks.register<JavaExec>("verifyCore") { dependsOn(tasks.testClasses); classpath = sourceSets["test"].runtimeClasspath; mainClass.set("br.com.ourtalk.core.CoreChecks") }
