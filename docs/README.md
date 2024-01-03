# sbt-rust

[![BSD 3 Clause License](https://img.shields.io/github/license/nigeleke/sbt-rust?style=plastic)](https://github.com/nigeleke/sbt-rust/blob/master/LICENSE)
[![Language](https://img.shields.io/badge/language-Scala-blue.svg?style=plastic)](https://www.scala-lang.org)
[![Build](https://img.shields.io/github/actions/workflow/status/nigeleke/sbt-rust/acceptance.yml?style=plastic)](https://github.com/nigeleke/sbt-rust/actions/workflows/acceptance.yml)
![Version](https://img.shields.io/github/v/tag/nigeleke/sbt-rust?style=plastic)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=plastic&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

  [Site](https://nigeleke.github.io/sbt-rust) \| [GitHub](https://github.com/nigeleke/sbt-rust) \| [API](https://nigeleke.github.io/sbt-rust/api/index.html)

An sbt plugin to integrate Rust development within Scala & SBT projects. This is useful for [Rust](https://www.rust-lang.org) projects
built with [Cargo](https://doc.rust-lang.org/cargo/) or [Yew](https://yew.rs/) projects built with [Trunk](https://trunkrs.dev/).

## Commands

The plugin attempts to use the `trunk` package manager by default. This default can be overridden by
setting `Rust / tooling := CargoPackageManager`, in which case `cargo` will then be used.

| Command            | CargoPackageManager       | TrunkPackageManager   | sbt Command |
| ------------------ | ------------------------- | --------------------- | ----------- |
| rustClean          | cargo clean [1]           | trunk clean           | clean       |
| rustCargoClean [2] | cargo clean [1]           | cargo clean [1]       |             |
| rustBuild          | cargo build [3]           | trunk  build          | compile     |
| rustTest           | cargo test                | cargo test            | test        |
| rustRun            | cargo run [4]             | trunk serve [5]       | run         |
| rustRelease        | cargo build [6] --release | trunk build --release |             |
| rustPackage        | cargo package             | cargo package         | package     |
| rustConfig         | cargo config get [7]      | trunk config show     |             |
| rustDoc            | cargo doc                 | cargo doc             | doc         |

    [1] Rust / cleanOptions
    [2] Forces cargo clean when using trunk package manager (trunk clean deletes dist folder, but not target folder contents)
    [3] Rust / debugOptions
    [4] Rust / runOptions
    [5] Waits for server to exit or ctrl-c from the user. The ctrl-c will abort the server process.
    [6] Rust / releaseOptions
    [7] Requires nightly build (as stands in April 2023)

## Motivation

The plugin is intended for use in a multi-project `build.sbt` file, where the frontend project is developed using [Yew](https://yew.rs/).
This enables the Yew project to be developed while remaining within the Scala sbt infrastructure.

The plugin really only "makes sense" for multi-project environments. If the user only has a single `cargo` / `trunk`
project they may as well use the tool directly. However a `build.sbt` could be created as a wrapper and this
plugin used if desired. The expected folder structure for this is demonstrated in the sbt-test scripted tests.

## Usage

### Pre-requisites

[Rust](https://www.rust-lang.org), [Cargo](https://doc.rust-lang.org/cargo/), [Yew](https://yew.rs/) and / or
[Trunk](https://trunkrs.dev/) must be installed independently (as required) and their binaries accessible on
the current PATH.

The plugin is not currently published in the Maven Repository (or similar), however it is easy enough to build and publish locally, as shown below:

```bash
git clone https://github.com/nigeleke/sbt-rust.git
sbt publishLocal
```

### plugins.sbt

`addSbtPlugin("nigeleke" % "sbt-rust" % "<version>")`

### build.sbt

```sbt
val scala3Version = "3.m.n"

lazy val root = project
  .in(file("."))
  .settings(
    name           := "myproject"
  )
  .aggregate(core, ui)

lazy val core = project
  .in(file("core"))
  .settings(
    name           := "myproject-core",
    scalaVersion   := scala3Version,
    ...
  )

lazy val ui = project
  .in(file("ui"))
  .enablePlugins(RustPlugin)
  .settings(
    name := "myproject-ui",
    Rust / tooling             := TrunkPackageManager, // optional, default. Allowed CargoPackageManager or TrunkPackageManager
    Rust / debugOptions   := "",   // optional, default
    Rust / releaseOptions := "",   // optional, default
    Rust / runOptions     := "",   // optional, default
    ...
  )
```

The folder layout and files within the `ui` folder are expected to match the `cargo new ui` or `trunk new ui`
layout. As such, the `cargo` or `trunk` tooling can continue to be used independently. (Consideration was given
to forcing the standard using `src/main/rust` folder structure but this was rejected as being too opinionated, and
not inline with the defaults of the underlying `cargo` and `trunk` tooling)

## Plugin Development

This plugin requires sbt 1.0.0+

### Testing

Run `test` for regular unit tests.

Run `scripted` for [sbt script tests](http://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html).

## Acknowledgements

1. [ChatGPT](https://chat.openai.com/) examples, which in turn were derived from:

   a. [https://www.scala-sbt.org/1.x/docs/Writing-Plugins.html](https://www.scala-sbt.org/1.x/docs/Writing-Plugins.html)
   
   b. 404 - https://github.com/lightbend/activator-rust/blob/master/src/main/scala/RustPlugin.scala

   c. 404 - https://github.com/jeffreyolchovy/sbt-rustup/blob/master/src/main/scala/sbtrustup/RustupPlugin.scala

2. [Pritam Kadam - Write & test your own scala SBT plugin…](https://medium.com/@phkadam2008/write-test-your-own-scala-sbt-plugin-6701b0e36a62)

3. [Stackoverflow: Running custom sbt plugin task for one project in a multi-project build.sbt](https://stackoverflow.com/questions/75973944/running-custom-sbt-plugin-task-for-one-project-in-a-multi-project-build-sbt)

4. [Stackoverflow: How can I use sbt commands like clean and compile in my custom sbt plugin](https://stackoverflow.com/questions/76002618/how-can-i-use-sbt-commands-like-clean-and-compile-in-my-custom-sbtplugin)
