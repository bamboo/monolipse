namespace monolipse.server.tests

import monolipse.core

class ProcessMessengerClientMock(ProcessMessengerClient):
	
	_handlers = MessageHandlerRegistry()
	[getter(Messages)] _messages = List[of Message]()
	
	def OnMessage(messageName as string, handler as MessageHandler):
		_handlers.Add(messageName, handler)
		
	def Send(message as Message):
		_messages.Add(message)
		
	def Dispatch(message as Message):
		_handlers.Dispatch(message)