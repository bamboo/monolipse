namespace monolipse.nunit.server

import NUnit.Core from nunit.core.interfaces as NUnitInterfaces

class TestFilter(NUnitInterfaces.ITestFilter):

	_testCases as (string)
	
	IsEmpty as bool:
		get: return false

	def constructor(testCases as (string)):
		_testCases = testCases
		
	def Pass(test as NUnitInterfaces.ITest) as bool:
		return true if _testCases.Length == 0
		return true if test.IsSuite
		return true if test.TestName.FullName in _testCases
		return false

	def Match(test as NUnitInterfaces.ITest) as bool:
		return true