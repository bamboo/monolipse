package monolipse.core.launching.internal;

import monolipse.core.foundation.WorkspaceUtilities;

import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

public class BoojayLaunchConfigurationDelegate extends JavaLaunchDelegate {
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		launch.setSourceLocator(new ISourceLocator() {
			public Object getSourceElement(IStackFrame stackFrame) {
				try {
					return WorkspaceUtilities.getFileForLocation(((IJavaStackFrame)stackFrame).getSourceName());
				} catch (DebugException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
		
		super.launch(configuration, mode, launch, monitor);
	}

}
