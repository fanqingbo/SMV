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

import org.tresamigos.smv.class_loader.SmvClassLoader

abstract class DataSetRepo {
  def loadDataSet(urn: String): SmvDataSet
}

abstract class DataSetRepoFactory {
  def createRepo(): DataSetRepo
}

class DataSetRepoScala(smvConfig: SmvConfig) extends DataSetRepo {
  val cl = SmvClassLoader(smvConfig, getClass.getClassLoader)

  def loadDataSet(fqn: String): SmvDataSet = {
    val ref = new SmvReflection(cl)
    ref.objectNameToInstance[SmvDataSet](fqn)
  }
}

class DataSetRepoFactoryScala(smvConfig: SmvConfig = new SmvConfig(Seq())) extends DataSetRepoFactory {
  def createRepo(): DataSetRepoScala = new DataSetRepoScala(smvConfig)
}

class DataSetRepoPython (iDSRepo: IDataSetRepoPy4J) extends DataSetRepo {
  def loadDataSet(fqn: String): SmvDataSet = iDSRepo.loadDataSet(fqn)
}

// This class will be implemented as its own Java interface, but for short term testing purposes
// we will wire it through to the deprecated SmvDataSetRepo interface
class IDataSetRepoPy4J() {
  def loadDataSet(fqn: String): SmvDataSet = SmvExtModule(fqn)
}

class DataSetRepoFactoryPython(iDSRepoFactory: IDataSetRepoFactoryPy4J) extends DataSetRepoFactory {
  def createRepo(): DataSetRepoPython = new DataSetRepoPython(iDSRepoFactory.createRepo())
}

// This class will be implemented as its own Java interface, but for short term testing purposes
// we will shimmy it in
class IDataSetRepoFactoryPy4J(repoFactoryMthd: () => IDataSetRepoPy4J) {
  def createRepo(): IDataSetRepoPy4J = repoFactoryMthd()
}