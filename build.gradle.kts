
plugins {
    java
    id("io.freefair.lombok") version "5.1.0"
    id("org.springframework.boot") version "2.3.1.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
}

group = "com.bt"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    jcenter()
}

val log4Version="2.13.3"
val junitVersion="5.6.2"
val assertjVersion="3.16.1"

dependencies {
//    implementation(platform("org.apache.logging.log4j:log4j-bom:$log4Version"))
//    implementation("org.apache.logging.log4j:log4j-core")
//    implementation("org.apache.logging.log4j:log4j-api")
//    implementation("org.apache.logging.log4j:log4j-slf4j-impl")
    //implementation("org.mongodb:mongodb-driver-sync:$mongodbVersion")
//    implementation("org.springframework.boot:spring-boot-starter") {
//        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
//    }
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-log4j2")


    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
//
//configure<JavaPluginConvention> {
//    sourceCompatibility = JavaVersion.VERSION_11
//}