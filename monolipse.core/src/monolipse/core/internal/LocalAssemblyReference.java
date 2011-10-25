package monolipse.core.internal;

import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblyReferenceVisitor;
import monolipse.core.ILocalAssemblyReference;
import monolipse.core.IMemorable;
import monolipse.core.IRemembrance;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class LocalAssemblyReference implements ILocalAssemblyReference {
	
	IFile _reference;

	LocalAssemblyReference(IFile reference) {
		_reference = reference;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(IFile.class)) {
			return _reference;
		}
		return null;
	}

	public String getAssemblyName() {
		return _reference.getName();
	}

	public IFile getFile() {
		return _reference;
	}

	public String getCompilerReference() {
		return _reference.getLocation().toOSString();
	}

	public String getType() {
		return IAssemblyReference.LOCAL;
	}
	
	static public class Remembrance implements IRemembrance {
		
		public String path;

		public Remembrance(String path) {
			this.path = path;
		}
		
		/**
		 * public no arg constructor for xstream deserialization
		 * on less capable virtual machines.
		 */
		public Remembrance() {
		}

		public IMemorable activate() throws CoreException {
			return BooAssemblyReference.get(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(this.path)));
		}
	}
	
	public IRemembrance getRemembrance() {
		return new Remembrance(_reference.getFullPath().toPortableString());
	}

	public boolean accept(IAssemblyReferenceVisitor visitor) throws CoreException {
		return visitor.visit(this);
	}
}
