/*
 * Copyright (c) 2023, Nigel Eke
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package nigeleke.sbt

import sbt.*
import Keys.*

import scala.language.postfixOps
import scala.sys.process.*

object RustPlugin extends AutoPlugin {

  sealed trait PackageManager
  case object CargoPackageManager extends PackageManager
  case object TrunkPackageManager extends PackageManager

  object autoImport {
    val tooling             = settingKey[PackageManager]("Cargo or Trunk. Default TrunkPackageManager")
    val cargoDebugOptions   = settingKey[String]("Additional options for 'cargo build'")
    val cargoReleaseOptions = settingKey[String]("Additional options for 'cargo build --release'")
    val trunkDebugOptions   = settingKey[String]("Additional options for 'trunk build'")
    val trunkReleaseOptions = settingKey[String]("Additional options for 'trunk build --release'")
    val rustClean           = taskKey[Unit]("Clean using cargo clean or trunk clean.")
    val rustCargoClean      = taskKey[Unit]("Force cargo clean, when trunk clean is not enough.")
    val rustBuild           = taskKey[Unit]("Compile code using cargo build or trunk build.")
    val rustTest            = taskKey[Unit]("Run tests using cargo test or trunk test.")
    val rustRun             = taskKey[Unit]("Run program using cargo run or trunk serve.")
    val rustRelease         = taskKey[Unit]("Compile code with --release using cargo build or trunk build.")
    val rustPackage         = taskKey[Unit]("Package program using cargo package or trunk package.")
    val rustConfig          = taskKey[Unit]("Show the current configuration.")
    val rustDoc             = taskKey[Unit]("Generate documentation using cargo doc or trunk doc.")
  }

  import autoImport._

  override def trigger = allRequirements

  private def execCommand(
      cargoCommand: String,
      trunkCommand: String = ""
  )(
      tooling: PackageManager
  ) = {
    val command = tooling match {
      case CargoPackageManager => cargoCommand
      case TrunkPackageManager => if (trunkCommand.isBlank) cargoCommand else trunkCommand
    }
    command !
  }

  lazy val Rust = config("rust")

  override lazy val projectSettings = {
    inConfig(Rust)(
      Seq(
        tooling             := TrunkPackageManager,
        cargoDebugOptions   := "",
        cargoReleaseOptions := "",
        trunkDebugOptions   := "",
        trunkReleaseOptions := ""
      )
    ) ++ Seq(
      rustClean      := execCommand("cargo clean", "trunk clean")((Rust / tooling).value),
      rustCargoClean := execCommand("cargo clean", "cargo clean")((Rust / tooling).value),
      rustBuild      := execCommand(
        s"cargo ${(Rust / cargoDebugOptions).value} build",
        s"trunk ${(Rust / trunkDebugOptions).value} build"
      )((Rust / tooling).value),
      rustTest       := execCommand("cargo test", "cargo test")((Rust / tooling).value),
      rustRun        := execCommand("cargo run", "trunk serve")((Rust / tooling).value),
      rustRelease    := execCommand(
        s"cargo ${(Rust / cargoReleaseOptions).value} build --release",
        s"trunk ${(Rust / trunkReleaseOptions).value} build --release"
      )((Rust / tooling).value),
      rustPackage    := execCommand("cargo package")((Rust / tooling).value),
      rustConfig     := execCommand("cargo config get", "trunk config show")((Rust / tooling).value),
      rustDoc        := execCommand("cargo doc")((Rust / tooling).value),
      clean          := ((ThisBuild / clean) dependsOn rustClean).value,
      compile        := ((Compile / compile) dependsOn rustBuild).value,
      test           := ((Test / test) dependsOn rustTest).value,
      run            := ((ThisBuild / run) dependsOn rustRun),
      Keys.`package` := ((Compile / Keys.`package`) dependsOn rustPackage).value,
      doc            := ((Compile / doc) dependsOn rustDoc).value
    )
  }

}
