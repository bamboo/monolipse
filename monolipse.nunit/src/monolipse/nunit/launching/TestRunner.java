package monolipse.nunit.launching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import monolipse.core.IAssemblySource;
import monolipse.core.launching.BooLauncher;
import monolipse.core.launching.IProcessMessageHandler;
import monolipse.core.launching.ProcessMessage;
import monolipse.core.launching.ProcessMessenger;
import monolipse.nunit.NUnitPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;


public class TestRunner {
	
	private IAssemblySource _source;
	
	public TestRunner(IAssemblySource source) {
		_source = source;
	}

	public void run() throws CoreException, IOException {
		final IFile outputFile = _source.getOutputFile();
		final NUnitPlugin plugin = NUnitPlugin.getDefault();
		
		ProcessMessenger messenger = new ProcessMessenger(createLaunchConfiguration());
		messenger.setMessageHandler("TESTS-STARTED",  new IProcessMessageHandler() {
			public void handle(ProcessMessage message) {
				int count = Integer.parseInt(message.payload.trim());
				plugin.fireTestsStarted(_source, count);
			}
		});
		messenger.setMessageHandler("TESTS-FINISHED",  new IProcessMessageHandler() {
			public void handle(ProcessMessage message) {
				plugin.fireTestsFinished(_source);
			}
		});
		messenger.setMessageHandler("TEST-STARTED",  new IProcessMessageHandler() {
			public void handle(ProcessMessage message) {
				plugin.fireTestStarted(_source, message.payload.trim());
			}
		});
		messenger.setMessageHandler("TEST-FAILED",  new IProcessMessageHandler() {
			public void handle(ProcessMessage message) {
				try {
					BufferedReader reader = new BufferedReader(new StringReader(message.payload));
					String fullName = reader.readLine();
					StringWriter buffer = new StringWriter();
					PrintWriter writer = new PrintWriter(buffer);
					String frame = null;
						while (null != (frame = reader.readLine())) {
							writer.println(frame);
						}
					String trace = buffer.getBuffer().toString();
					plugin.fireTestFailed(_source, fullName, trace);
				} catch (IOException e) {
					NUnitPlugin.logException(e);
				}
			}
		});
		plugin.fireTestsAboutToStart(_source);
		messenger.send("RUN", outputFile.getLocation().toOSString());
	}
	
	private ILaunchConfiguration createLaunchConfiguration() throws CoreException {
		ILaunchConfigurationType configType = BooLauncher.getLaunchConfigurationType("monolipse.nunit.support");
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, "nunit support");
		wc.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
		return wc;
	}

}
