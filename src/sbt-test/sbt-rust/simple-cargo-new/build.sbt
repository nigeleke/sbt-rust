version      := "0.1"
scalaVersion := "2.12.1"

import nigeleke.sbt.RustPlugin.*

lazy val root = (project in file("."))
  .settings(
    name           := "",
    Rust / tooling := CargoPackageManager
  )
