package monolipse.core.interpreter;

import java.io.IOException;

import monolipse.core.compiler.AbstractBooServiceClient;
import monolipse.core.launching.IProcessMessageHandler;
import monolipse.core.launching.ProcessMessage;

import org.eclipse.core.runtime.CoreException;


public class InteractiveInterpreter extends AbstractBooServiceClient {
	
	IInterpreterListener _listener;
	
	public InteractiveInterpreter() throws CoreException {
		setMessageHandler("EVAL-FINISHED", new IProcessMessageHandler() {
			public void handle(ProcessMessage message) {
				if (null == _listener) return;
				_listener.evalFinished(message.payload);
			}
		});
	}
	
	public void eval(String code) throws IOException {
		send("EVAL", code);
	}

	protected String getProposalsResponseMessageId() {
		return "INTERPRETER-PROPOSALS";
	}

	protected String getProposalsMessageId() {
		return "GET-INTERPRETER-PROPOSALS";
	}

	public void addListener(IInterpreterListener listener) {
		if (null != _listener) throw new IllegalStateException("only a single listener is supported");
		_listener = listener;
	}
	
}
