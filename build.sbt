name := "Alexandrite"

version := "1"

scalaVersion := "2.13.7"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % "3.5.0" cross CrossVersion.full)

libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.5.0"

scalacOptions ++= Seq(
      "-Xsource:2.13",
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit"
      // Enables autoclonetype2 in 3.4.x (on by default in 3.5)
    //   "-P:chiselplugin:useBundlePlugin"
    )