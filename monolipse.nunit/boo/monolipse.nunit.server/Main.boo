namespace monolipse.nunit.server

import System
import monolipse.core

portNumber, = argv
try:
	client = NetworkProcessMessengerClient()
	NUnitRunner(client)
	client.Start(int.Parse(portNumber))
except x:
	Console.Error.WriteLine(x)