namespace monolipse.nunit.server

import System
import NUnit.Util from nunit.util
import NUnit.Core
import monolipse.core

service NUnitRunner:
	
	onMessage "RUN":
		try:
			arguments = /,/.Split(message.Payload.Trim())
			
			assemblyName = arguments[0]
			testCases = arguments[1:]
			domain = TestDomain()
			assert domain.Load(TestPackage(assemblyName, [assemblyName]))
			
			testFilter = null
			
			send "TESTS-STARTED", domain.CountTestCases(testFilter) 
			domain.Run(TestListener(client), testFilter)
		except x:
			Console.Error.WriteLine(x)
		ensure:
			send "TESTS-FINISHED"
			(client as NetworkProcessMessengerClient).Stop()

