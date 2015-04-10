import NativePackagerKeys._
val akkaVersion = "2.3.9"
val sprayVerion = "1.3.2"
val playVersion = "2.3.8"
val rmongoVersion = "0.10.5.0.akka23"
libraryDependencies ++= Seq(
    "io.spray"            %%  "spray-can"     % sprayVerion,
    "io.spray"            %%  "spray-routing" % sprayVerion,
    "io.spray"            %%  "spray-testkit" % sprayVerion % "test",
    "io.spray"            %%  "spray-client"  % sprayVerion,
    "com.typesafe.play"   %%  "play-json"     % playVersion,
    "com.typesafe.akka"   %%  "akka-actor"    % akkaVersion,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaVersion % "test",
    "ch.qos.logback" 	  %   "logback-classic" % "1.0.9",
    "org.reactivemongo"   %%  "reactivemongo" % rmongoVersion,
    "org.specs2"          %%  "specs2-core"   % "2.3.11" 	% "test"
)

lazy val commonSettings = Seq(
	organization := "com.wannaup",
	version := "0.0.1",
	resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
	scalaVersion := "2.11.5",
	scalacOptions ++= Seq(),
	scalacOptions in Test ~= { (options: Seq[String]) =>
	    options filterNot (_ == "-Ywarn-dead-code") // Allow dead code in tests (to support using mockito).
	},
	parallelExecution in Test := false
)

lazy val root = (project in file("."))
	.settings(commonSettings: _*)
	.settings(
		name := "postman"
)

packageArchetype.java_application
Revolver.settings