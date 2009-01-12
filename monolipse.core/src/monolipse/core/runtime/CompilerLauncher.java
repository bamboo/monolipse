package monolipse.core.runtime;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

import monolipse.core.*;
import monolipse.core.foundation.WorkspaceUtilities;

import org.eclipse.core.resources.*;

public abstract class CompilerLauncher implements IMonoCompilerLauncher {
	
	public static CompilerLauncher createLauncher(String language) throws IOException {
		if (language.equals(IAssemblySourceLanguage.BOO))
			return new BooCompilerLauncher();
		if (language.equals(IAssemblySourceLanguage.BOOJAY))
			return new BoojayCompilerLauncher();
		return new CSharpCompilerLauncher(language);
	}
	
	public static CompilerLauncher createLauncher(IAssemblySource source) throws IOException {
		CompilerLauncher launcher = createLauncher(source.getLanguage());
		launcher.setOutput(source.getOutputFile());
		launcher.setOutputType(source.getOutputType());
		launcher.addReferences(source.getReferences());
		launcher.add(source.getAdditionalOptions().split("\\s+"));
		return launcher;
	}

	private final IMonoLauncher _launcher;
	private ResponseFile _responseFile;
	
	protected CompilerLauncher(String compiler) throws IOException {
		this(BooCore.createLauncherWithRuntimeLocation(compiler));
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
	
	public void addReferences(IAssemblyReference[] references) {		
		for (int i=0; i<references.length; ++i) {
			add("-r:" + references[i].getCompilerReference());
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
