import com.beust.kobalt.*
import com.beust.kobalt.api.Project
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.maven.DependencyManager
import com.beust.kobalt.plugin.packaging.assemble
import java.io.File
import java.nio.charset.Charset

val repos = repos("https://dl.bintray.com/kmruiz/maven",
        "http://repository.jetbrains.com/all",
        "https://dl.bintray.com/cbeust/maven/")

val plugins = plugins("com.beust.kobalt:kobalt-line-count:")

@Task(name = "createVersion", reverseDependsOn = arrayOf("compile", "test", "assemble"), runAfter = arrayOf("clean"))
fun taskCreateVersion(project: Project) : TaskResult {

    val gitLog = File(".git/logs/HEAD").readLines(Charset.forName("UTF-8"))
    val gitHash = gitLog[gitLog.lastIndex].split(" ")[1]

    val path = "com/guatec/internal"
    with(arrayListOf<String>()) {
        File("src/main/resources/$path/VersionTemplate.txt").forEachLine {
            add(it.replace("@version@", project.version!!).replace("@gitHash@", gitHash))
        }
        File("src/generated/java/$path/Version.java").writeText(joinToString("\n"))
    }
    return TaskResult()
}

val p = project {
    name = "mixed-example"
    group = "com.guatec"
    artifactId = name
    version = "1.1.2"

    sourceDirectories {
        path("src/main/java", "src/generated/java")
    }

    dependencies {
        compile(file("lib/jrobin-1.5.9.jar"))
        compile("log4j:log4j:1.2.17")
        compile("org.apache.commons:commons-lang3:3.4")
        compile("com.h2database:h2:jar:1.4.192")
        //compile("org.flywaydb:flyway-core:4.0")
        compile(file("lib/flyway-core-0-SNAPSHOT.jar"))
        compile("com.squareup.retrofit:converter-jackson:")
    }

    dependenciesTest {
        compile("org.testng:testng:6.9.5")
    }

    test {
        val quasarDep = DependencyManager.create("co.paralleluniverse:quasar-core:0.7.5")
        jvmArgs("-javaagent:${quasarDep.jarFile.get()}", "-Dco.paralleluniverse.fibers.verifyInstrumentation")
    }

    assemble {
        jar {
            fatJar=true
        }
    }
}
