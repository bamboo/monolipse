package monolipse.core.internal;

import monolipse.core.BooCore;
import monolipse.core.IAssemblySource;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class BooMarkers {

	static void addMarker(IResource resource, String message, int lineNumber, int severity) {
		try {
			IMarker marker = resource.createMarker(BooMarkers.BOO_PROBLEM_MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
			BooCore.logException(e);
		}
	}

	static void addErrorMarker(IAssemblySource source, String message) {
		addMarker(source.getFolder(), message, -1, IMarker.SEVERITY_ERROR);
	}

	static void deleteMarkers(IResource resource) {
		try {
			resource.deleteMarkers(BooMarkers.BOO_PROBLEM_MARKER_TYPE, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			BooCore.logException(e);
		}
	}

	public static final String BOO_PROBLEM_MARKER_TYPE = BooCore.ID_PLUGIN + ".booProblem";

}
