version      := "0.1"
scalaVersion := "2.12.1"

import nigeleke.sbt.RustPlugin.*

lazy val root = (project in file("."))
  .enablePlugins(RustPlugin)
  .settings(
    name                  := "test",
    Rust / cleanOptions   := "--target-dir target-trunk-01", // rustCargoClean
    Rust / debugOptions   := "--target-dir target-trunk-01", // n/a for TrunkPackageManager
    Rust / releaseOptions := "--target-dir target-trunk-01", // n/a for TrunkPackageManager
    Rust / runOptions     := "--target-dir target-trunk-01", // n/a for TrunkPackageManager
    Rust / docOptions     := "--target-dir target-trunk-01", // rustDoc
    run                   := {}
  )
