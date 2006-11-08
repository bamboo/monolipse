package monolipse.core.launching;

import monolipse.core.IAssemblySource;
import monolipse.core.IBooLaunchConfigurationConstants;
import monolipse.core.IBooLaunchConfigurationTypes;
import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.core.internal.BooAssemblySource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;


public class BooLauncher {

	public static ILaunchConfiguration getAppLaunchConfiguration(
			IAssemblySource source) throws CoreException {
		ILaunchConfiguration configuration = findAppLaunchConfiguration(source);
		return null == configuration
				? createAppLaunchConfiguration(source)
				: configuration;
	}

	private static ILaunchConfiguration createAppLaunchConfiguration(IAssemblySource source) throws CoreException {
		final ILaunchConfigurationType configType = getAppLaunchConfigType();
		return createAssemblySourceLaunchConfiguration(source, configType);
	}

	public static ILaunchConfiguration createAssemblySourceLaunchConfiguration(IAssemblySource source, final ILaunchConfigurationType configType) throws CoreException {
		return createResourceLaunchConfiguration(configType,
				IBooLaunchConfigurationConstants.ATTR_ASSEMBLY_SOURCE_PATH, source.getFolder());
	}
	
	public static IAssemblySource getConfiguredAssemblySource(ILaunchConfiguration configuration) throws CoreException {
		return BooAssemblySource.get(getAssemblySourceFolder(configuration));
	}

	private static IFolder getAssemblySourceFolder(ILaunchConfiguration configuration)
			throws CoreException {
		return WorkspaceUtilities.getFolder(configuration.getAttribute(
				IBooLaunchConfigurationConstants.ATTR_ASSEMBLY_SOURCE_PATH, ""));
	}

	private static ILaunchConfiguration findAppLaunchConfiguration(
			IAssemblySource source) throws CoreException {
		final ILaunchConfigurationType configType = getAppLaunchConfigType();
		return findAssemblySourceLaunchConfiguration(source, configType);
	}

	public static ILaunchConfiguration findAssemblySourceLaunchConfiguration(IAssemblySource source, final ILaunchConfigurationType configType) throws CoreException {
		return findLaunchConfiguration(configType,
				IBooLaunchConfigurationConstants.ATTR_ASSEMBLY_SOURCE_PATH,
				WorkspaceUtilities.getPortablePath(source.getFolder()));
	}

	public static ILaunchConfiguration getScriptLaunchConfiguration(IFile file)
			throws CoreException {
		ILaunchConfiguration configuration = findScriptLaunchConfiguration(file);
		return null == configuration
				? createScriptConfiguration(file)
				: configuration;
	}

	private static ILaunchConfiguration createScriptConfiguration(IFile file)
			throws CoreException {
		return createResourceLaunchConfiguration(getScriptLaunchConfigType(),
				IBooLaunchConfigurationConstants.ATTR_SCRIPT_PATH, file);
	}

	private static ILaunchConfiguration createResourceLaunchConfiguration(
			ILaunchConfigurationType configType, String pathAttributeName,
			IResource resource) throws CoreException {
		String path = WorkspaceUtilities.getPortablePath(resource);
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
				generateUniqueLaunchConfigurationName(path));
		wc.setAttribute(pathAttributeName, path);
		wc.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
		return wc.doSave();
	}

	private static String generateUniqueLaunchConfigurationName(String path) {
		return DebugPlugin.getDefault().getLaunchManager()
				.generateUniqueLaunchConfigurationNameFrom(path);
	}

	private static ILaunchConfiguration findScriptLaunchConfiguration(IFile file)
			throws CoreException {
		return findLaunchConfiguration(getScriptLaunchConfigType(),
				IBooLaunchConfigurationConstants.ATTR_SCRIPT_PATH,
				WorkspaceUtilities.getPortablePath(file));
	}

	private static ILaunchConfiguration findLaunchConfiguration(
			ILaunchConfigurationType configType, String attributeName,
			String attributeValue) throws CoreException {
		ILaunchConfiguration[] existing = getLaunchManager()
				.getLaunchConfigurations(configType);
		for (int i = 0; i < existing.length; ++i) {
			if (attributeValue.equals(existing[i].getAttribute(attributeName,
					""))) {
				return existing[i];
			}
		}
		return null;
	}

	private static ILaunchConfigurationType getAppLaunchConfigType() {
		return getLaunchConfigurationType(IBooLaunchConfigurationTypes.ID_BOO_APP);
	}

	private static ILaunchConfigurationType getScriptLaunchConfigType() {
		return getLaunchConfigurationType(IBooLaunchConfigurationTypes.ID_BOO_SCRIPT);
	}

	public static ILaunchConfigurationType getLaunchConfigurationType(
			String configTypeId) {
		return getLaunchManager().getLaunchConfigurationType(configTypeId);
	}

	private static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public static String getProcessMessengerPort(ILaunchConfiguration configuration) throws CoreException {
		return Integer.toString(configuration.getAttribute(IBooLaunchConfigurationConstants.ATTR_PROCESS_MESSENGER_PORT, 0xB00));
	}

}
