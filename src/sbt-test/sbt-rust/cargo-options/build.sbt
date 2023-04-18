version      := "0.1"
scalaVersion := "2.12.1"

import nigeleke.sbt.RustPlugin.*

lazy val root = (project in file("."))
  .enablePlugins(RustPlugin)
  .settings(
    name                  := "test",
    Rust / tooling        := CargoPackageManager,
    Rust / cleanOptions   := "--target-dir target-cargo-01",
    Rust / debugOptions   := "--target-dir target-cargo-01",
    Rust / releaseOptions := "--target-dir target-cargo-01",
    Rust / runOptions     := "--target-dir target-cargo-01",
    Rust / docOptions     := "--target-dir target-cargo-01",
    run                   := {}
  )
