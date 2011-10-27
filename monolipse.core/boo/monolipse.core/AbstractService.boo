namespace monolipse.core

import System.IO

class AbstractService:
	
	[getter(client)]
	_client as ProcessMessengerClient
	
	[getter(response)] 
	_response = StringWriter()

	def constructor(client as ProcessMessengerClient):
		_client = client
		
	def onMessage(name as string, handler as MessageHandler):
		_client.OnMessage(name) do (message as Message):
			resetBuffer()
			try:
				handler(message)
			except x:
				System.Console.Error.WriteLine(x)
	
	def writeLine(line):
		_response.WriteLine(line)
		
	def resetBuffer():
		_response.GetStringBuilder().Length = 0
		
	def flush(name as string):
		payload = _response.ToString()
		resetBuffer()
		send name, payload
		
	def send(name as string, payload):
		_client.Send(Message(Name: name, Payload: payload.ToString()))
		
	def send(name as string):
		send(name, "")
