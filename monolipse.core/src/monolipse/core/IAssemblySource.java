package monolipse.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IAssemblySource extends IAdaptable {
	
	public interface OutputType {
		public static final String CONSOLE_APPLICATION = "exe";
		public static final String WINDOWS_APPLICATION = "winexe";
		public static final String LIBRARY = "library";
	}

	IFolder getFolder();

	IFile[] getSourceFiles() throws CoreException;

	void setReferences(IAssemblyReference... references);

	IAssemblyReference[] getReferences();
	
	boolean visitReferences(IAssemblyReferenceVisitor visitor) throws CoreException;

	String getOutputType();

	void setOutputType(String outputType);
	
	AssemblySourceLanguage getLanguage();
	
	void setLanguage(AssemblySourceLanguage language);

	IFile getOutputFile() throws CoreException;
	
	void setOutputFolder(IFolder folder);

	IFolder getOutputFolder() throws CoreException;
	
	void refresh(IProgressMonitor monitor) throws CoreException;

	void save(IProgressMonitor monitor) throws CoreException;

	String getAdditionalOptions();

	void setAdditionalOptions(String additionalOptions);

	boolean hasOutputFolder();
}