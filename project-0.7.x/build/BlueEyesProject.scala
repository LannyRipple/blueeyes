import sbt._
import netbeans.plugin._
import de.element34.sbteclipsify._

trait OneJar { this: DefaultProject =>
  lazy val oneJar = oneJarAction

  def oneJarAction = oneJarTask.dependsOn(`package`) describedAs("Creates a single JAR containing all dependencies that runs the project's mainClass")

  def oneJarTask: Task = task {
    import FileUtilities._
    import java.io.{ByteArrayInputStream, File}
    import java.util.jar.Manifest
    import org.apache.commons.io.FileUtils

    val manifest = new Manifest(new ByteArrayInputStream((
      "Manifest-Version: 1.0\n" +
      "Main-Class: " + mainClass.get + "\n").getBytes))

    val versionString = version match {
      case BasicVersion(major, _, _, _) => "-v" + major.toString

      case _ => version.toString
    }

    val allDependencies = jarPath +++ runClasspath +++ mainDependencies.scalaJars

    log.info("All dependencies of " + name + ": " + allDependencies)

    val destJar = (normalizedName + versionString + ".jar"): Path

    FileUtilities.withTemporaryDirectory(log) { tmpDir =>
      val tmpPath = Path.fromFile(tmpDir)

      allDependencies.get.foreach { dependency =>
        log.info("Unzipping " + dependency + " to " + tmpPath)

        if (dependency.ext.toLowerCase == "jar") {
          unzip(dependency, tmpPath, log)
        }
        else if (dependency.asFile.isDirectory) {
          FileUtils.copyDirectory(dependency.asFile, tmpDir)
        }
        else {
          copyFile(dependency.asFile, tmpDir, log)
        }
      }

      new File(tmpDir, "META-INF/MANIFEST.MF").delete

      log.info("Creating single jar out of all dependencies: " + destJar)

      jar(tmpDir.listFiles.map(Path.fromFile), destJar, manifest, true, log)

      None
    }
  }
}

class BlueEyesProject(info: ProjectInfo) extends DefaultProject(info) with Repositories with Eclipsify with IdeaProject with PublishingProject with GpgPlugin with ChecksumPlugin{
  val specs         = "org.scala-tools.testing"     %% "specs"              % "1.6.8"         % "provided"
  val scala_check   = "org.scala-tools.testing"     %% "scalacheck"         % "1.9"           % "provided"
  val mockito       = "org.mockito"                 % "mockito-all"         % "1.8.5"         % "provided"
  val javolution       = "javolution"                  % "javolution"                  % "5.5.1"
  val netty         = "org.jboss.netty"             % "netty"               % "3.2.4.Final"   % "compile"
  val mongo         = "org.mongodb"                 % "mongo-java-driver"   % "2.6.3"         % "compile"
  val joda_time     = "joda-time"                   % "joda-time"           % "1.6.2"         % "compile"
  val configgy      = "net.lag"                     % "configgy"            % "2.0.0"         % "compile" intransitive()
  val rhino         = "rhino"                       % "js"                  % "1.7R2"         % "compile"
  val akka_actor = "se.scalablesolutions.akka"   % "akka-actor"         % "1.1.2"
  val akka_typed_actor = "se.scalablesolutions.akka"   % "akka-typed-actor"   % "1.1.2"
  val xlightweb     = "org.xlightweb"               % "xlightweb"           % "2.13.2"        % "compile"
  val codec         = "commons-codec"               % "commons-codec"       % "1.5"           % "compile"
  val clhm_lru      = "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.1" % "compile"
  val scalaz_core   = "org.scalaz"                  %% "scalaz-core"        % "6.0.1"

  lazy val benchmark = benchmarkTask

  def benchmarkTask = task { args =>
    val duration = if (args.isEmpty) "600" else args(0)
    runTask(Some("blueeyes.benchmark.Benchmark"), runClasspath, Array(duration)) dependsOn(compile, copyResources) describedAs("Run benchmark test")
  }

  override def mainClass = Some("blueeyes.demo.BlueEyesDemo")

  override val ivyXML =
  <dependencies>
    <dependency org="se.scalablesolutions.akka" name="akka-actor" rev="1.1.2">
      <exclude module="configgy"/>
    </dependency>
  </dependencies>
}

trait Repositories {
  val scalareleases   = MavenRepository("Scala Repo Releases",        "http://scala-tools.org/repo-releases/")
  val scalasnapshots  = MavenRepository("Scala-tools.org Repository", "http://scala-tools.org/repo-snapshots/")
  val sonatyperelease = MavenRepository("Sonatype Releases",          "http://oss.sonatype.org/content/repositories/releases")
  val nexusscalatools = MavenRepository("Nexus Scala Tools",          "http://nexus.scala-tools.org/content/repositories/releases")
  val mavenrepo1      = MavenRepository("Maven Repo 1",               "http://repo1.maven.org/maven2/")
  val scalablerepo    = MavenRepository("Maven Repo 1",               "http://akka.io/repository/")
  val guiceyfruitrepo    = MavenRepository("Maven Repo 1",               "http://guiceyfruit.googlecode.com/svn/repo/releases/")
  val jbossreleases   = MavenRepository("JBoss Releases",             "http://repository.jboss.org/nexus/content/groups/public/")
}
