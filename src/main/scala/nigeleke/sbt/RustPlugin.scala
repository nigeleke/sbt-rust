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

  object autoImport {
    val wasmBuild           = settingKey[Boolean](
      "Enable override for the plugin's trunk over cargo preference. Set false to use cargo. Default true. Only applies when both toml files exist."
    )
    val cargoDebugOptions   = settingKey[String]("Additional options for 'cargo build'")
    val cargoReleaseOptions = settingKey[String]("Additional options for 'cargo build --release'")
    val trunkDebugOptions   = settingKey[String]("Additional options for 'trunk build'")
    val trunkReleaseOptions = settingKey[String]("Additional options for 'trunk build --release'")
    val rustClean           = taskKey[Unit]("Clean using cargo clean or trunk clean.")
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
      wasmBuild: Boolean,
      baseDirectory: File // ,
//      log: Logger
  ) = {
    val cargoFound = (baseDirectory / "Cargo.toml").exists()
    val trunkFound = (baseDirectory / "Trunk.toml").exists()
    val command    = (cargoFound, trunkFound && wasmBuild) match {
      case (_, true) =>
        val realTrunkCommand = if (trunkCommand.isEmpty) cargoCommand else trunkCommand
        "trunk" +: realTrunkCommand.split(" ").toSeq
      case (true, _) =>
        "cargo" +: cargoCommand.split(" ").toSeq
      case _         =>
//        log.error("Cannot find `Cargo.toml` or `Trunk.toml` file.")
        Seq("echo", "'Command not completed'")
    }
//    log.info(command.mkString(" "))
    command !
  }

  lazy val Rust = config("rust")

  override lazy val projectSettings = {
    inConfig(Rust)(
      Seq(
        wasmBuild           := true,
        cargoDebugOptions   := "",
        cargoReleaseOptions := "",
        trunkDebugOptions   := "",
        trunkReleaseOptions := ""
      )
    ) ++ Seq(
      rustClean      := execCommand("clean")(
        (Rust / wasmBuild).value,
        baseDirectory.value // ,
//        streams.value.log
      ),
      rustBuild      := execCommand(
        s"build ${(Rust / cargoDebugOptions).value}",
        s"build ${(Rust / trunkDebugOptions).value}"
      )(
        (Rust / wasmBuild).value,
        baseDirectory.value // ,
//        streams.value.log
      ),
      rustTest       := execCommand("test")(
        (Rust / wasmBuild).value,
        baseDirectory.value // ,
//        streams.value.log
      ),
      rustRun        := execCommand("run", "serve")(
        (Rust / wasmBuild).value,
        baseDirectory.value // ,
//        streams.value.log
      ),
      rustRelease    := execCommand(
        s"build --release ${(Rust / cargoReleaseOptions).value}",
        s"build --release ${(Rust / trunkReleaseOptions).value}"
      )(
        (Rust / wasmBuild).value,
        baseDirectory.value // ,
//        streams.value.log
      ),
      rustPackage    := execCommand("package")(
        (Rust / wasmBuild).value,
        baseDirectory.value // ,
//        streams.value.log
      ),
      rustConfig     := execCommand(
        "config get",
        "config show"
      )(
        (Rust / wasmBuild).value,
        baseDirectory.value // ,
//        streams.value.log
      ),
      rustDoc        := execCommand("doc")(
        (Rust / wasmBuild).value,
        baseDirectory.value // ,
//        streams.value.log
      ),
      clean          := ((ThisBuild / clean) dependsOn rustClean).value,
      compile        := ((Compile / compile) dependsOn rustBuild).value,
      test           := ((Test / test) dependsOn rustTest).value,
      run            := ((ThisBuild / run) dependsOn rustRun),
      Keys.`package` := ((Compile / Keys.`package`) dependsOn rustPackage).value,
      doc            := ((Compile / doc) dependsOn rustDoc).value
    )
  }

}
