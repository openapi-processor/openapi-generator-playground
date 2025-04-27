plugins {
    java
    groovy
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.ben-manes.versions") version "0.52.0"

    id("org.openapi.generator") version "7.12.0"
    id("io.openapiprocessor.openapi-processor") version "2023.2"
}

group = "io.openapiprocessor"
version = "1.0.0"

repositories {
    mavenCentral ()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.codehaus.groovy:groovy:3.0.24")

    // extra
    implementation("joda-time:joda-time:2.14.0")

    // required by openapi-generator
    implementation("io.swagger.core.v3:swagger-annotations:2.2.30")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    testImplementation ("org.springframework.boot:spring-boot-starter-test") {
//        exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
}

tasks.test {
    useJUnitPlatform ()
}

sourceSets {
    main {
        java {
            srcDir("build/openapi-generator")
            srcDir("build/openapi-processor")
        }
    }
}

/*
 * sample api path => folder name
 */

//def ACTIVE_API = "mapping-additional-properties"
//def ACTIVE_API = "array-mapping"
//def ACTIVE_API = "param"
//def ACTIVE_API = "records"
val ACTIVE_API = "train-travel"

/*
 * openapi-generator
 */

openApiGenerate {
//    verbose = true
    generatorName = "spring"
    inputSpec = "$rootDir/src/api/$ACTIVE_API/openapi.yaml".toString()
    outputDir = "$buildDir/openapi-generator".toString()
    apiPackage = "io.openapiprocessor.oag.api"
    modelPackage = "io.openapiprocessor.oag.model"
    importMappings.set(mapOf(
       "Year" to "java.time.Year",
//       "Instant": "java.time.Instant",
       "DateTime" to "org.joda.time.JodaTime"
    ))
    typeMappings.set(mapOf(
//        "integer+year": "Year",
        "integer+date-time" to "DateTime",
//        "DateTimeSchema": "DateTime"
    ))
//    schemaMappings.set([
//            "DateTime": "java.time.Instant",
//        "DateTimeSchema": "org.joda.time.DateTime"
//    ])
    configOptions.set(mapOf(
        "sourceFolder" to "",
        "interfaceOnly" to "true",
        "dateLibrary" to "java8",
        "useBeanValidation" to "true",
        "useSpringBoot3" to "true"
//        annotationLibrary: "none"  / errors
//        hideGenerationTimestamp: "true",
//        skipDefaultInterface: "true"
    ))
}

tasks.compileJava {
    dependsOn("openApiGenerate")
}

/*
 * openapi-processor
 */

openapiProcessor {
    apiPath("${projectDir}/src/api/${ACTIVE_API}/openapi.yaml")

    process("spring") {
        processor("io.openapiprocessor:openapi-processor-spring:2025.2")
        targetDir("$buildDir/openapi-processor")

        prop("mapping", "${projectDir}/src/api/${ACTIVE_API}/mapping.yaml")
    }

//    checkUpdates true
}

tasks.compileJava {
    dependsOn("processSpring")
}
