package monolipse.core.internal;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import monolipse.core.BooCore;
import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblyReferenceVisitor;
import monolipse.core.IBooAssemblyReference;
import monolipse.core.IMemorable;
import monolipse.core.IRemembrance;

public class BooAssemblyReference implements IBooAssemblyReference, IRemembrance {
	
	private String assemblyName;
	
	/**
	 * For serialization only.
	 */
	public BooAssemblyReference() {
	}

	public BooAssemblyReference(String booAssemblyName) {
		this.assemblyName = booAssemblyName;
	}
	
	@Override
	public int hashCode() {
		return assemblyName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof BooAssemblyReference
			? ((BooAssemblyReference)obj).assemblyName.equals(assemblyName)
			: false;
	}

	@Override
	public IRemembrance getRemembrance() {
		return this;
	}

	@Override
	public String getAssemblyName() {
		return assemblyName;
	}

	@Override
	public String getCompilerReference() {
		try {
			return BooCore.resolveBundlePath("lib/boojay/" + assemblyName + ".dll");
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getType() {
		return IAssemblyReference.BOO_LIB;
	}

	@Override
	public boolean accept(IAssemblyReferenceVisitor visitor)
			throws CoreException {
		return visitor.visit(this);
	}
	
	public IMemorable activate() throws CoreException {
		return this;
	}
}
