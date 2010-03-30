package monolipse.core.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import monolipse.core.AssemblySourceLanguage;
import monolipse.core.BooCore;
import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblySource;
import monolipse.core.IMonoCompilerLauncher;
import monolipse.core.IMonoLauncher;
import monolipse.core.foundation.WorkspaceUtilities;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public abstract class CompilerLauncher implements IMonoCompilerLauncher {
	
	public static CompilerLauncher createLauncher(AssemblySourceLanguage language) throws IOException {
		if (language.equals(AssemblySourceLanguage.BOO))
			return new BooCompilerLauncher();
		if (language.equals(AssemblySourceLanguage.BOOJAY)) 
			return new BoojayCompilerLauncher();
		return new CSharpCompilerLauncher(language);
	}
	
	public static CompilerLauncher createLauncher(IAssemblySource source) throws IOException {
		CompilerLauncher launcher = createLauncher(source.getLanguage());
		
		launcher.setOutput(source.getOutputFile());
		launcher.setOutputType(source.getOutputType());
		launcher.add(source.getAdditionalOptions().split("\\s+"));
		
		launcher.addReferences(source.getReferences());
		if (source.getLanguage().equals(AssemblySourceLanguage.BOOJAY)) {
			launcher.addReferences(getProjectClasspaths(source));
		} 
		return launcher;
	}

	private static String[] getProjectClasspaths(IAssemblySource source) {
		ArrayList<String> result = new ArrayList<String>();
		
		IJavaProject javaProject = JavaCore.create(source.getFolder().getProject());
		try {
			IClasspathEntry[] classpaths = javaProject.getRawClasspath();
			for (IClasspathEntry classpath: classpaths) {
				if (classpath.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					result.add(classpath.getPath().toOSString());
				}
				if (classpath.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					IClasspathContainer container = JavaCore.getClasspathContainer(classpath.getPath(), javaProject);
					for (IClasspathEntry e: container.getClasspathEntries())
						result.add(e.getPath().toOSString());
				}
			}
		} catch (JavaModelException e) {
			BooCore.logException(e);
		}

		return result.toArray(new String[result.size()]);
	}

	private final IMonoLauncher _launcher;
	private ResponseFile _responseFile;
	
	protected CompilerLauncher(String compiler) throws IOException {
		this(BooCore.createLauncher(compiler));
	}

	protected CompilerLauncher(final IMonoLauncher launcher) throws IOException {
		_launcher = launcher;
		enableDebugging();
		enableResponseFile();
	}

	private void enableResponseFile() throws IOException {
		_responseFile = new ResponseFile();
	}

	public void add(String[] args) {
		for (int i = 0; i < args.length; i++) {
			add(args[i]);
		}
	}
	
	public void add(String arg) {
		if (null != _responseFile) {
			addToResponseFile(arg);
		} else {
			_launcher.add(arg);
		}
	}

	private void addToResponseFile(String arg) {
		try {
			_responseFile.add(arg);
		} catch (IOException e) {
			BooCore.logException(e);
		}
	}
	
	public void setWorkingDir(File workingDir) {
		_launcher.setWorkingDir(workingDir);
	}
	
	public Process launch() throws IOException {
		ensureResponseFile();
		return _launcher.launch();
	}

	private void ensureResponseFile() {
		if (null != _responseFile) {
			_responseFile.close();
			_launcher.add(_responseFile.toString());
			_responseFile = null;
		}
	}
	
	public void enableDebugging() {
		add("-debug+");
	}
	
	public void setPipeline(String pipeline) {
		add("-p:" + pipeline);
	}
	
	public void setOutputType(String outputType) {
		add("-target:" + outputType);
	}
	
	public void setOutput(IResource output) {
		add("-out:" + WorkspaceUtilities.getLocation(output));
	}
	
	public void setOutput(String output) {
		add("-out:" + output);
	}

	public void addReferences(IAssemblyReference[] references) {		
		for (int i=0; i<references.length; ++i) {
			add("-r:" + references[i].getCompilerReference());
		}
	}
		
	public void addReferences(String[] references) {		
		for (int i=0; i<references.length; ++i) {
			add("-r:" + references[i]);
		}
	}

	public void addSourceFiles(IFile[] files) {
		for (int i=0; i<files.length; ++i) {
			add(WorkspaceUtilities.getLocation(files[i]));
		}
	}
	
	public CompilerError[] run() throws IOException {
		Process p = launch();
		ArrayList errors = new ArrayList();
		parseCompilerOutput(p.getErrorStream(), errors);
		parseCompilerOutput(p.getInputStream(), errors);
		return (CompilerError[]) errors.toArray(new CompilerError[errors.size()]);
	}

	private void parseCompilerOutput(InputStream stream, ArrayList errors) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("utf-8")));		
		String line = null;
		while (null != (line = reader.readLine())) {
			
			CompilerError error = parseCompilerError(line);
			if (null != error) errors.add(error);
		}
	}
	
	protected abstract CompilerError parseCompilerError(String line);
}
