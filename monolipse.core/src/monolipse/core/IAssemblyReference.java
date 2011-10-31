package monolipse.core;

import org.eclipse.core.runtime.CoreException;


public interface IAssemblyReference extends IMemorable {
	
	public static final String LOCAL = "local";
	
	public static final String GAC = "gac";
	
	public static final String ASSEMBLY_SOURCE = "assembly source";

	public static final String BOO_LIB = "boo library";

	/**
	 * Assembly friendly name.
	 * @return assembly friendly name
	 */
	String getAssemblyName();
	
	/**
	 * Returns a string representation of the reference suitable to be
	 * passed as a command line argument to the compiler.
	 * 
	 * @return string representation of this reference
	 */
	String getCompilerReference();

	String getType();
	
	boolean accept(IAssemblyReferenceVisitor visitor) throws CoreException;
}
