package monolipse.core.foundation;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

public class Projects {

	public static void ensureHasJavaNature(IProject project) throws CoreException {
		if (project.hasNature(JavaCore.NATURE_ID))
			return;
		
		final IProjectDescription description = project.getDescription();
		description.setNatureIds(ArrayUtilities.append(description.getNatureIds(), JavaCore.NATURE_ID));
		project.setDescription(description, null);
	}

}
