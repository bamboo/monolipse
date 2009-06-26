namespace monolipse.nunit.server

import System
import System.IO
import NUnit.Core from nunit.core.interfaces
import monolipse.core

class TestListener(MarshalByRefObject, EventListener):

	_client as ProcessMessengerClient
	_error as Exception
	
	def constructor(client as ProcessMessengerClient):
		_client = client
		
	def RunFinished(e as Exception):
		_error = e
		Console.Error.WriteLine("RunFinished")
		Console.Error.WriteLine(_error)
		
	def RunFinished(r as TestResult):
		pass
	
	def RunStarted(name as string, testCount as int):
		pass

	def SuiteFinished(r as TestSuiteResult):
		pass
		
	def SuiteStarted(s as TestName):
		pass
		
	def TestStarted(name as TestName):
		Send("TEST-STARTED", name.FullName)
		_error = null
		
	def TestOutput(output as TestOutput):
		Console.WriteLine(output)

	def TestFinished(r as TestCaseResult):
		unless r.IsFailure /*or r.SetupFailure*/: return

		Console.Error.WriteLine(r.Message)
		Console.Error.WriteLine(r.StackTrace)
		writer = StringWriter()
		writer.WriteLine(r.Test.TestName.FullName)
		writer.WriteLine(r.Message)
		writer.WriteLine(r.StackTrace)
		Send("TEST-FAILED", writer.ToString())
		
	def UnhandledException(e as Exception):
		Console.Error.WriteLine("UnhandledException")
		Console.Error.WriteLine(e)
		
	def Send(messageName as string, payload as string):
		_client.Send(Message(Name: messageName, Payload: payload))

