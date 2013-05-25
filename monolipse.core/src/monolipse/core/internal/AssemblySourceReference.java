package monolipse.core.internal;

import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblyReferenceVisitor;
import monolipse.core.IAssemblySource;
import monolipse.core.IAssemblySourceReference;
import monolipse.core.IMemorable;
import monolipse.core.IRemembrance;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class AssemblySourceReference implements IAssemblySourceReference {
	
	private IAssemblySource _source;

	AssemblySourceReference(IAssemblySource source) {
		_source = source;
	}

	public IAssemblySource getAssemblySource() {
		return _source;
	}

	public String getAssemblyName() {
		return _source.getFolder().getName();
	}

	public String getCompilerReference() throws CoreException {
		return _source.getOutputFile().getLocation().toOSString();
	}

	public String getType() {
		return IAssemblyReference.ASSEMBLY_SOURCE;
	}

	public boolean accept(IAssemblyReferenceVisitor visitor)
			throws CoreException {
		return visitor.visit(this);
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
		
		@Override
		public String toString() {
			return path;
		}

		public IMemorable activate() throws CoreException {
			IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(this.path));
			BooAssemblySource source = BooAssemblySource.get(folder);
			return AssemblyReferences.assemblyReferenceFor(source);
		}
	}

	public IRemembrance getRemembrance() {
		return new Remembrance(_source.getFolder().getFullPath().toPortableString());
	}

}
