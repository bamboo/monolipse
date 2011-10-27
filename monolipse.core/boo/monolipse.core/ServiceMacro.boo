namespace monolipse.core

import Boo.Lang.Compiler
import Boo.Lang.PatternMatching

macro service:
	case [| service $name |]:
		yield [|
			class $name(AbstractService):
				def constructor(client as ProcessMessengerClient):
					super(client)
					$(service.Body)
		|]
		
macro service.onMessage:
	case [| onMessage $name |]:
		yield [| onMessage($name, { message as Message | $(onMessage.Body) }) |]

macro service.onMessageWithResponse:
	case [| onMessageWithResponse $name |]:
		yield [|
			onMessage $name:
				try:
					$(onMessageWithResponse.Body)
				ensure:
					flush $name + "-RESPONSE"
		|]
