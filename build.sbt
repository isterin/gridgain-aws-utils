organization := "com.github.isterin"

name := "gridgain-aws-utils"

moduleName := "gridgain-aws-utils"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.1"

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "GridGain Repo" at "http://www.gridgainsystems.com/maven2/"

libraryDependencies ++= Seq(
  "org.gridgain" % "gridgain" % "3.6.0c" exclude("com.amazonaws", "aws-java-sdk"),
  "com.amazonaws" % "aws-java-sdk" % "1.3.3"
)

publishTo <<= (version) { version: String =>
	Some(Resolver.file("file", new File("../maven-repo") / {
    	if  (version.trim.endsWith("SNAPSHOT"))  "snapshots"
        else                                     "releases/" }    ))
}