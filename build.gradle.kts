import com.vanniktech.maven.publish.SonatypeHost

plugins {
	java
	`java-test-fixtures`
	jacoco
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "7.4.0.8496"
	// Pinned to 0.28.0, NOT the latest (0.37.0) - confirmed empirically, not assumed: 0.35.0+
	// raised the minimum Gradle version to 8.13, but even on exactly 8.13 (and 8.14.5) its
	// java-test-fixtures workaround throws NoSuchMethodError on the internal Gradle class
	// ProjectDerivedCapability (constructor signature mismatch between what 0.37.0 was compiled
	// against and what this project's Gradle wrapper actually ships - see gradle-wrapper.properties,
	// still on 8.7). 0.28.0 is the version that added Central Portal support in the first place
	// (see SonatypeHost.CENTRAL_PORTAL below) while still targeting Gradle 8.1-8.7, matching this
	// project's wrapper. Revisit this pin only alongside a deliberate Gradle version bump.
	id("com.vanniktech.maven.publish") version "0.28.0"
}

group = "io.github.astro-techmath"
version = "0.2.0"

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

mavenPublishing {
    coordinates("io.github.astro-techmath", "allcrud", version.toString())

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

    // 0.28.0's no-arg publishToMavenCentral() defaults to SonatypeHost.DEFAULT (the old OSSRH
    // host, shut down since June 2025) - Central Portal only became the default in later
    // versions incompatible with this project's Gradle version (see the version pin above).
    // Must be passed explicitly here.
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
