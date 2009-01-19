namespace monolipse.core

import Boo.Lang.Compiler
import Boo.Lang.Compiler.Ast
import Boo.Lang.PatternMatching

macro service:
	match service:
		case [| service $name |]:
			yield [|
				class $name(AbstractService):
					def constructor(client as ProcessMessengerClient):
						super(client)
						$(service.Body)
			|]
		
macro onMessage:
	match onMessage:
		case [| onMessage $name |]:
			yield [| onMessage($name, { message as Message | $(onMessage.Body) }) |]

macro onMessageWithResponse:
	match onMessageWithResponse:
		case [| onMessageWithResponse $name |]:
			yield [|
				onMessage $name:
					try:
						$(onMessageWithResponse.Body)
					except x:
						System.Console.Error.WriteLine(x)
					ensure:
						flush $name + "-RESPONSE"
			|]
