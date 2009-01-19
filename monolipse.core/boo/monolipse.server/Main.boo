namespace monolipse.server

import monolipse.core

portNumber, = argv
client = NetworkProcessMessengerClient()
InterpreterService(client)
CompilerService(client)
client.Start(int.Parse(portNumber))
