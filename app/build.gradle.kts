import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    jacoco
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

jacoco { toolVersion = "0.8.14" }

tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val clasesDebug = layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
val generados = listOf("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/*_Impl*.class")
val ambitoLogica = listOf(
    "**/data/model/**", "**/data/repository/**",
    "**/ui/screens/**/*ViewModel*", "**/ui/screens/**/*UiState*",
    "**/ui/screens/**/CampoFormulario*", "**/ui/screens/**/Formulario*"
)

tasks.register<JacocoReport>("coberturaDebug") {
    group = "verification"
    description = "Genera la cobertura unitaria de todo el código Android de producción."
    dependsOn("testDebugUnitTest")
    executionData(layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"))
    classDirectories.setFrom(fileTree(clasesDebug) { exclude(generados) })
    sourceDirectories.setFrom(files("src/main/java"))
    reports { xml.required.set(true); html.required.set(true) }
}

tasks.register<JacocoReport>("coberturaLogicaDebug") {
    group = "verification"
    description = "Genera la cobertura de todos los ViewModels, estados, modelos y repositorios."
    dependsOn("testDebugUnitTest")
    executionData(layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"))
    classDirectories.setFrom(fileTree(clasesDebug) {
        include(ambitoLogica)
        exclude(generados)
    })
    sourceDirectories.setFrom(files("src/main/java"))
    reports { xml.required.set(true); html.required.set(true) }
}

tasks.register<JacocoCoverageVerification>("verificarCoberturaLogicaDebug") {
    group = "verification"
    description = "Exige al menos 80 % de líneas y ramas en el conjunto de lógica."
    dependsOn("coberturaLogicaDebug")
    executionData(layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"))
    classDirectories.setFrom(fileTree(clasesDebug) { include(ambitoLogica); exclude(generados) })
    violationRules {
        rule {
            limit { counter = "LINE"; minimum = "0.80".toBigDecimal() }
            limit { counter = "BRANCH"; minimum = "0.80".toBigDecimal() }
        }
    }
}

android {
    namespace = "com.example.museodelosmuseos"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.museodelosmuseos"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.osmdroid.android)
    implementation(libs.play.services.code.scanner)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.coil.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    testImplementation(libs.junit)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
