# sbt-rust

[![BSD 3 Clause License](https://img.shields.io/github/license/nigeleke/sbt-rust?style=plastic)](https://github.com/nigeleke/sbt-rust/blob/master/LICENSE)
[![Language](https://img.shields.io/badge/language-Scala-blue.svg?style=plastic)](https://www.scala-lang.org)
[![Build](https://img.shields.io/github/actions/workflow/status/nigeleke/sbt-rust/acceptance.yml?style=plastic)](https://github.com/nigeleke/sbt-rust/actions/workflows/acceptance.yml)
![Version](https://img.shields.io/github/v/tag/nigeleke/sbt-rust?style=plastic)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=plastic&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

An sbt plugin to integrate Rust development within Scala & SBT projects. This is useful for [Rust](https://www.rust-lang.org) projects
built with [Cargo](https://doc.rust-lang.org/cargo/) or [Yew](https://yew.rs/) projects built with [Trunk](https://trunkrs.dev/).

The plugin detects `Cargo.toml`or `Trunk.toml` files in the project's root directory. If found, it will run the equivalent
`cargo` or `trunk` command. If both `.toml` files then `trunk` is invoked by default. This default can be overridden by
setting `wasmBuild / rust := false`, in which case `cargo` will be the default.

| Command     | Cargo                 | Trunk                 | sbt Command |
|-------------|-----------------------|-----------------------|-------------|
| rustClean   | cargo clean           | trunk clean           | clean       |
| rustBuild   | cargo build           | trunk build           | compile     |
| rustTest    | cargo test            | trunk test            | test        |
| rustRun     | cargo run             | trunk serve           | run         |
| rustRelease | cargo build --release | trunk build --release |             |
| rustPackage | cargo package         | trunk package         | package     |
| rustConfig  | cargo config get [1]  | trunk config show     |             |
| rustDoc     | cargo doc             | trunk doc             | doc         |

[1] As at April 2023 requires nightly build.

## Motivation

An multi-project `build.sbt` file, where the frontend project is developed using [Yew](https://yew.rs/).
This enables the yew project to be developed while remaining within the Scala sbt infrastructure.

The plugin only "makes sense" for multi-project environments. If the user only has a single `cargo` / `trunk`
project they may as well use the tool directly. However a `build.sbt` could be created as a wrapper and this
plugin used if desired.

## Usage

### Pre-requisites

[Rust](https://www.rust-lang.org), [Cargo](https://doc.rust-lang.org/cargo/), [Yew](https://yew.rs/) and / or
[Trunk](https://trunkrs.dev/) must be installed independently (as required) and their binaries accessible on
the current PATH.

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
    name        := "myproject-ui",
    wasmBuild   := true,
    ...
  )
```

The folder layout and files within the `ui` folder are expected to match the `cargo new ui` or `trunk new ui`
layout. As such, the `cargo` or `trunk` tooling can continue to be used independently. (Consideration was given
to forcing the standard `src/main/rust` folder structure but this was rejected as being too opinionated)

## Plugin Development

This plugin requires sbt 1.0.0+

### Testing

Run `test` for regular unit tests.

Run `scripted` for [sbt script tests](http://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html).

### CI

The generated project uses [sbt-github-actions](https://github.com/djspiewak/sbt-github-actions) as a plugin
to generate workflows for GitHub actions. For full details of how to use it [read this](https://github.com/djspiewak/sbt-github-actions/blob/main/README.md)

### Publishing

1. Publish your source to GitHub
2. Follow the instructions in [sbt-ci-release](https://github.com/olafurpg/sbt-ci-release/blob/main/readme.md) to create a sonatype account and setup your keys
3. `sbt ci-release`
4. [Add your plugin to the community plugins list](https://github.com/sbt/website#attention-plugin-authors)
5. [Claim your project in Scaladex](https://github.com/scalacenter/scaladex-contrib#claim-your-project)

## Acknowledgements

1. [ChatGPT](https://chat.openai.com/) examples, which in turn were derived from:

   a. [https://www.scala-sbt.org/1.x/docs/Writing-Plugins.html](https://www.scala-sbt.org/1.x/docs/Writing-Plugins.html)
   
   b. 404 - https://github.com/lightbend/activator-rust/blob/master/src/main/scala/RustPlugin.scala

   c. 404 - https://github.com/jeffreyolchovy/sbt-rustup/blob/master/src/main/scala/sbtrustup/RustupPlugin.scala

2. [Pritam Kadam - Write & test your own scala SBT plugin…](https://medium.com/@phkadam2008/write-test-your-own-scala-sbt-plugin-6701b0e36a62)
