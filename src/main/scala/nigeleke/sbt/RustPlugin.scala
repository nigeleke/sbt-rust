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
    val Rust           = config("rust")
    val tooling        = settingKey[PackageManager]("Cargo or Trunk. Default TrunkPackageManager")
    val debugOptions   = settingKey[String]("Options for 'cargo' or 'trunk build'")
    val releaseOptions = settingKey[String]("Options for 'cargo' or 'trunk build --release'")
    val runOptions     = settingKey[String]("Options to pass to 'cargo run' or 'trunk serve'")
    val rustClean      = taskKey[Unit]("Clean using cargo clean or trunk clean.")
    val rustCargoClean = taskKey[Unit]("Force cargo clean, when trunk clean is not enough.")
    val rustBuild      = taskKey[Unit]("Compile code using cargo build or trunk build.")
    val rustTest       = taskKey[Unit]("Run tests using cargo test or trunk test.")
    val rustRun        = taskKey[Unit]("Run program using cargo run or trunk serve.")
    val rustRelease    = taskKey[Unit]("Compile code with --release using cargo build or trunk build.")
    val rustPackage    = taskKey[Unit]("Package program using cargo package or trunk package.")
    val rustConfig     = taskKey[Unit]("Show the current configuration.")
    val rustDoc        = taskKey[Unit]("Generate documentation using cargo doc or trunk doc.")
//    val clean               = taskKey[Unit]("Generic clean command")
  }

  object privateImport {
    val useCargo = settingKey[Boolean]("Factor the tooling package manager to a simple test")
  }

  import autoImport._
  import privateImport._

  override def requires = empty
  override def trigger  = noTrigger

  private def execCommand(command: String, workingDirectory: File) = Def.task {
    val application = command.takeWhile(_ != ' ')
    val arguments   = command.drop(application.length).trim
    val process     = Process(Seq(application, arguments), workingDirectory)
    val exitCode    = (process !)
    if (exitCode != 0)
      throw new RuntimeException(s"Command '$command' failed with exit code $exitCode")
  }

  override lazy val projectSettings = {
    inConfig(Rust)(
      Seq(
        tooling        := TrunkPackageManager,
        debugOptions   := "",
        releaseOptions := "",
        runOptions     := ""
      )
    ) ++ Seq(
      useCargo := (Rust / tooling).value == CargoPackageManager
    ) ++ Seq(
      rustClean      := Def.taskDyn {
        val useCargoV = useCargo.value
        val command   = if (useCargoV) "cargo clean" else "trunk clean"
        execCommand(command, thisProject.value.base)
      }.value,
      rustCargoClean := Def.taskDyn {
        execCommand("cargo clean", thisProject.value.base)
      }.value,
      rustBuild      := Def.taskDyn {
        val useCargoV    = useCargo.value
        val cargoCommand = s"cargo ${(Rust / debugOptions).value} build"
        val trunkCommand = s"trunk ${(Rust / debugOptions).value} build"
        val command      = if (useCargoV) cargoCommand else trunkCommand
        execCommand(command, thisProject.value.base)
      }.value,
      rustTest       := Def.taskDyn {
        execCommand("cargo test", thisProject.value.base)
      }.value,
      rustRun        := Def.taskDyn {
        val useCargoV    = useCargo.value
        val cargoCommand = s"cargo run ${(Rust / runOptions).value}"
        val trunkCommand = s"trunk serve ${(Rust / runOptions).value}"
        val command      = if (useCargoV) cargoCommand else trunkCommand
        execCommand(command, thisProject.value.base)
      }.value,
      rustRelease    := Def.taskDyn {
        val useCargoV    = useCargo.value
        val cargoCommand = s"cargo ${(Rust / releaseOptions).value} build --release"
        val trunkCommand = s"trunk ${(Rust / releaseOptions).value} build --release"
        val command      = if (useCargoV) cargoCommand else trunkCommand
        execCommand(command, thisProject.value.base)
      }.value,
      rustPackage    := Def.taskDyn {
        execCommand("cargo package", thisProject.value.base)
      }.value,
      rustConfig     := Def.taskDyn {
        val useCargoV    = useCargo.value
        val cargoCommand = "cargo config get"
        val trunkCommand = "trunk config show"
        val command      = if (useCargoV) cargoCommand else trunkCommand
        execCommand(command, thisProject.value.base)
      }.value,
      rustDoc        := Def.taskDyn {
        execCommand("cargo doc", thisProject.value.base)
      }.value
////      clean          := println(s"${thisProject.value}\n\n"),
////      // ((ThisBuild / clean) dependsOn rustClean).value,
////      compile        := ((Compile / compile) dependsOn rustBuild).value,
////      test           := ((Test / test) dependsOn rustTest).value,
////      run            := ((ThisProject / run) dependsOn rustRun),
////      Keys.`package` := ((Compile / Keys.`package`) dependsOn rustPackage).value,
////      doc            := ((Compile / doc) dependsOn rustDoc).value
    )

  }

}
