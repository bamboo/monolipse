namespace monolipse.core

class MessageHandlerRegistry:
	
	_handlers = {}
	
	def Add([required] name as string, [required] handler as MessageHandler):
		_handlers.Add(name, handler)
		
	def Dispatch(message as Message):
		handler as MessageHandler = _handlers[message.Name]
		return if handler is null
		handler(message)