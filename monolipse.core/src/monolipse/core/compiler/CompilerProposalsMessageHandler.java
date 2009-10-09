package monolipse.core.compiler;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;

import monolipse.core.BooCore;
import monolipse.core.launching.*;


public class CompilerProposalsMessageHandler implements IProcessMessageHandler {
	private static final CompilerProposal[] EMPTY = new CompilerProposal[0];
	private final Exchanger<ArrayList<CompilerProposal>> _proposalExchanger = new Exchanger();
	
	public CompilerProposal[] getProposals() {
		final ArrayList<CompilerProposal> proposals = exchange(null);
		return null == proposals
			? EMPTY
			: proposals.toArray(EMPTY);
	}

	public void handle(ProcessMessage response) {
		final ArrayList proposals = new ArrayList();
		collectProposals(proposals, response);
		exchange(proposals);
	}

	private ArrayList<CompilerProposal> exchange(final ArrayList proposals) {
		try {
			 return _proposalExchanger.exchange(proposals, 3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			BooCore.logException(e);
		} catch (TimeoutException e) {
			BooCore.logException(e);
		}
		return null;
	}

	private void collectProposals(final ArrayList resultingProposals,
			ProcessMessage response) {
		String messagePayload = response.payload;
		BufferedReader reader = new BufferedReader(new StringReader(messagePayload));	
		try {
			String line;
			while (null != (line = reader.readLine())) {
				String[] parts = line.split(":");
				switch (parts.length) {
				case 3:
					resultingProposals.add(new CompilerProposal(parts[0], parts[1], parts[2]));
					break;
				case 2:
					resultingProposals.add(new CompilerProposal(parts[0], parts[1], parts[1]));
					break;
				default:
					BooCore.logInfo("Invalid proposal: " + line);
				}
			}
		} catch (IOException unexpected) {
			BooCore.logException(unexpected);
		}
	}
}
