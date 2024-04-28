plugins {
	java
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
//	jacoco
}

group = "com.astro"
version = "1.0.0"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

//jacoco {
//	toolVersion = "0.8.12"
//	reportsDirectory = layout.buildDirectory.dir("reports/coverage")
//}

configurations {
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
}

//tasks.check {
//	dependsOn(tasks.jacocoTestCoverageVerification)
//}

tasks.wrapper {
	gradleVersion = "8.7"
	distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<Test> {
	useJUnitPlatform()
//	finalizedBy(tasks.jacocoTestReport)
}
//
//val excludeTesting = arrayOf(
//	"**/AllcrudApplication**",
//	"**/allcrud/common/**",
//	"**/allcrud/entity/**",
//	"**/allcrud/exception/**",
//	"**/allcrud/repository/**",
//	"**/allcrud/enums/**",
//	"**/allcrud/util/**",
//)
//
//tasks.withType<JacocoReport> {
//	afterEvaluate {
//		classDirectories.setFrom(files(classDirectories.files.map {
//			fileTree(it).apply {
//				exclude(excludeTesting.toList())
//			}
//		}))
//	}
//}
//
//tasks.jacocoTestReport {
//	reports {
//		xml.required = false
//		csv.required = false
//		html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
//	}
//}
//
//tasks.jacocoTestCoverageVerification {
//	violationRules {
//		rule {
//			limit {
//				counter = "LINE"
//				value = "COVEREDRATIO"
//				minimum = "0.80".toBigDecimal()
//			}
//		}
//	}
//}
