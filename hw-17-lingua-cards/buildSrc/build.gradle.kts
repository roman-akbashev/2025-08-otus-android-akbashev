plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.apis:google-api-services-androidpublisher:v3-rev142-1.25.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.46.0")
}