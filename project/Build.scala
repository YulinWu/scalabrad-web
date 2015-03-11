import sbt._
import sbt.Keys._
import com.earldouglas.xwp.XwpPlugin

object ApplicationBuild extends Build {

  val gwtModules = taskKey[Seq[String]]("the list of gwt modules to compile")
  val gwtCompile = taskKey[Unit]("invoke the gwt compiler")
  val gwtDevMode = taskKey[Unit]("invoke gwt dev mode")
  val gwtCodeServer = taskKey[Unit]("invoke gwt code server")

  val gwtVersion = "2.7.0"
  val jettyVersion = "8.1.12.v20130726" // this version is embedded in gwt 2.7
  //val jettyVersion = "9.2.6.v20141205"

  val main = Project(
    id = "scalabrad-web-gwt",
    base = file("."),
    settings = Project.defaultSettings ++ XwpPlugin.jetty() ++ Seq(
      version := "1.0-SNAPSHOT",
      scalaVersion := "2.11.6",
      resolvers += "bintray" at "http://jcenter.bintray.com",
      resolvers += "bintray-maffoo" at "http://dl.bintray.com/maffoo/maven",
      libraryDependencies ++= Seq(
        "org.eclipse.jetty" % "jetty-continuation" % jettyVersion withSources(),
        "org.eclipse.jetty" % "jetty-server" % jettyVersion withSources(),
        "org.eclipse.jetty" % "jetty-util" % jettyVersion withSources(),
        "com.google.inject" % "guice" % "3.0" withSources() exclude("asm", "asm"),
        "com.google.inject.extensions" % "guice-servlet" % "3.0" withSources(),
        "com.google.gwt" % "gwt-codeserver" % gwtVersion withSources(),
        "com.google.gwt" % "gwt-dev" % gwtVersion withSources(),
        "com.google.gwt" % "gwt-elemental" % gwtVersion withSources(),
        "com.google.gwt" % "gwt-servlet" % gwtVersion withSources(),
        "com.google.gwt" % "gwt-user" % gwtVersion withSources(),
        "com.google.gwt.inject" % "gin" % "2.1.2" withSources(),
        "com.googlecode.gflot" % "gflot" % "3.3.0" withSources(),
        "org.labrad" %% "scalabrad" % "0.2.0-M3-9" withSources()
      ),
      gwtModules := Seq("org.labrad.browser.LabradBrowser"),
      gwtCompile := {
        val srcs = (sourceDirectories in Compile).value
        val classes = (fullClasspath in Compile).value.map(_.data)
        val cp = srcs ++ classes

        val warDir = (XwpPlugin.webappDest in XwpPlugin.webapp).value
        val args: Array[String] = Array(
          "-war", warDir.getAbsolutePath
        ) ++ gwtModules.value

        val forkOptions = ForkOptions(
          bootJars = Nil,
          javaHome = javaHome.value,
          connectInput = connectInput.value,
          outputStrategy = outputStrategy.value,
          runJVMOptions = javaOptions.value,
          workingDirectory = Some(target.value),
          envVars = envVars.value
        )
        val scalaRun = new ForkRun(forkOptions)
        scalaRun.run("com.google.gwt.dev.Compiler", cp, args, streams.value.log)
      },
      gwtDevMode := {
        val srcs = (sourceDirectories in Compile).value
        val classes = (fullClasspath in Compile).value.map(_.data)
        val cp = srcs ++ classes

        val warDir = (XwpPlugin.webappDest in XwpPlugin.webapp).value
        val args: Array[String] = Array(
          "-war", warDir.getAbsolutePath,
          "-port", "7667"
        ) ++ gwtModules.value

        val forkOptions = ForkOptions(
          bootJars = Nil,
          javaHome = javaHome.value,
          connectInput = connectInput.value,
          outputStrategy = outputStrategy.value,
          runJVMOptions = javaOptions.value,
          workingDirectory = Some(target.value),
          envVars = envVars.value
        )
        val scalaRun = new ForkRun(forkOptions)
        scalaRun.run("com.google.gwt.dev.DevMode", cp, args, streams.value.log)
      },
      gwtCodeServer := {
        val srcs = (sourceDirectories in Compile).value
        val classes = (fullClasspath in Compile).value.map(_.data)
        val cp = srcs ++ classes

        val args: Array[String] = Array(
          "-port", "9876"
        ) ++ gwtModules.value

        val forkOptions = ForkOptions(
          bootJars = Nil,
          javaHome = javaHome.value,
          connectInput = connectInput.value,
          outputStrategy = outputStrategy.value,
          runJVMOptions = javaOptions.value,
          workingDirectory = Some(target.value),
          envVars = envVars.value
        )
        val scalaRun = new ForkRun(forkOptions)
        scalaRun.run("com.google.gwt.dev.codeserver.CodeServer", cp, args, streams.value.log)
      },
      XwpPlugin.postProcess := { _ => gwtCompile.value }
    )
  )
}
