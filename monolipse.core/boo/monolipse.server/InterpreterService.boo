namespace monolipse.server

import System
import Boo.Lang.Interpreter
import monolipse.core

service InterpreterService:
	
	interpreter = InteractiveInterpreter(RememberLastValue: true, Print: writeLine)
	
	onMessageWithResponse "EVAL":
		interpreter.LoopEval(message.Payload)

	onMessageWithResponse "GET-INTERPRETER-PROPOSALS":
		proposals = interpreter.SuggestCodeCompletion(message.Payload)
		writeTypeSystemEntitiesTo proposals, response
