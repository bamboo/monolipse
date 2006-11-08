namespace monolipse.server

import monolipse.core

portNumber, = argv
client = ProcessMessengerClient()
InterpreterService(client)
CompilerService(client)
client.Start(int.Parse(portNumber))
