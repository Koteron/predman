plugins {
	java
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.liquibase.gradle") version "2.2.0"
	id("com.google.protobuf") version "0.9.4"
}

liquibase {
	activities.register("main") {
		arguments = mapOf(
			"changelogFile" to "src/main/resources/db/changelog-master.yaml",
			"url" to "jdbc:postgresql://localhost:5430/predman-db",
			"username" to "admin",
			"password" to "admin",
			"logLevel" to "info"
		)
	}
}

group = "com.predman"
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
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
	implementation("org.liquibase:liquibase-core")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("io.jsonwebtoken:jjwt:0.12.6")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("org.springframework.boot:spring-boot-starter-security")

	implementation("jakarta.validation:jakarta.validation-api:3.0.2")
	implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
	implementation("org.glassfish:jakarta.el:5.0.0-M1")

	implementation("com.google.protobuf:protobuf-java-util:3.23.0")
	implementation("io.grpc:grpc-protobuf:1.58.0")
	implementation("io.grpc:grpc-stub:1.58.0")
	implementation("com.google.protobuf:protobuf-java:3.23.0")
	implementation("io.grpc:grpc-netty-shaded:1.58.0")
	compileOnly("org.apache.tomcat:annotations-api:6.0.53")

	liquibaseRuntime("org.liquibase:liquibase-core:4.23.1")
	liquibaseRuntime("org.postgresql:postgresql:42.7.2")
	liquibaseRuntime("info.picocli:picocli:4.7.5")
	liquibaseRuntime("ch.qos.logback:logback-classic:1.4.12")
	liquibaseRuntime(sourceSets.main.get().runtimeClasspath)
}

tasks.withType<Test> {
	useJUnitPlatform()
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:3.25.3"
	}
	plugins {
		create("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:1.63.0"
		}
	}
	generateProtoTasks {
		all().forEach {
			it.plugins {
				create("grpc")
			}
		}
	}
}

sourceSets {
	main {
		java {
			srcDirs("build/generated/source/proto/main/java")
			srcDirs("build/generated/source/proto/main/grpc")
		}
	}
}

tasks.compileJava {
	dependsOn("generateProto")
}