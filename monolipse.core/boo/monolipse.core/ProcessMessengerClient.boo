namespace monolipse.core

class Message:
	public static final EndMarker = "END-MESSAGE"	
	public Name as string	
	public Payload as string
	
	override def ToString():
		return "Message(${Name}, ${Payload})"

callable MessageHandler(message as Message)

interface ProcessMessengerClient:
	
	def Send(message as Message)
	def OnMessage(messageName as string, handler as MessageHandler)
	
[extension] def send(this as ProcessMessengerClient, message as string):
	this.send(message, "")
	
[extension] def send(this as ProcessMessengerClient, message as string, payload):
	this.Send(Message(Name: message, Payload: payload.ToString()))
	