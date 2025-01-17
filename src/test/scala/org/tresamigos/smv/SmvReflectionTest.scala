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

class SmvReflectionTest extends SparkTestUtil {
//  override def appArgs = testAppArgs.singleStage ++ Seq("-m", "None")

  test("Test SmvReflection.objectsInPackage method.") {
    val mods: Seq[SmvModule] = SmvReflection.objectsInPackage[SmvModule]("org.tresamigos.smv.smvAppTestPkg1")

    assertUnorderedSeqEqual(mods,
      Seq(org.tresamigos.smv.smvAppTestPkg1.X, org.tresamigos.smv.smvAppTestPkg1.Y))(
        Ordering.by[SmvModule, String](_.fqn))


    // TODO: CLEANUP: this doesn't belong here
//    assert(testApp.moduleNameForPrint(org.tresamigos.smv.smvAppTestPkg1.X) === "X")
  }

}
