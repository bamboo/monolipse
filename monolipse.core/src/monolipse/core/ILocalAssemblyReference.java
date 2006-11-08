package monolipse.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;

public interface ILocalAssemblyReference extends IAssemblyReference, IAdaptable {
	IFile getFile();
}
