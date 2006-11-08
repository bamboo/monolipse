package monolipse.core.compiler;

import java.io.IOException;

import monolipse.core.BooCore;
import monolipse.core.IBooLaunchConfigurationTypes;
import monolipse.core.launching.BooLauncher;
import monolipse.core.launching.IProcessMessageHandler;
import monolipse.core.launching.ProcessMessage;
import monolipse.core.launching.ProcessMessenger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;


public abstract class AbstractBooServiceClient {
	
	private ProcessMessenger _messenger;
	
	private CompilerProposalsMessageHandler _proposalsHandler = new CompilerProposalsMessageHandler();
	
	public AbstractBooServiceClient() throws CoreException {
		_messenger = new ProcessMessenger(createLaunchConfiguration());
		_messenger.setMessageHandler(getProposalsResponseMessageId(), _proposalsHandler);
	}
	
	public CompilerProposal[] getCompletionProposals(String code) throws IOException {
		
		CompilerProposal[] proposals = null;
		
		Object lock = _proposalsHandler.getMessageLock();
		synchronized (lock) {
			try {
				_messenger.send(createMessage(getProposalsMessageId(), code));
				lock.wait(_messenger.getTimeout());
				proposals = _proposalsHandler.getProposals();
			} catch (Exception e) {
				BooCore.logException(e);
			}
		}
		return proposals;
	}
	
	public void unload() {
		try {
			_messenger.unload();
		} catch (Exception x) {
			BooCore.logException(x);
		}
	}

	public void dispose() {
		_messenger.dispose();
	}
	
	protected abstract String getProposalsMessageId();
	
	protected abstract String getProposalsResponseMessageId();
	
	protected ProcessMessage createMessage(String name, String code) {
		return new ProcessMessage(name, code);
	}
	
	protected void setMessageHandler(String messageName, IProcessMessageHandler handler) {
		_messenger.setMessageHandler(messageName, handler);
	}
	
	protected void send(String messageName, String payload) throws IOException {
//		BooCore.logInfo(messageName + ":" + payload);
		_messenger.send(messageName, payload);
	}
	
	public static ILaunchConfiguration createLaunchConfiguration() throws CoreException {
		ILaunchConfigurationType configType = BooLauncher.getLaunchConfigurationType(IBooLaunchConfigurationTypes.ID_INTERPRETER_SUPPORT);
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, "interpreter support");
		wc.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
		return wc;
	}

}
