package nigeleke.sbt

import sbt._

import nigeleke.sbt.RustPlugin.*

import org.scalatest.matchers.should.*
import org.scalatest.wordspec.*

class RustPluginSpec extends AnyWordSpec with Matchers {

  "RustPlugin" should {

    "expect nine rustSomething sbt commands" in {
      val rustCommands =
        projectSettings.map(_.toString).filter(_.contains("This / This / This,rust"))
      println(rustCommands.mkString("\n"))
      rustCommands.size should be(9)
    }

    /* A weak set of tests. Better (but still somewhat weak) tests are evaluated in `scripted`. */
    "allow the following sbt commands" when {

      def assertExists(keyValue: String) =
        projectSettings.map(_.toString).find(_.contains(keyValue)) match {
          case Some(_) => succeed
          case None    => fail(s"$keyValue not found in $projectSettings")
        }

      "rustClean" in { assertExists("rustClean") }
      "rustCargoClean" in { assertExists("rustCargoClean") }
      "rustBuild" in { assertExists("rustBuild") }
      "rustTest" in { assertExists("rustTest") }
      "rustRun" in { assertExists("rustRun") }
      "rustRelease" in { assertExists("rustRelease") }
      "rustPackage" in { assertExists("rustPackage") }
      "rustConfig" in { assertExists("rustConfig") }
      "rustDoc" in { assertExists("rustDoc") }
      "clean" in { assertExists("clean") }
      "compile" in { assertExists("compile") }
      "test" in { assertExists("test") }
      "run" in { assertExists("run") }
      "package" in { assertExists("package") }
      "doc" in { assertExists("doc") }

    }

  }

}
