#
# This file is licensed under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from smv import smvPy
from pyspark.sql.column import Column

def nGram2(c1, c2):
    return Column(smvPy._jvm.org.tresamigos.smv.smvfuncs.nGram2(c1._jc, c2._jc))

def nGram3(c1, c2):
    return Column(smvPy._jvm.org.tresamigos.smv.smvfuncs.nGram3(c1._jc, c2._jc))

def diceSorensen(c1, c2):
    return Column(smvPy._jvm.org.tresamigos.smv.smvfuncs.diceSorensen(c1._jc, c2._jc))

def normlevenshtein(c1, c2):
    return Column(smvPy._jvm.org.tresamigos.smv.smvfuncs.normlevenshtein(c1._jc, c2._jc))

def jaroWinkler(c1, c2):
    return Column(smvPy._jvm.org.tresamigos.smv.smvfuncs.jaroWinkler(c1._jc, c2._jc))
