
plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.2.21"
}

group = "com.ontimehealth"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("com.github.librepdf:openpdf:1.3.30")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("com.mercadopago:sdk-java:2.1.27")

	constraints {
		implementation("commons-beanutils:commons-beanutils:1.11.0") {
			because("Corrige CVE-2025-48734 (transitiva de mercadopago:sdk-java)")
		}
		implementation("org.codehaus.plexus:plexus-utils:4.0.2") {
			because("Corrige CVE-2025-67030 (transitiva de mercadopago:sdk-java)")
		}
		implementation("org.iq80.snappy:snappy:0.5") {
			because("Corrige CVE-2024-36124 (transitiva de mercadopago:sdk-java)")
		}
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
