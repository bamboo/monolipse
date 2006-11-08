package monolipse.core.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import monolipse.core.BooCore;
import monolipse.core.launching.IProcessMessageHandler;
import monolipse.core.launching.ProcessMessage;

public class CompilerProposalsMessageHandler implements IProcessMessageHandler {
	private final ArrayList _proposals;

	public CompilerProposalsMessageHandler() {
		this._proposals = new ArrayList();
	}
	
	public Object getMessageLock() {
		return _proposals;
	}
	
	public CompilerProposal[] getProposals() {
		synchronized (getMessageLock()) {
			return (CompilerProposal[]) _proposals.toArray(new CompilerProposal[_proposals.size()]);
		}
	}

	public void handle(ProcessMessage response) {
		Object lock = getMessageLock();
		synchronized (lock) {
			_proposals.clear();
			
			String messagePayload = response.payload;
			BufferedReader reader = new BufferedReader(new StringReader(messagePayload));	
			String line;
			try {
				while (null != (line = reader.readLine())) {
					String[] parts = line.split(":");
					_proposals.add(new CompilerProposal(parts[0], parts[1], parts[2]));
				}
			} catch (IOException unexpected) {
				BooCore.logException(unexpected);
			}
			lock.notify();
		}
	}
}
