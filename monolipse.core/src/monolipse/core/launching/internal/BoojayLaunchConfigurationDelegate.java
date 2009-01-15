package monolipse.core.launching.internal;

import monolipse.core.foundation.WorkspaceUtilities;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.launching.*;
import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

public class BoojayLaunchConfigurationDelegate extends JavaLaunchDelegate {
	
	private static final class BoojaySourceLocator extends JavaSourceLocator {
		public BoojaySourceLocator(IJavaProject create) throws CoreException {
			super(create);
		}

		public Object getSourceElement(IStackFrame stackFrame) {
			try {
				final IFile file = WorkspaceUtilities.getFileForLocation(((IJavaStackFrame)stackFrame).getSourceName());
				if (file != null)
					return file;
			} catch (DebugException e) {
				e.printStackTrace();
			}
			return super.getSourceElement(stackFrame);
		}
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		launch.setSourceLocator(new BoojaySourceLocator(javaProjectFor(configuration)));
		super.launch(configuration, mode, launch, monitor);
	}

	private IJavaProject javaProjectFor(ILaunchConfiguration configuration)
			throws CoreException {
		return JavaCore.create(projectFor(configuration));
	}

	private IProject projectFor(ILaunchConfiguration configuration)
			throws CoreException {
		final String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		return WorkspaceUtilities.getWorkspaceRoot().getProject(projectName);
	}
}
