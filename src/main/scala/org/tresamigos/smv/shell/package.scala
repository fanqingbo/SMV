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
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import java.io.File


/**
 * Provide functions for the interactive shell
 *
 * In SMV's `tools/conf/smv_shell_init.scala` or project's `conf/shell_init.scala` add
 * {{{
 * import org.tresamigos.smv.shell._
 * }}}
 **/
package object shell {
  /**
   * list all the smv-shell commands
   **/
  def help = println(ShellCmd.help)

  /**
   * list all the stages
   **/
  def lsStage = println(ShellCmd.lsStage)

  /**
   * list all datasets in a stage
   * @param stageName could be the FQN or just the basename
   **/
  def ls(stageName: String) = println(ShellCmd.ls(stageName))

  /**
   * list all the datasets in the entire project
   **/
  def ls = println(ShellCmd.ls)

  /**
   * list `dead` datasets in a stage
   * `dead` dataset is defined as "no contribution to the Output modules of the stage"
   * @param stageName could be the FQN or the basename
   **/
  def lsDead(stageName: String) = println(ShellCmd.lsDead(stageName))

  /**
   * list `dead` datasets in the entire project
   **/
  def lsDead = println(ShellCmd.lsDead)

  /**
   * list `leaf` datasets in a stage
   * `leaf` dataset is defined as "no modules in the stage depend on it, excluding Output modules"
   * Note: a `leaf` dataset must be `dead`, but some `dead` datasets are Not `leaf`s
   * @param stageName could be the FQN or the basename
   */
  def lsLeaf(stageName: String) = println(ShellCmd.lsLeaf(stageName))

  /**
   * list `leaf` datasets in the entire project
   **/
  def lsLeaf = println(ShellCmd.lsLeaf)

  /** take a stage name and print all DS in this stage, without unused input DS */
  def graph(stageName: String) = println(ShellCmd._graph(stageName))

  /** take no parameter, print stages and inter-stage links */
  def graph() = println(ShellCmd._graph())

  /** take a DS, print in-stage dependency of that DS */
  def graph(ds: SmvDataSet) = println(ShellCmd._graph(ds))

  /**
   * list all `ancestors` of a dataset
   * `ancestors` are datasets current dataset depends on, directly or in-directly,
   * even include datasets from other stages
   **/
  def ancestors(ds: SmvDataSet) = println(ShellCmd.ancestors(ds))

  /**
   * list all `descendants` of a dataset
   * `descendants` are datasets which depend on the current dataset directly or in-directly,
   * even include datasets from other stages
   **/
  def descendants(ds: SmvDataSet) = println(ShellCmd.descendants(ds))

  /**
   * Print current time
   **/
  def now() = println(ShellCmd.now())

  /**
   * Read in a Hive table as DF
   **/
  def openHive(tableName: String) = ShellCmd.openHive(tableName)

  /**
   * Read in a Csv file as DF
   **/
  def openCsv(path: String, ca: CsvAttributes = null, parserCheck: Boolean = false)
    = ShellCmd.openCsv(path, ca, parserCheck)

  /**
   * Resolve SmvDataSet
   *
   * @param ds an SmvDataSet
   * @return result DataFrame
   **/
  def df(ds: SmvDataSet) = ShellCmd.df(ds)


  /**
   * Dynamically load modules
   *
   * @param fqn the fully qualified name of SmvDataSet
   * @return result DataFrame
   **/
  def ddf(fqn: String) = ShellCmd.ddf(fqn)
  def ddf(ds: SmvDataSet) = ShellCmd.ddf(ds.fqn)

  /**
   * Try best to discover Schema from raw Csv file
   *
   * @param path Csv file path and name
   * @param n number of records to check for schema discovery, default 100k
   * @param ca CsvAttributes, default CsvWithHeader
   *
   * Will save a schema file with postfix ".toBeReviewed" in local directory.
   **/
  def discoverSchema(
    path: String,
    n: Int = 100000,
    ca: CsvAttributes = CsvAttributes.defaultCsvWithHeader
  ) = {
    implicit val csvAttributes=ca
    val helper = new SchemaDiscoveryHelper(SmvApp.app.sqlContext)
    val schema = helper.discoverSchemaFromFile(path, n)
    val outpath = SmvSchema.dataPathToSchemaPath(path) + ".toBeReviewed"
    val outFileName = (new File(outpath)).getName
    schema.saveToLocalFile(outFileName)
    println(s"Discovered schema file saved as ${outFileName}, please review and make changes.")
  }
}
