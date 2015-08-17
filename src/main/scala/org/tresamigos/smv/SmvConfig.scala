/*
 * This file is licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tresamigos.smv

import scala.util.Try

import java.io.{IOException, InputStreamReader, FileInputStream, File}
import java.util.Properties

import org.rogach.scallop.ScallopConf


/**
 * command line argumetn parsing using scallop library.
 * See (https://github.com/scallop/scallop) for details on using scallop.
 */
private[smv] class CmdLineArgsConf(args: Seq[String]) extends ScallopConf(args) {
  val DEFAULT_SMV_APP_CONF_FILE = "conf/smv-app-conf.props"
  val DEFAULT_SMV_USER_CONF_FILE = "conf/smv-user-conf.props"

  val smvProps = propsLong[String]("smv-props", "key=value command line props override")
  val smvAppConfFile = opt("smv-app-conf", noshort = true,
    default = Some(DEFAULT_SMV_APP_CONF_FILE),
    descr = "app level (static) SMV configuration file path")
  val smvUserConfFile = opt("smv-user-conf", noshort = true,
    default = Some(DEFAULT_SMV_USER_CONF_FILE),
    descr = "user level (dynamic) SMV configuration file path")
  val devMode = toggle("dev", default=Some(false),
    descrYes="enable dev mode (persist all intermediate module results",
    descrNo="enable production mode (all modules are evaluated from scratch")
  val genEdd = toggle("edd", default = Some(false), noshort = true,
    descrYes = "summarize data and generate an edd file in the same directory as csv and schema",
    descrNo  = "do not summarize data")
  val graph = toggle("graph", default=Some(false),
    descrYes="generate a dependency graph of the given modules (modules are not run)",
    descrNo="do not generate a dependency graph")
  val json = toggle("json", default=Some(false), noshort=true,
    descrYes="generate a json object to represent entire app's module dependency (modules are not run)",
    descrNo="do not generate a json")

  val modsToRun = opt[List[String]]("run-module", 'm', descr = "run specified list of module FQNs")
  val stagesToRun = opt[List[String]]("run-stage", 's', descr = "run all output modules in specified stages")
  val runAllApp = toggle("run-app", noshort = true, default = Some(false),
                  descrYes = "run all output modules in all stages in app.")

  // make sure something was specified to run!!!
  validateOpt (modsToRun, stagesToRun, runAllApp) {
    case (None, None, Some(false)) => Left("Must supply an app, stage, or module to run!")
    case _ => Right(Unit)
  }

  // override default error message handler that does a sys.exit(1) which makes it difficult to debug.
  printedName = "SmvApp"
  errorMessageHandler = { errMsg =>
    println(printedName + ": " + errMsg)
    throw new IllegalArgumentException(errMsg)
  }

}

/**
 * Container of all SMV config driven elements (cmd line, app props, user props, etc).
 */
class SmvConfig(cmdLineArgs: Seq[String]) {
  val cmdLine = new CmdLineArgsConf(cmdLineArgs)

  private val appConfProps = _loadProps(cmdLine.smvAppConfFile())
  private val usrConfProps = _loadProps(cmdLine.smvUserConfFile())
  private val cmdLineProps = cmdLine.smvProps
  private val defaultProps = Map(
    "smv.appName" -> "Smv Application",
    "smv.stages" -> ""
  )

  // merge order is important here.  Highest priority comes last as it will override all previous
  private[smv] val mergedProps = defaultProps ++ appConfProps ++ usrConfProps ++ cmdLineProps

  // --- config params.  App should access configs through vals below rather than from props maps
  val appName = mergedProps("smv.appName")
  private val stagesList = splitProp("smv.stages") map {s => SmvStage(s, this)}
  val stages = new SmvStages(stagesList.toSeq)

  val sparkSqlProps = mergedProps.filterKeys(k => k.startsWith("spark.sql."))

  /**
   * sequence of SmvModules to run based on the command line arguments.
   * Returns the union of -a/-m/-s command line flags.
   */
  private[smv] def modulesToRun() : Seq[SmvModule] = {

    val empty = Some(Seq.empty[String])
    val directMods = cmdLine.modsToRun.orElse(empty)().map { m => SmvReflection.objectNameToInstance[SmvModule](m) }
    val stageMods = cmdLine.stagesToRun.orElse(empty)().flatMap(s => stages.findStage(s).allOutputModules)
    val appMods = if (cmdLine.runAllApp()) stages.allOutputModules else Seq.empty[SmvModule]

    directMods ++ stageMods ++ appMods
  }

  /**
   * load the given properties file and return the resulting props as a scala Map.
   * Note: if the provided file does not exit, this will just silently return an empty map.
   * This is by design as the default app/user smv config files may be missing (valid).
   * Adopted from the Spark AbstractCommnadBuilder::loadPropertiesFile
   */
  private def _loadProps(propsFileName: String) : scala.collection.Map[String, String] = {
    import scala.collection.JavaConverters._

    val props = new Properties()
    val propsFile = new File(propsFileName)

    if (propsFile.isFile()) {
      var fd: FileInputStream = null
      try {
        fd = new FileInputStream(propsFile)
        props.load(new InputStreamReader(fd, "UTF-8"));
      } finally {
        if (fd != null) {
          try {
            fd.close()
          } catch {
            case e:IOException => None
          }
        }
      }
    }

    props.asScala
  }

  /**
   * convert the property value to an array of strings.
   * Return empty array if original prop value was an empty string
   */
  private[smv] def splitProp(propName: String) = {
    val propVal = mergedProps(propName)
    if (propVal == "")
      Array[String]()
    else
      propVal.split(Array(',', ':')).map(_.trim)
  }

  private[smv] def getPropAsInt(propName: String) : Option[Int] = {
    mergedProps.get(propName).flatMap { s: String => Try(s.toInt).toOption }
  }
}
