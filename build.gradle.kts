plugins {
	java
	`maven-publish`
	`java-test-fixtures`
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
}

group = "com.astro"
version = "1.0.0"

java {
	sourceCompatibility = JavaVersion.VERSION_21
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

val commonsCollections = "4.4"
val commonsLang = "3.14.0"
val unloggedVersion = "0.4.5"
val restAssuredVersion = "5.4.0"
val jacksonDatatypeJsrVersion = "2.17.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
	implementation("org.springframework.boot:spring-boot-starter-jersey")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonDatatypeJsrVersion")
	implementation("org.apache.commons:commons-collections4:$commonsCollections")
	implementation("org.apache.commons:commons-lang3:$commonsLang")
	implementation("video.bug:unlogged-sdk:$unloggedVersion")
	implementation("io.rest-assured:rest-assured:$restAssuredVersion")
	implementation("io.rest-assured:spring-mock-mvc:$restAssuredVersion")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("video.bug:unlogged-sdk:$unloggedVersion")
	annotationProcessor("org.projectlombok:lombok")
	compileOnly("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")

	testFixturesImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
	testFixturesImplementation("io.rest-assured:spring-mock-mvc:$restAssuredVersion")
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-rest")
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-web")
	testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
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

artifacts {
	add("testFiles", tasks["testArchive"])
}

publishing {
	publications {
		create<MavenPublication>(project.name) {
			artifactId = project.name.lowercase().replace(":", "-")
			from(components["java"])
		}
	}
}

