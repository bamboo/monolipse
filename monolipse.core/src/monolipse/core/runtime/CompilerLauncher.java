package monolipse.core.runtime;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import monolipse.core.*;
import monolipse.core.foundation.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;

public abstract class CompilerLauncher implements IMonoCompilerLauncher {
	
	public static CompilerLauncher createLauncher(AssemblySourceLanguage language) throws IOException {
		if (language.equals(AssemblySourceLanguage.BOO))
			return new BooCompilerLauncher();
		if (language.equals(AssemblySourceLanguage.BOOJAY))
			return new BoojayCompilerLauncher();
		return new CSharpCompilerLauncher(language);
	}
	
	public static CompilerLauncher createLauncher(IAssemblySource source) throws IOException, CoreException {
		CompilerLauncher launcher = createLauncher(source.getLanguage());
		
		launcher.setOutput(source.getOutputFile());
		launcher.setOutputType(source.getOutputType());
		launcher.add(source.getAdditionalOptions().split("\\s+"));
		
		launcher.addReferences(source.getReferences());
		if (source.getLanguage().equals(AssemblySourceLanguage.BOOJAY)) {
			launcher.addReferences(classpathEntryReferencesFor(source));
		}
		return launcher;
	}

	private static String[] classpathEntryReferencesFor(IAssemblySource source) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			addClasspathEntriesTo(result, javaProjectFor(source).getResolvedClasspath(true));
		} catch (JavaModelException e) {
			BooCore.logException(e);
		}
		return result.toArray(new String[result.size()]);
	}

	private static IJavaProject javaProjectFor(IAssemblySource source) {
		return JavaCore.create(source.getFolder().getProject());
	}

	private static void addClasspathEntriesTo(ArrayList<String> result, IClasspathEntry[] classpaths) {
		for (IClasspathEntry classpath: classpaths)
			addClasspathEntryTo(result, classpath);
	}

	private static void addClasspathEntryTo(ArrayList<String> result, IClasspathEntry classpath) {
		int entryKind = classpath.getEntryKind();
		if (entryKind == IClasspathEntry.CPE_LIBRARY) {
			String ref = classpath.getPath().toOSString();
			logInfo("Classpath " + classpath + " referenced as '" + ref + "'");
			result.add(ref);
			return;
		}
		logInfo("Skipping " + classpath);
	}

	private static void logInfo(String message) {
		BooCore.logInfo(message);
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

	public void addReferences(IAssemblyReference[] references) throws CoreException {		
		for (IAssemblyReference ref : references)
			add("-r:" + ref.getCompilerReference());
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
		ArrayList<CompilerError> errors = new ArrayList<CompilerError>();
		parseCompilerOutput(p.getErrorStream(), errors);
		parseCompilerOutput(p.getInputStream(), errors);
		return errors.toArray(new CompilerError[errors.size()]);
	}

	private void parseCompilerOutput(InputStream stream, ArrayList<CompilerError> errors) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("utf-8")));		
		String line = null;
		while (null != (line = reader.readLine())) {
			
			CompilerError error = parseCompilerError(line);
			if (null != error) errors.add(error);
		}
	}
	
	protected abstract CompilerError parseCompilerError(String line);
}
