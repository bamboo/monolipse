namespace monolipse.core

import System.IO
import System.Threading
import System.Net.Sockets

transient class NetworkProcessMessengerClient(ProcessMessengerClient, System.MarshalByRefObject):
	
	_client as TcpClient
	_reader as TextReader
	_writer as TextWriter
	_handlers = MessageHandlerRegistry()
	_running = false
	
	def OnMessage(name as string, handler as MessageHandler):
		_handlers.Add(name, handler)
	
	def Start(portNumber as int):
		try:
			using _client = Connect(portNumber):
				stream = _client.GetStream()
				using _reader = StreamReader(stream), _writer = StreamWriter(stream):
					_running = true
					MessageLoop()
		ensure:			
			_client = null
			_reader = null
			_writer = null
				
	def Stop():
		if _client is null:
			return
		_client.Close()
		_running = false
		
	[lock]		
	def Send([required] message as Message):
		assert _writer is not null
		try:
			_writer.WriteLine(message.Name)
			_writer.Write(message.Payload)
			_writer.WriteLine(Message.EndMarker)
		ensure:
			_writer.Flush()

	def Send([required] name as string, [required] payload as string):		
		self.Send(Message(Name: name, Payload: payload))
		
	private def Connect(portNumber as int):
		for i in range(3):
			try:
				return TcpClient("127.0.0.1", portNumber)
			except x:
				Thread.Sleep(100ms)
				
	private def MessageLoop():
		while _running:
			message = ReadMessage()
			break if message is null
			if message.Name == "QUIT":
				Send(message)
				break
			DispatchMessage(message)
			
	private def DispatchMessage(message as Message):
		_handlers.Dispatch(message)
	
	private def ReadMessage():
		name = _reader.ReadLine()
		writer = StringWriter()
		while true:
			line = _reader.ReadLine()
			return null if line is null
			break if line == Message.EndMarker
			if line.EndsWith(Message.EndMarker):
				writer.WriteLine(line[:-Message.EndMarker.Length])
			else:
				writer.WriteLine(line)
		return Message(Name: name, Payload: writer.ToString())
