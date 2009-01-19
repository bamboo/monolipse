namespace monolipse.core

import System.IO

class AbstractService:
	
	_client as ProcessMessengerClient
	
	[getter(response)] 
	_response = StringWriter()

	def constructor(client as ProcessMessengerClient):
		_client = client
		
	def onMessage(name as string, handler as MessageHandler):
		_client.OnMessage(name) do (message as Message):
			resetBuffer()
			handler(message)
	
	def writeLine(line):
		_response.WriteLine(line)
		
	def resetBuffer():
		_response.GetStringBuilder().Length = 0
		
	def flush(name as string):
		_client.Send(Message(Name: name, Payload: _response.ToString()))
