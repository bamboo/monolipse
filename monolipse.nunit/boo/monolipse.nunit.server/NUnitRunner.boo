namespace monolipse.nunit.server

import System
import NUnit.Util from nunit.util
import monolipse.core

service NUnitRunner:
	
	onMessage "RUN":
		try:
			arguments = /,/.Split(message.Payload.Trim())
			
			assemblyName = arguments[0]
			testCases = arguments[1:]
			domain = TestDomain()
			test = domain.Load(assemblyName)
			if len(testCases) > 0:
				send "TESTS-STARTED", len(testCases) 
				domain.Run(TestListener(client), testCases)
			else:
				send "TESTS-STARTED", test.CountTestCases()
				test.Run(TestListener(client))
		except x:
			Console.Error.WriteLine(x)
		ensure:
			send "TESTS-FINISHED"
			(client as NetworkProcessMessengerClient).Stop()

