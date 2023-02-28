import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import java.util.*

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.vanniktech.maven.publish.base")
    id("com.gradle.plugin-publish") version "1.0.0"
}
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localPropsFile.reader()
        .use { Properties().apply { load(it) } }
        .onEach { (name, value) -> ext[name.toString()] = value.toString() }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}
fun getExtraString(name: String) = ext[name]?.toString()

fun Project.plainJavadocJar(): TaskProvider<*> {
    return tasks.register("simpleJavadocJar", com.vanniktech.maven.publish.tasks.JavadocJar::class.java) {
        val task = tasks.named("javadoc")
        this.dependsOn(task)
        this.from(task)
    }
}

publishing {
    repositories {
        // 发布到本地
        mavenLocal {
            name = "local"
            url = uri("${project.buildDir}/repo")
        }
        maven {
            name = "release"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }
    publications.withType<MavenPublication> {
        if (this.name == "pluginMaven") {
            artifactId = "complete-kotlin"
            artifact(plainJavadocJar())
        }
    }
}

version = file("version.txt").useLines { it.first() }
group = "io.github.wjf510"

gradlePlugin {
    // Define the plugin
    plugins.create("complete-kotlin") {
        id = "${group}.complete-kotlin"
        displayName = "CompleteKotlin"
        description = "Enable auto-completion and symbol resolution for all Kotlin/Native platforms."
        implementationClass = "com.louiscad.complete_kotlin.CompleteKotlinPlugin"
    }
}

pluginBundle {
    website = ""
    vcsUrl = ""
    tags = listOf("kotlin", "kotlin-multiplatform", "kmm", "plugins")
}

repositories { mavenCentral() }

dependencies {
    implementation(gradleKotlinDsl())
    testImplementation(Kotlin.test.junit5)
}

tasks.withType<KotlinJvmCompile>().configureEach {
    kotlinOptions.apiVersion = "1.4"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}
mavenPublishing {
    signAllPublications()
    pomFromGradleProperties()
}
