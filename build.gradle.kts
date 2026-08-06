plugins {
	java
	`maven-publish`
	`java-test-fixtures`
    signing
	jacoco
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "7.4.0.8496"
}

group = "io.github.astro-techmath"
version = "0.1.0-beta"

jacoco {
    toolVersion = "0.8.15"
}

sonar {
    properties {
        property("sonar.projectKey", "astro-techmath_allcrud")
        property("sonar.organization", "astro-techmath")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        // These 3 are genuinely abstract extension points (CrudController/CrudService are pure
        // delegation to Spring MVC/JPA with zero decision logic of their own; the concrete type
        // parameters and any real behavior only exist once a CONSUMER writes a concrete subclass
        // - AbstractGlobalExceptionHandler is the same shape, its @ExceptionHandler methods only
        // do something once wired into a real Spring MVC exception-handling flow). They're only
        // ever exercised via consumers' own test suites (e.g. allcrud-generator's external smoke
        // tests, which extend them with real entities/controllers) - never inside this repo. A
        // fake subclass here just to move the coverage number would test nothing real (no logic
        // of this class's own would be exercised, only Spring's plumbing) - confirmed by reading
        // each class, not assumed from the JaCoCo report alone.
        property("sonar.coverage.exclusions", "src/main/java/com/techmath/allcrud/controller/CrudController.java," +
                "src/main/java/com/techmath/allcrud/service/CrudService.java," +
                "src/main/java/com/techmath/allcrud/exception/handler/AbstractGlobalExceptionHandler.java")
    }
}

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
	// Avoids redeclaring the same dependency under multiple configurations (implementation +
	// testFixturesImplementation + testImplementation) just to make it visible in each scope -
	// each dependency below is now declared exactly once, in its lowest/most specific scope.
	named("testFixturesImplementation") {
		extendsFrom(configurations.implementation.get())
	}
	named("testImplementation") {
		extendsFrom(configurations.getByName("testFixturesImplementation"))
	}
}

repositories {
	mavenCentral()
}

val springBootVersion = "4.1.0"

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
    }
}

val commonsCollections = "4.5.0"
val commonsLang = "3.18.0"
val restAssuredVersion = "6.0.0"
val instancioVersion = "5.4.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("org.apache.commons:commons-collections4:$commonsCollections")
	implementation("org.apache.commons:commons-lang3:$commonsLang")
	implementation("io.rest-assured:spring-mock-mvc:$restAssuredVersion")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	compileOnly("org.projectlombok:lombok")

	testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-rest")
    testFixturesImplementation("org.springframework.boot:spring-boot-testcontainers")
    testFixturesImplementation("org.testcontainers:testcontainers-postgresql")
	testFixturesImplementation("org.instancio:instancio-junit:$instancioVersion")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
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
            suppressPomMetadataWarningsFor("testFixturesApiElements")
            suppressPomMetadataWarningsFor("testFixturesRuntimeElements")

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

tasks.register("createPublishingBundle") {
    group = "publishing"
    description = "Create a bundle ZIP for Sonatype Central Portal"

    dependsOn("publishToMavenLocal")

    val bundleFile = layout.buildDirectory.file("allcrud-${version}-bundle.zip")

    outputs.file(bundleFile)

    doLast {
        val mavenRepo = file("${System.getProperty("user.home")}/.m2/repository/io/github/astro-techmath/allcrud/${version}")
        val tempDir = layout.buildDirectory.dir("central-bundle-temp").get().asFile

        delete(tempDir)

        val targetDir = File(tempDir, "io/github/astro-techmath/allcrud/${version}")
        targetDir.mkdirs()

        val filesToCopy = listOf(
            "allcrud-${version}.jar",
            "allcrud-${version}.jar.asc",
            "allcrud-${version}-sources.jar",
            "allcrud-${version}-sources.jar.asc",
            "allcrud-${version}-javadoc.jar",
            "allcrud-${version}-javadoc.jar.asc",
            "allcrud-${version}-test-fixtures.jar",
            "allcrud-${version}-test-fixtures.jar.asc",
            "allcrud-${version}.pom",
            "allcrud-${version}.pom.asc",
            "allcrud-${version}.module",
            "allcrud-${version}.module.asc"
        )

        filesToCopy.forEach { fileName ->
            val sourceFile = File(mavenRepo, fileName)
            if (sourceFile.exists()) {
                val targetFile = File(targetDir, fileName)
                sourceFile.copyTo(targetFile, overwrite = true)

                ant.withGroovyBuilder {
                    "checksum"("file" to targetFile, "algorithm" to "MD5", "fileext" to ".md5")
                }

                ant.withGroovyBuilder {
                    "checksum"("file" to targetFile, "algorithm" to "SHA-1", "fileext" to ".sha1")
                }
            }
        }

        ant.withGroovyBuilder {
            "zip"("destfile" to bundleFile.get().asFile) {
                "fileset"("dir" to tempDir) {
                    "include"("name" to "**/*")
                }
            }
        }

        println("\nBundle created successfully!")
        println("Location: ${bundleFile.get().asFile.absolutePath}")
        println("Size: ${bundleFile.get().asFile.length() / 1024} KB")
        println("\nFiles included:")
        targetDir.listFiles()?.sorted()?.forEach { println("   - ${it.name}") }
    }
}

tasks.register("publishToCentralPortal") {
    group = "publishing"
    description = "Create bundle and show upload instructions"

    dependsOn("createPublishingBundle")

    doLast {
        val bundleFile = layout.buildDirectory.file("allcrud-${version}-bundle.zip").get().asFile

        println("\n" + "=".repeat(70))
        println("READY TO PUBLISH TO MAVEN CENTRAL!")
        println("=".repeat(70))
        println("\nBundle file: ${bundleFile.absolutePath}")
        println("Size: ${bundleFile.length() / 1024} KB")
        println("\nUPLOAD OPTIONS:")
        println("\nManual Upload (Recommended):")
        println("- Go to: https://central.sonatype.com/publishing")
        println("- Click 'Upload Component'")
        println("- Select the bundle ZIP file")
        println("- Wait for validation")
        println("- Click 'Publish'")

        val username = project.findProperty("sonatypeUsername") as String? ?: "YOUR_USERNAME"
        val password = project.findProperty("sonatypePassword") as String? ?: "YOUR_PASSWORD"

        println("\nCommand Line Upload:")
        println("\n   curl -X POST \\")
        println("     https://central.sonatype.com/api/v1/publisher/upload \\")
        println("     -H \"Authorization: Bearer ${username}:${password}\" \\")
        println("     -F \"bundle=@${bundleFile.absolutePath}\"")

        println("\n" + "=".repeat(70))
        println("\nAfter upload, check status at:")
        println("   https://central.sonatype.com/publishing/deployments")
        println("\n" + "=".repeat(70) + "\n")
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
