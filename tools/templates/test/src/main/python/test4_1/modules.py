from smv import *

from org.tresamigos.smvtest.test4_1 import input

class M1(SmvPyModule, SmvPyOutput):
    def requiresDS(self):
        return [input.table]

    def run(self, i):
        return i[input.table]
