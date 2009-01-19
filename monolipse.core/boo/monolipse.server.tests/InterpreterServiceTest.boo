namespace monolipse.server.tests

import NUnit.Framework
import monolipse.core
import monolipse.server

[TestFixture]
class InterpreterServiceTest:
	
	clientMock = ProcessMessengerClientMock()
	service = InterpreterService(clientMock)
	
	[Test] def GetInterpreterProposals():
		
		message = Message(Name: "GET-INTERPRETER-PROPOSALS", Payload: "object().__codecomplete__")
		clientMock.Dispatch(message)
		response = clientMock.Messages
		assert 1 == len(response), response.ToString()
		
		expectedPayload = """
Constructor:constructor:
Method:Equals:def Equals(obj as object) as bool
Method:Equals:static def Equals(objA as object, objB as object) as bool
Method:GetHashCode:def GetHashCode() as int
Method:GetType:def GetType() as System.Type
Method:ToString:def ToString() as string
Method:ReferenceEquals:static def ReferenceEquals(objA as object, objB as object) as bool
"""
		Assert.AreEqual("GET-INTERPRETER-PROPOSALS-RESPONSE", response[0].Name)
		Assert.AreEqual(normalizeWhitespace(expectedPayload), normalizeWhitespace(response[0].Payload))
		
def normalizeWhitespace(s as string):
	return s.Trim().Replace("\r\n", "\n")