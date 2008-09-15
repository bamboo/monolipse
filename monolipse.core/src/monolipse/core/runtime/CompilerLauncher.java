package monolipse.core.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import monolipse.core.BooCore;
import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblySource;
import monolipse.core.IAssemblySourceLanguage;
import monolipse.core.IMonoCompilerLauncher;
import monolipse.core.IMonoLauncher;
import monolipse.core.IMonoRuntime;
import monolipse.core.foundation.IOUtilities;
import monolipse.core.foundation.WorkspaceUtilities;

import org.eclipse.core.resources.IFile;


public abstract class CompilerLauncher implements IMonoCompilerLauncher {
	
	public static CompilerLauncher createLauncher(String language) throws IOException {
		if (language.equals(IAssemblySourceLanguage.BOO)) {
			return new BooCompilerLauncher();
		}
		return new CSharpCompilerLauncher(language);
	}
	
	public static CompilerLauncher createLauncher(IAssemblySource source) throws IOException {
		CompilerLauncher launcher = createLauncher(source.getLanguage());
		launcher.setOutputFile(source.getOutputFile());
		launcher.setOutputType(source.getOutputType());
		launcher.addReferences(source.getReferences());
		launcher.add(source.getAdditionalOptions().split("\\s+"));
		return launcher;
	}

	private IMonoLauncher _launcher;
	private ResponseFile _responseFile;
	
	protected CompilerLauncher(String compiler) throws IOException {
		IMonoRuntime runtime = BooCore.getRuntime();
		_launcher = runtime.createLauncher(IOUtilities.combinePath(runtime.getLocation(), compiler));
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
			try {
				_responseFile.add(arg);
			} catch (IOException e) {
				BooCore.logException(e);
			}
		} else {
			_launcher.add(arg);
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
	
	public void setOutputFile(IFile outputFile) {
		add("-out:" + WorkspaceUtilities.getLocation(outputFile));
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
