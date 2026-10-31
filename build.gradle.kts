plugins {
    id("java-library")
}

group = "net.cfauto"
version = "1.0-SNAPSHOT"

val asm_version = project.findProperty("asm_version")
val jopt_simple_version = project.findProperty("jopt_simple_version")
val log4j_version = project.findProperty("log4j_version")

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    mavenCentral()
}

dependencies {
    api("org.ow2.asm:asm:${asm_version}")
    api("org.ow2.asm:asm-analysis:${asm_version}")
    api("org.ow2.asm:asm-commons:${asm_version}")
    api("org.ow2.asm:asm-tree:${asm_version}")
    api("org.ow2.asm:asm-util:${asm_version}")
    api("net.fabricmc:sponge-mixin:0.17.5+mixin.0.8.7")
    api("io.github.llamalad7:mixinextras-common:0.5.5")
    api("net.fabricmc:mapping-io:0.9.1")
    compileOnlyApi("org.jspecify:jspecify:1.0.1")

    api("net.sf.jopt-simple:jopt-simple:${jopt_simple_version}")

    api("org.apache.logging.log4j:log4j-core:${log4j_version}")
    api("org.apache.logging.log4j:log4j-api:${log4j_version}")
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class.java) {
    options.release = 8
}

