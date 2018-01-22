
scalaVersion := "2.11.12"

// Library versions
lazy val scalatestVersion    = "3.0.4"

// Modules
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.simplexportal.cms",
      scalaVersion    := "2.12.4"
    )),
    name := "Simplex CMS",
    libraryDependencies ++= Seq( // compile scope dependencies
      "com.fasterxml.woodstox" % "woodstox-core" % "5.0.3",
      "org.scalactic" %% "scalactic"            % scalatestVersion
    ) ++ Seq( // Test scope dependencies
      "org.scalatest"     %% "scalatest"            % scalatestVersion,
      "com.github.pathikrit" %% "better-files" % "3.4.0"
    ).map(_ % Test)
  )


