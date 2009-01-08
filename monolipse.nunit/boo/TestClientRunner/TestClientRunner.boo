namespace TestClientRunner

import System
import System.IO
import NUnit.Core from nunit.core
import NUnit.Util from nunit.util
import monolipse.core

// TODO: report errors
class TextWriterListener(MarshalByRefObject, EventListener):

	_client as ProcessMessengerClient
	_error as Exception
	
	def constructor(client as ProcessMessengerClient):
		_client = client
		
	def RunFinished(e as Exception):
		_error = e
		Console.Error.WriteLine("RunFinished")
		Console.Error.WriteLine(_error)
		
	def RunFinished(r as (TestResult)):
		pass
	
	def RunStarted(t as (Test)):
		pass

	def SuiteFinished(r as TestSuiteResult):
		pass
		
	def SuiteStarted(s as TestSuite):
		pass
		
#	def TestOutput(output as NUnit.Core.TestOutput):
#		pass
		
	[lock]
	def TestStarted(t as TestCase):
		_client.Send("TEST-STARTED", t.FullName)
		_error = null

	[lock]
	def TestFinished(r as TestCaseResult):
		unless r.IsFailure /*or r.SetupFailure*/: return

		Console.Error.WriteLine(r.Message)
		Console.Error.WriteLine(r.StackTrace)
		writer = StringWriter()
		writer.WriteLine(r.Test.FullName)
		writer.WriteLine(r.Message)
		writer.WriteLine(r.StackTrace)
		_client.Send("TEST-FAILED", writer.ToString())
		
	def UnhandledException(e as Exception):
		Console.Error.WriteLine("UnhandledException")
		Console.Error.WriteLine(e)

client = ProcessMessengerClient()
client.OnMessage("RUN") do (message as Message):
	try:
		assemblyName = message.Payload.Trim()
		domain = TestDomain()
		test = domain.Load(assemblyName)
		client.Send("TESTS-STARTED", test.CountTestCases().ToString())
		test.Run(TextWriterListener(client))
	except x:
		Console.Error.WriteLine(x)
	client.Send("TESTS-FINISHED", "")
	client.Stop()
	
portNumber, = argv
try:
	client.Start(int.Parse(portNumber))
except x:
	Console.Error.WriteLine(x)