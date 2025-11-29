plugins {
	java
	`maven-publish`
	`java-test-fixtures`
	id("io.spring.dependency-management") version "1.1.7"
    signing
}

group = "com.techmath"
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

val commonsCollections = "4.5.0"
val commonsLang = "3.18.0"
val restAssuredVersion = "5.5.2"
val jacksonDatatypeJsrVersion = "2.20.0"
val instancioVersion = "5.4.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
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
	testFixturesImplementation("io.rest-assured:spring-mock-mvc:$restAssuredVersion")
	testFixturesImplementation("org.instancio:instancio-junit:$instancioVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.6")
    }
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

artifacts {
	add("testFiles", tasks["testArchive"])
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.techmath"
            artifactId = "allcrud"
            version = "0.1.0-beta"

            from(components["java"])

            pom {
                name.set("Allcrud")
                description.set("Generic CRUD library for Spring Boot REST APIs")
                url.set("https://github.com/mathmferreira/allcrud")

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
                    connection.set("scm:git:git://github.com/mathmferreira/allcrud.git")
                    developerConnection.set("scm:git:ssh://github.com/mathmferreira/allcrud.git")
                    url.set("https://github.com/mathmferreira/allcrud")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}
