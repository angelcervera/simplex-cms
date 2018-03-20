
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
      "com.typesafe"                 %  "config"                    % "1.3.2",
      "org.scala-lang.modules"       %% "scala-xml"                 % "1.1.0",
      "com.github.pathikrit"         %% "better-files"              % "3.4.0",
      "com.iheart"                   %% "ficus"                     % "1.4.3"
    ).map(_ % Compile) ++
    Seq( // Test scope dependencies
      "org.scalatest"                %% "scalatest"                 % scalatestVersion,
      "org.scalactic"                %% "scalactic"                 % scalatestVersion
    ).map(_ % Test)
  )


