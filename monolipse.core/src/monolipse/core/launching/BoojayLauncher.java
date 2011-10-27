package monolipse.core.launching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import monolipse.core.BooCore;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;

public class BoojayLauncher {
	
	private static final String LAUNCH_CONFIG_ID = "monolipse.core.launching.boojayLaunchConfigurationType";

	public static ILaunchConfiguration launchConfigurationFor(IFile file, String mode) throws CoreException {
		try {
			return new BoojayLaunchConfigurationBuilder(file, mode).build();
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, BooCore.ID_PLUGIN, e.getMessage(), e));
		}
	}
	
	static class BoojayLaunchConfigurationBuilder {
		
		private final IFile file;
		private final String mainTypeName;
		private final ILaunchConfigurationType configurationType;
		private final String mode;
		
		public BoojayLaunchConfigurationBuilder(IFile file, String mode) {
			this.file = file;
			this.mainTypeName = mainTypeNameFor(file);
			this.configurationType = BooLauncher.getLaunchConfigurationType(LAUNCH_CONFIG_ID);
			this.mode = mode;
		}

		public ILaunchConfiguration build() throws CoreException, IOException {
		
			final ILaunchConfiguration existing = findExistingLaunchConfiguration();
			if (existing != null)
				return existing;
			
			final ILaunchConfigurationWorkingCopy workingCopy = newLaunchConfigurationFor();
			return workingCopy.doSave();
		}

		private ILaunchConfiguration findExistingLaunchConfiguration()
				throws CoreException {
			return BooLauncher.findLaunchConfiguration(configurationType, IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeName);
		}
		
		private ILaunchConfigurationWorkingCopy newLaunchConfigurationFor() throws CoreException, IOException {
			final ILaunchConfigurationWorkingCopy workingCopy = configurationType.newInstance(null, filenameWithoutExtension(file));
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpathFor(file));
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeName);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, file.getProject().getName());
			if (isDebugMode())
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, true);
			return workingCopy;
		}

		private boolean isDebugMode() {
			return "debug".equals(mode);
		}
	
		private static List<String> classpathFor(IFile file) throws CoreException, IOException {
			final List<String> classPath = new ArrayList<String>();
			classPath.add(JavaRuntime.newDefaultProjectClasspathEntry(javaProjectFor(file)).getMemento());
			classPath.add(JavaRuntime.newArchiveRuntimeClasspathEntry(boojayLangJarPath()).getMemento());
			return classPath;
		}

		private static IJavaProject javaProjectFor(IFile file) throws CoreException {
			final IProject project = file.getProject();
			return JavaCore.create(project);
		}

		private static Path boojayLangJarPath() throws IOException {
			return new Path(BooCore.resolveBundlePath("lib/boojay/boojay.lang.jar"));
		}
	
		private static String mainTypeNameFor(IFile file) {
			return filenameWithoutExtension(file) + "Module";
		}
	
		private static String filenameWithoutExtension(IFile file) {
			return file.getFullPath().removeFileExtension().lastSegment();
		}
	}
}
