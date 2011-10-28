namespace monolipse.core

import System.IO

class AbstractService:
	
	final client as ProcessMessengerClient
	
	final response = StringWriter()

	def constructor(client as ProcessMessengerClient):
		self.client = client
		
	def onMessage(name as string, handler as MessageHandler):
		client.OnMessage(name) do (message as Message):
			resetBuffer()
			try:
				handler(message)
			except x:
				System.Console.Error.WriteLine(x)
	
	def writeLine(line):
		response.WriteLine(line)
		
	def flushResponse(name as string):
		payload = response.ToString()
		resetBuffer()
		send name, payload
		
	def resetBuffer():
		response.GetStringBuilder().Length = 0
		
	def send(name as string, payload):
		client.Send(Message(Name: name, Payload: payload.ToString()))
		
	def send(name as string):
		send name, ""
