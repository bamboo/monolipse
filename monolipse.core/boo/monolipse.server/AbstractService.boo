namespace monolipse.server

import System.IO
import Boo.Lang.Compiler.TypeSystem
import monolipse.core

class AbstractService:
	
	_client as ProcessMessengerClient
	_buffer = StringWriter()

	def constructor(client as ProcessMessengerClient):
		_client = client
		registerMessageHandlers()
	
	def writeLine(line):
		_buffer.WriteLine(line)
		
	def resetBuffer():
		_buffer.GetStringBuilder().Length = 0
		
	def flush(name as string):
		_client.Send(Message(Name: name, Payload: _buffer.ToString()))
		
	def getEntityType(entity as IEntity):
		if EntityType.Type == entity.EntityType:
			type = entity as IType
			return "Interface" if type.IsInterface
			return "Enum" if type.IsEnum
			return "Struct" if type.IsValueType
			return "Callable" if type isa ICallableType
			return "Class"
		return entity.EntityType.ToString()
		
	def writeTypeSystemEntities(entities as (IEntity)):
		for member in entities:
			writeLine("${getEntityType(member)}:${member.Name}:${describeEntity(member)}")
		
	abstract def registerMessageHandlers():
		pass
