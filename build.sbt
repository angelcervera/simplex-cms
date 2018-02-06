
scalaVersion := "2.11.12"

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
    libraryDependencies ++= Seq( // compile scope dependencies
      "com.fasterxml.woodstox" %  "woodstox-core"        % "5.0.3",
      "org.scalactic"          %% "scalactic"            % scalatestVersion,
      "org.apache.velocity"    %  "velocity-engine-core" % velocityVersion,
      "org.apache.velocity"    %  "velocity-tools"       % velocityVersion,
      "com.typesafe"           %  "config"               % "1.3.2"
    ) ++ Seq( // Test scope dependencies
      "org.scalatest"          %% "scalatest"            % scalatestVersion,
      "com.github.pathikrit"   %% "better-files"         % "3.4.0"

    ).map(_ % Test)
  )


