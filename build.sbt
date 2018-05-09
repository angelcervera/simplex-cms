
// Library versions
lazy val scalatestVersion  = "3.0.4"
lazy val velocityVersion   = "2.0"


// Modules
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.simplexportal.cms",
      scalaVersion    := "2.12.4"
    )),
    name := "Simplex CMS",
    libraryDependencies ++=
      Seq( // compile scope dependencies
      "com.fasterxml.woodstox"       %  "woodstox-core"             % "5.0.3",
//      "org.apache.velocity"          %  "velocity-engine-core"      % velocityVersion,
//      "org.apache.velocity"          %  "velocity-tools"            % velocityVersion,
      "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.9.4",
      "org.json4s"                   %% "json4s-native"             % "3.5.3",
      "org.json4s"                   %% "json4s-jackson"            % "3.5.3",
      "org.json4s"                   %% "json4s-ext"                % "3.5.3",
      "com.typesafe"                 %  "config"                    % "1.3.2",
      "org.scala-lang.modules"       %% "scala-xml"                 % "1.1.0",
      "com.github.pathikrit"         %% "better-files"              % "3.4.0",
      "com.iheart"                   %% "ficus"                     % "1.4.3",
      "com.typesafe.scala-logging"   %% "scala-logging"             % "3.8.0",
      "ch.qos.logback"                % "logback-classic"           % "1.2.3",
      "com.typesafe.akka"            %% "akka-http"                 % "10.1.0",
      "com.typesafe.akka"            %% "akka-stream"               % "2.5.11",
      "com.vladsch.flexmark"          % "flexmark-all"              % "0.32.18"
    ).map(_ % Compile) ++
    Seq( // Test scope dependencies
      "org.scalatest"                %% "scalatest"                 % scalatestVersion,
      "org.scalactic"                %% "scalactic"                 % scalatestVersion,
      "org.scalamock"                %% "scalamock"                 % "4.1.0"
    ).map(_ % Test)
  )


