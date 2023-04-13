version      := "0.1"
scalaVersion := "2.12.1"

lazy val root = (project in file("."))
  .enablePlugins(RustPlugin)
  .settings(
    name := "test"
  )
