package monolipse.core.launching;

import java.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.*;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.*;

public class BoojayLauncher {
	
	private static final String LAUNCH_CONFIG_ID = "monolipse.core.launching.boojayLaunchConfigurationType";

	public static ILaunchConfiguration launchConfigurationFor(IFile file) throws CoreException {
//		final IFolder outputFolder = BooCore.assemblySourceContaining(file).getOutputFolder();
		
		return new BoojayLaunchConfigurationBuilder(file).build();
	}
	
	static class BoojayLaunchConfigurationBuilder {
		
		private final IFile file;
		private String mainTypeName;
		private ILaunchConfigurationType configurationType;
		
		public BoojayLaunchConfigurationBuilder(IFile file) {
			this.file = file;
			this.mainTypeName = mainTypeNameFor(file);
			this.configurationType = BooLauncher.getLaunchConfigurationType(LAUNCH_CONFIG_ID);
		}

		public ILaunchConfiguration build() throws CoreException {
		
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
		
		private ILaunchConfigurationWorkingCopy newLaunchConfigurationFor() throws CoreException {
			final ILaunchConfigurationWorkingCopy workingCopy = configurationType.newInstance(null, filenameWithoutExtension(file));
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpathFor(file));
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeName);
			return workingCopy;
		}
	
		private static List<String> classpathFor(IFile file) throws CoreException {
			final IRuntimeClasspathEntry projectClasspath = JavaRuntime.newDefaultProjectClasspathEntry(JavaCore.create(file.getProject()));
			
			final List<String> classPath = new ArrayList<String>();
			classPath.add(projectClasspath.getMemento());
			return classPath;
		}
	
		private static String mainTypeNameFor(IFile file) {
			return filenameWithoutExtension(file) + "Module";
		}
	
		private static String filenameWithoutExtension(IFile file) {
			return file.getFullPath().removeFileExtension().lastSegment();
		}
	}

}
