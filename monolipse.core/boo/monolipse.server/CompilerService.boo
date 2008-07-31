namespace monolipse.server

import System
import Boo.Lang.Compiler
import monolipse.core

class CompilerService(AbstractService):
	def constructor(client as ProcessMessengerClient):
		super(client)
		
	def registerMessageHandlers():
		_client.OnMessage("GET-OUTLINE") do (message as Message):
			resetBuffer()
			try:
				compiler = BooCompiler()
				compiler.Parameters.Pipeline = Pipelines.Parse()
				compiler.Parameters.Input.Add(Boo.Lang.Compiler.IO.StringInput("outline", message.Payload))
				module = compiler.Run().CompileUnit.Modules[0]
				module.Accept(OutlineVisitor(_buffer))
			except x:
				Console.Error.WriteLine(x)
				resetBuffer()
			flush("OUTLINE-RESPONSE")
			
		_client.OnMessage("GET-COMPILER-PROPOSALS") do (message as Message):
			resetBuffer()
			try:
				writeTypeSystemEntities(ContentAssistProcessor.getProposals(message.Payload))
			except x:
				Console.Error.WriteLine(x)
				resetBuffer()
			flush("COMPILER-PROPOSALS")
			
		_client.OnMessage("EXPAND") do (message as Message):
			resetBuffer()
			compiler = BooCompiler()
			compiler.Parameters.Pipeline = Pipelines.Compile()
			compiler.Parameters.Input.Add(Boo.Lang.Compiler.IO.StringInput("expand", message.Payload))
			writeLine(compiler.Run().CompileUnit.ToCodeString())
			flush("EXPAND-RESPONSE")
			
			