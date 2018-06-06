/* =========================================================================================
 * Copyright © 2013-2018 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

val kamonCore       = "io.kamon"      %% "kamon-core"     % "1.1.2"
val kamonTestkit    = "io.kamon"      %% "kamon-testkit"  % "1.1.2"
//val el              = "org.glassfish" %  "javax.el"      % "3.0.0"

lazy val root = (project in file("."))
  .settings(noPublishing: _*)
  .aggregate(annotationApi, annotation)

val commonSettings = Seq(
    scalaVersion := "2.12.6",
    resolvers += Resolver.mavenLocal,
    crossScalaVersions := Seq("2.12.6", "2.11.12", "2.10.7")
)

lazy val annotationApi = (project in file("kamon-annotation-api"))
  .enablePlugins(JmhPlugin)
  .settings(moduleName := "kamon-annotation-api", resolvers += Resolver.mavenLocal)
  .settings(noPublishing: _*)
  .settings(commonSettings: _*)

lazy val annotation = (project in file("kamon-annotation"))
  .enablePlugins(JavaAgent)
  .settings(moduleName := "kamon-annotation")
  .settings(commonSettings: _*)
  .settings(javaAgents += "io.kamon"  % "kanela-agent"  % "0.0.300"  % "compile;test")
  .settings(
      libraryDependencies ++=
        compileScope(kamonCore) ++
          testScope(scalatest, logbackClassic, kamonTestkit)
  ).dependsOn(annotationApi)
