package monolipse.core.launching.internal;

import java.io.IOException;

import monolipse.core.BooCore;
import monolipse.core.IAssemblySource;
import monolipse.core.AssemblySourceLanguage;
import monolipse.core.IBooLaunchConfigurationConstants;
import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.core.runtime.CompilerLauncher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;


public class BooScriptLaunchConfigurationDelegate extends AbstractBooLaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {

			launch.addProcess(DebugPlugin.newProcess(launch,
					launchScript(configuration), configuration.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Process launchScript(ILaunchConfiguration configuration)
			throws CoreException, IOException {
		IFile scriptFile = getScriptFile(configuration);

		CompilerLauncher launcher = CompilerLauncher.createLauncher(AssemblySourceLanguage.BOO);
		launcher.setPipeline("run");
		launcher.addSourceFiles(new IFile[] { scriptFile });
		launcher.setWorkingDir(scriptFile.getParent().getLocation().toFile());

		IAssemblySource container = BooCore.assemblySourceContaining(scriptFile);
		if (null != container) launcher.addReferences(container.getReferences());
		return launcher.launch();
	}

	private IFile getScriptFile(ILaunchConfiguration configuration)
			throws CoreException {
		return WorkspaceUtilities.getFile(configuration.getAttribute(
				IBooLaunchConfigurationConstants.ATTR_SCRIPT_PATH, ""));
	}

}
