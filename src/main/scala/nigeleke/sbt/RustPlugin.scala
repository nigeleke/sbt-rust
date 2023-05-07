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
import sbt.Keys.*

import scala.language.postfixOps
import scala.sys.process.*

object RustPlugin extends AutoPlugin {

  sealed trait PackageManager
  case object CargoPackageManager extends PackageManager
  case object TrunkPackageManager extends PackageManager

  object autoImport {
    val tooling        = settingKey[PackageManager]("Cargo or Trunk. Default TrunkPackageManager")
    val cleanOptions   = settingKey[String]("Options for 'cargo clean'")
    val debugOptions   = settingKey[String]("Options for 'cargo build'")
    val releaseOptions = settingKey[String]("Options for 'cargo build --release'")
    val runOptions     = settingKey[String]("Options to pass to 'cargo run'")
    val docOptions     = settingKey[String]("Options to pass to 'cargo doc'")
    val rustClean      = taskKey[Unit]("Clean using 'cargo clean' or 'trunk clean'")
    val rustCargoClean = taskKey[Unit]("Force 'cargo clean', when 'trunk clean' is not enough.")
    val rustBuild      = taskKey[Unit]("Compile code using 'cargo build' or 'trunk build'.")
    val rustTest       = taskKey[Unit]("Run tests using 'cargo test' or 'trunk test'.")
    val rustRun        = taskKey[Unit]("Run program using 'cargo run' or 'trunk serve'.")
    val rustRelease    = taskKey[Unit]("Compile with --release using 'cargo build' or 'trunk build'.")
    val rustPackage    = taskKey[Unit]("Package program using 'cargo package' or 'trunk package'.")
    val rustConfig     = taskKey[Unit]("Show the current configuration.")
    val rustDoc        = taskKey[Unit]("Generate documentation using 'cargo doc'")
  }

  import autoImport._

  override def requires = plugins.JvmPlugin
  override def trigger  = noTrigger

  private def execCommand(command: String, workingDirectory: File): Def.Initialize[Task[Unit]] =
    Def.task {
      val builder = Process(command, workingDirectory)
      val process = builder.run()

      scala.sys.addShutdownHook { destroyProcess() }

      def destroyProcess() = if (process.isAlive()) process.destroy()

      try {
        val exitCode = process.exitValue()
        if (exitCode != 0)
          throw new RuntimeException(s"Command '$command' failed with exit code $exitCode")
      } catch {
        case _: InterruptedException => destroyProcess()
      } finally {
        destroyProcess()
      }
    }

  lazy val Rust = config("rust")

  override lazy val projectSettings = {
    inConfig(Rust)(
      Seq(
        tooling        := TrunkPackageManager,
        cleanOptions   := "",
        debugOptions   := "",
        releaseOptions := "",
        runOptions     := "",
        docOptions     := ""
      )
    ) ++ Seq(
      rustClean      := Def.taskDyn {
        val useCargo     = (Rust / tooling).value == CargoPackageManager
        val cargoCommand = s"cargo clean ${(Rust / cleanOptions).value}"
        val trunkCommand = s"trunk clean"
        val command      = if (useCargo) cargoCommand else trunkCommand
        execCommand(command, thisProject.value.base)
      }.value,
      rustCargoClean := Def.taskDyn {
        val command = s"cargo clean ${(Rust / cleanOptions).value}"
        execCommand(command, thisProject.value.base)
      }.value,
      rustBuild      := Def.taskDyn {
        val useCargo     = (Rust / tooling).value == CargoPackageManager
        val cargoCommand = s"cargo build ${(Rust / debugOptions).value}"
        val trunkCommand = s"trunk build"
        val command      = if (useCargo) cargoCommand else trunkCommand
        execCommand(command, thisProject.value.base)
      }.value,
      rustTest       := Def.taskDyn {
        execCommand("cargo test", thisProject.value.base)
      }.value,
      rustRun        := Def.taskDyn {
        val useCargo     = (Rust / tooling).value == CargoPackageManager
        val cargoCommand = s"cargo run ${(Rust / runOptions).value}"
        val trunkCommand = s"trunk serve ${(Rust / runOptions).value}"
        val command      = if (useCargo) cargoCommand else trunkCommand
        execCommand(command, thisProject.value.base)
      }.value,
      rustRelease    := Def.taskDyn {
        val useCargo     = (Rust / tooling).value == CargoPackageManager
        val cargoCommand = s"cargo build --release ${(Rust / releaseOptions).value}"
        val trunkCommand = s"trunk build --release"
        val command      = if (useCargo) cargoCommand else trunkCommand
        execCommand(command, thisProject.value.base)
      }.value,
      rustPackage    := Def.taskDyn {
        execCommand("cargo package", thisProject.value.base)
      }.value,
      rustConfig     := Def.taskDyn {
        val useCargo     = (Rust / tooling).value == CargoPackageManager
        val cargoCommand = "cargo --list" // TODO: Implement when fix released.
        val trunkCommand = "trunk config show"
        val command      = if (useCargo) cargoCommand else trunkCommand
        execCommand(command, thisProject.value.base)
      }.value,
      rustDoc        := Def.taskDyn {
        execCommand(s"cargo doc ${(Rust / docOptions).value}", thisProject.value.base)
      }.value,
      clean          := (Compile / clean).dependsOn(rustClean).value,
      compile        := (Compile / compile).dependsOn(rustBuild).value,
      test           := (Test / test).dependsOn(rustTest).value,
      Keys.`package` := (Compile / Keys.`package`).dependsOn(rustPackage).value,
      doc            := (Compile / doc).dependsOn(rustDoc).value
    )

  }

}
