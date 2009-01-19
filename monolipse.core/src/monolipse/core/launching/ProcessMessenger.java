package monolipse.core.launching;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import monolipse.core.BooCore;
import monolipse.core.IBooLaunchConfigurationConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.*;


public class ProcessMessenger {
	
	private final Object _socketMutex = new Object();
	
	private Socket _socket;
	
	private final Map _handlers = new HashMap();

	private ILaunchConfiguration _configuration;
	
	private int _timeout = 8000;
	
	public ProcessMessenger(ILaunchConfiguration configuration) {
		_configuration = configuration;
	}
	
	public void setTimeout(int timeout) {
		_timeout = timeout;
	}
	
	public int getTimeout() {
		return _timeout;
	}
	
	public synchronized void setMessageHandler(String messageName, IProcessMessageHandler handler) {
		if (null == handler) {
			_handlers.remove(messageName);
		} else {
			_handlers.put(messageName, handler);
		}
	}
	
	public void send(String name, String payload) throws IOException {
		if (null == name || null == payload) throw new IllegalArgumentException();
		send(new ProcessMessage(name, payload));
	}
	
	public void send(ProcessMessage message) throws IOException {
		synchronized (_socketMutex) {
			if (null == _socket) {
				launch();
				try {
					// always wait a little longer
					// than the socket timeout
					_socketMutex.wait(_timeout*2);
				} catch (InterruptedException x) {
					BooCore.logException(x);
					throw new RuntimeException(x);
				}
				if (null == _socket) {
					throw new IOException("no connection from process");
				}
			}
			doSend(message);
		}
	}

	private void doSend(ProcessMessage message) throws IOException {
		final BufferedWriter buffered = buffered(_socket.getOutputStream());
		try {
			message.writeTo(buffered);
		} finally {
			buffered.flush();
		}
	}

	public void unload() {
		synchronized (_socketMutex) {
			if (null == _socket) return;
			try {
				doSend(new ProcessMessage("QUIT", ""));
				try {
					_socketMutex.wait(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				BooCore.logException(e);
			}
			finally {
				if (null != _socket) {
					try { _socket.close(); } catch (IOException e) {}
					_socket = null;
				}
			}
		}
	}
	
	public void dispose() {
		try {
			unload();
		} catch (Exception x) {
			BooCore.logException(x);
		}
	}
	
	private void launch() throws IOException {
		
		final String jobName = "ProcessMessenger [" + _configuration.getName() + "]";
		final int portNumber = findAvailablePort();
		
		final Job server = new Job(jobName) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					listen(monitor, portNumber);
				} catch (Exception x) {
					BooCore.logException(x);
				}
				return Status.OK_STATUS;
			}			
		};
		server.setPriority(Job.LONG);
		server.setSystem(true);
		server.schedule();
		
		final Job client = new Job(jobName + " client") 	{
			protected IStatus run(IProgressMonitor monitor) {
				try {
					BooCore.logInfo(jobName + " using port " + Integer.toHexString(portNumber));
					launchConfiguration(portNumber, monitor);
				} catch (Exception x) {
					BooCore.logException(x);
				}
				return Status.OK_STATUS;
			}
			
			private void launchConfiguration(int portNumber, IProgressMonitor monitor) throws CoreException {
				ILaunchConfigurationWorkingCopy workingCopy = _configuration.getWorkingCopy();
				workingCopy.setAttribute(IBooLaunchConfigurationConstants.ATTR_PROCESS_MESSENGER_PORT, portNumber);
				workingCopy.launch("run", monitor);
			}
		};
		client.schedule();
	}
	
	private int findAvailablePort() {
		final int begin = 0x1B00;
		final int end = 0x1BFF;
		int i=begin;
		for (; i<=end; ++i) {
			if (isPortAvailable(i)) return i;
		}
		return i;
	}
	
	private static boolean isPortAvailable(int port) {
		try {
			new ServerSocket(port, 1, localhost()).close();
			return true;
		} catch (Exception ioe) {
		}
		return false;
	}
	
	private void listen(IProgressMonitor monitor, int portNumber) {
		try {
			ServerSocket server = new ServerSocket(portNumber, 50, localhost());
			server.setSoTimeout(_timeout);
			try {
				synchronized (_socketMutex) {
					_socket = server.accept();
					_socketMutex.notifyAll();
				}
				try {
					while (!monitor.isCanceled()) {
						ProcessMessage message = readMessage(monitor);
						if (null == message) break;
						if (message.name.equals("QUIT")) {
							synchronized (_socketMutex) {
								_socketMutex.notifyAll();
							}
							break;
						}
						handle(message);
					}
				} finally {
					unload();
				}
			} finally {
				server.close();
			}
		} catch (IOException e) {
			BooCore.logException(e);
		}
	}

	private static InetAddress localhost() throws UnknownHostException {
		return InetAddress.getByName("127.0.0.1");
	}
	
	private synchronized void handle(final ProcessMessage message) {
		final IProcessMessageHandler handler = (IProcessMessageHandler) _handlers.get(message.name); 
		if (null == handler) return;
		SafeRunner.run(new ISafeRunnable() {
			public void handleException(Throwable exception) {
				BooCore.logException(exception);
			}

			public void run() throws Exception {
				handler.handle(message);
			}
		});
	}

	private ProcessMessage readMessage(IProgressMonitor monitor) throws IOException {
		BufferedReader reader = buffered(_socket.getInputStream());
		StringWriter buffer = new StringWriter();
		PrintWriter writer = new PrintWriter(buffer);
		String name = reader.readLine();
		while (true) {
			if (monitor.isCanceled()) return null;
			String line = reader.readLine();
			if (null == line) return null;
			if (line.equals(ProcessMessage.END_MARKER)) break;
			if (line.endsWith(ProcessMessage.END_MARKER)) {
				writer.println(line.substring(0, line.length() - ProcessMessage.END_MARKER.length()));
				break;
			}	
			writer.println(line);
		}
		return new ProcessMessage(name, buffer.getBuffer().toString());
	}

	private BufferedWriter buffered(final OutputStream outputStream) {
		return new BufferedWriter(new OutputStreamWriter(outputStream));
	}

	private BufferedReader buffered(final InputStream inputStream) {
		return new BufferedReader(new InputStreamReader(inputStream));
	}		
}
