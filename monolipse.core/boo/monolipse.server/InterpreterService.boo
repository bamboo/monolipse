namespace monolipse.server

import System
import Boo.Lang.Interpreter
import Boo.Lang.Interpreter.Builtins
import monolipse.core
import Boo.Lang.Environments

service InterpreterService:
	
	interpreter = InteractiveInterpreter(RememberLastValue: true)
	
	onMessageWithResponse "EVAL":
		using console = ConsoleCapture():
			try:
				interpreter.Eval(message.Payload)
				_ = interpreter.LastValue
				if _ is not null:
					writeLine repr(_)
					interpreter.SetValue("_", _)
			ensure:
				writeLine console.ToString().Trim()

	onMessageWithResponse "GET-INTERPRETER-PROPOSALS":
		proposals = interpreter.SuggestCompletionsFor(message.Payload)
		proposals.Environment.Run:
			writeTypeSystemEntitiesTo proposals.Value, response
