from smv import *
import traceback

try:
    from org.tresamigos.smvtest.test4_1.modules import M1

    M1Link = SmvPyModuleLink(M1)

except:
    traceback.print_exc()
