package monolipse.nunit.launching;

import java.io.*;
import java.util.List;

import monolipse.core.IAssemblySource;
import monolipse.core.foundation.*;
import monolipse.core.launching.*;
import monolipse.nunit.NUnitPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.*;


public class TestRunner {
	
	private final IAssemblySource _source;
	private final String _runArguments;
	
	public TestRunner(IAssemblySource source, List<String> testNames) throws CoreException {
		_source = source;
		_runArguments = testNames.isEmpty()
			? assemblyLocation()
			: assemblyLocation() + "," + Strings.commaSeparatedList(testNames);
	}

	private String assemblyLocation() throws CoreException {
		return _source.getOutputFile().getLocation().toOSString();
	}
	
	public void run() throws CoreException, IOException {
		final NUnitPlugin plugin = NUnitPlugin.getDefault();
		
		final ProcessMessenger messenger = new ProcessMessenger(createLaunchConfiguration());
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
					final BufferedReader reader = new BufferedReader(new StringReader(message.payload));
					final String fullName = reader.readLine();
					final String trace = IOUtilities.toString(reader);
					plugin.fireTestFailed(_source, fullName, trace);
				} catch (IOException e) {
					NUnitPlugin.logException(e);
				}
			}
		});
		plugin.fireTestsAboutToStart(_source);
		messenger.send("RUN", _runArguments);
	}
	
	private ILaunchConfiguration createLaunchConfiguration() throws CoreException {
		ILaunchConfigurationType configType = BooLauncher.getLaunchConfigurationType("monolipse.nunit.support");
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, "nunit support");
		wc.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
		return wc;
	}

}
