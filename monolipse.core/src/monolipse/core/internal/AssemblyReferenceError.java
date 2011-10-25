package monolipse.core.internal;

import org.eclipse.core.runtime.CoreException;

import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblyReferenceVisitor;
import monolipse.core.IRemembrance;

public class AssemblyReferenceError implements IAssemblyReference {

	private final CoreException _error;
	private final IRemembrance _remembrance;

	public AssemblyReferenceError(CoreException e, IRemembrance ref) {
		this._error = e;
		this._remembrance = ref;
	}

	@Override
	public IRemembrance getRemembrance() {
		return _remembrance;
	}

	@Override
	public String getAssemblyName() {
		return error().getMessage();
	}

	@Override
	public String getCompilerReference() {
		return null;
	}

	@Override
	public String getType() {
		return "error";
	}

	@Override
	public boolean accept(IAssemblyReferenceVisitor visitor) throws CoreException {
		return true;
	}

	public CoreException error() {
		return _error;
	}
}
