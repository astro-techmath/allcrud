plugins {
	java
	`maven-publish`
	`java-test-fixtures`
    signing
	id("io.spring.dependency-management") version "1.1.7"
    id("com.gradle.plugin-publish") version "1.3.0"
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

buildscript {
    dependencies {
        classpath("org.apache.httpcomponents.client5:httpclient5:5.3")
        classpath("org.apache.httpcomponents.core5:httpcore5:5.2.4")
    }
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
    description = "Create a bundle for Sonatype Central Portal"

    dependsOn("publishToMavenLocal")

    val bundleDir = layout.buildDirectory.dir("central-bundle")
    val bundleFile = layout.buildDirectory.file("allcrud-${version}-bundle.zip")

    outputs.file(bundleFile)

    doLast {
        val mavenRepo = file("${System.getProperty("user.home")}/.m2/repository/io/github/astro-techmath/allcrud/${version}")

        delete(bundleDir)
        copy {
            from(mavenRepo)
            into(bundleDir)
            include("allcrud-${version}.jar")
            include("allcrud-${version}.jar.asc")
            include("allcrud-${version}-sources.jar")
            include("allcrud-${version}-sources.jar.asc")
            include("allcrud-${version}-javadoc.jar")
            include("allcrud-${version}-javadoc.jar.asc")
            include("allcrud-${version}.pom")
            include("allcrud-${version}.pom.asc")
            include("allcrud-${version}.module")
            include("allcrud-${version}.module.asc")
        }

        ant.withGroovyBuilder {
            "zip"("destfile" to bundleFile.get().asFile) {
                "fileset"("dir" to bundleDir.get().asFile)
            }
        }

        println("✅ Bundle created successfully!")
        println("📦 Location: ${bundleFile.get().asFile.absolutePath}")
        println("📊 Size: ${bundleFile.get().asFile.length() / 1024} KB")
    }
}

tasks.register("publishToCentralPortal") {
    group = "publishing"
    description = "Publish to Sonatype Central Portal"

    dependsOn("createPublishingBundle")

    doLast {
        val bundleFile = layout.buildDirectory.file("allcrud-${version}-bundle.zip").get().asFile

        if (!bundleFile.exists()) {
            throw GradleException("Bundle file not found: ${bundleFile.absolutePath}")
        }

        val username = project.findProperty("sonatypeUsername") as String?
            ?: System.getenv("SONATYPE_USERNAME")
            ?: throw GradleException("sonatypeUsername not found")

        val password = project.findProperty("sonatypePassword") as String?
            ?: System.getenv("SONATYPE_PASSWORD")
            ?: throw GradleException("sonatypePassword not found")

        println("🚀 Uploading to Sonatype Central Portal...")
        println("📦 Bundle: ${bundleFile.name}")
        println("📊 Size: ${bundleFile.length() / 1024} KB")

        val curlCommand = listOf(
            "curl",
            "-X", "POST",
            "https://central.sonatype.com/api/v1/publisher/upload",
            "-H", "Authorization: Bearer ${username}:${password}",
            "-F", "bundle=@${bundleFile.absolutePath}",
            "-v"
        )

        val process = ProcessBuilder(curlCommand)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val error = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        println("\n📤 Upload Response:")
        println(output)

        if (exitCode == 0 && output.contains("\"state\":\"VALIDATED\"") || output.contains("\"state\":\"PUBLISHING\"")) {
            println("\n✅ SUCCESS! Publication uploaded to Central Portal!")
            println("🌐 Check status at: https://central.sonatype.com/publishing/deployments")
        } else {
            println("\n❌ Upload failed!")
            println("Error: $error")
            throw GradleException("Failed to upload to Central Portal")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
