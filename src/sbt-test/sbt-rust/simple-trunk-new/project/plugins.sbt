{
  val pluginVersion = System.getProperty("plugin.version")
  println(s"pluginVersion2: $pluginVersion")
  if (pluginVersion == null) {
    throw new RuntimeException(
      """|The system property 'plugin.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  } else addSbtPlugin("nigeleke" % "sbt-rust" % pluginVersion)
}
