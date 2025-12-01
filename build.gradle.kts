plugins {
	java
	`maven-publish`
	`java-test-fixtures`
    signing
	id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.astro-techmath"
version = "0.1.0-beta"

java {
	sourceCompatibility = JavaVersion.VERSION_21
	withSourcesJar()
	withJavadocJar()
}

configurations {
	create("testFiles")
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

val springBootVersion = "3.5.6"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
    }
}

val commonsCollections = "4.5.0"
val commonsLang = "3.18.0"
val restAssuredVersion = "5.5.6"
val jacksonDatatypeJsrVersion = "2.20.0"
val instancioVersion = "5.4.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonDatatypeJsrVersion")
	implementation("org.apache.commons:commons-collections4:$commonsCollections")
	implementation("org.apache.commons:commons-lang3:$commonsLang")
	implementation("io.rest-assured:spring-mock-mvc:$restAssuredVersion")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	compileOnly("org.projectlombok:lombok")

	testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-rest")
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-web")
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-validation")
    testFixturesImplementation("org.springframework.boot:spring-boot-testcontainers")
    testFixturesImplementation("org.testcontainers:postgresql")
	testFixturesImplementation("io.rest-assured:spring-mock-mvc:$restAssuredVersion")
	testFixturesImplementation("org.instancio:instancio-junit:$instancioVersion")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register<Jar>("testArchive") {
	description = "Registration for unit tests"
	group = JavaBasePlugin.VERIFICATION_GROUP
	archiveBaseName.set("allcrud")
	from(project.the<SourceSetContainer>()["test"].output)
}

tasks.javadoc {
	if (JavaVersion.current().isJava11Compatible) {
		(options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
	}
}

tasks.withType<Javadoc> {
	options {
		encoding = "UTF-8"
		memberLevel = JavadocMemberLevel.PUBLIC
		title = "Allcrud API Documentation"
	}
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

artifacts {
	add("testFiles", tasks["testArchive"])
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.astro-techmath"
            artifactId = "allcrud"
            version = "0.1.0-beta"

            from(components["java"])

            pom {
                name.set("Allcrud")
                description.set("Generic CRUD library for Spring Boot REST APIs")
                url.set("https://github.com/astro-techmath/allcrud")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("mathmferreira")
                        name.set("Matheus de Almeida Maia Ferreira")
                        email.set("mathmferreira@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/astro-techmath/allcrud.git")
                    developerConnection.set("scm:git:ssh://github.com/astro-techmath/allcrud.git")
                    url.set("https://github.com/astro-techmath/allcrud")
                }
            }
        }
    }

    repositories {
        maven {
            name = "CentralPortal"
            url = uri("https://central.sonatype.com/api/v1/publisher/upload/")
            credentials {
                username = project.findProperty("sonatypeUsername") as String? ?: System.getenv("SONATYPE_USERNAME")
                password = project.findProperty("sonatypePassword") as String? ?: System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey = project.findProperty("signing.keyId") as String? ?: System.getenv("SIGNING_KEY_ID")
    val signingPassword = project.findProperty("signing.password") as String? ?: System.getenv("SIGNING_PASSWORD")
    val signingSecretKey = project.findProperty("signing.secretKeyRingFile") as String? ?: System.getenv("SIGNING_SECRET_KEY_RING_FILE")

    if (signingKey != null && signingPassword != null && signingSecretKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }

    sign(publishing.publications["maven"])
}
