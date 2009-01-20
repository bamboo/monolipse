package monolipse.core.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblyReferenceVisitor;
import monolipse.core.IAssemblySource;
import monolipse.core.AssemblySourceLanguage;
import monolipse.core.IAssemblySourceReference;
import monolipse.core.IRemembrance;
import monolipse.core.foundation.WorkspaceUtilities;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public class BooAssemblySource implements IAssemblySource {
	
	private static final String SETTINGS_CHARSET = "utf-8";

	private static final String SETTINGS_FILE = ".monolipse";
	
	private static final QualifiedName ASSEMBLY_SOURCE_SESSION_KEY = new QualifiedName("monolipse.core.resources", "BooAssemblySourceSession");
	
	public static IAssemblySource create(IFolder folder) throws CoreException {
		synchronized (folder) {
			IAssemblySource source = BooAssemblySource.get(folder);
			if (null != source)
				return source;
			source = internalCreate(folder);
			source.save(null);
			return source;
		}
	}
	
	public static BooAssemblySource get(IFolder folder) throws CoreException {
		synchronized (folder) {
			BooAssemblySource source = (BooAssemblySource) folder.getSessionProperty(ASSEMBLY_SOURCE_SESSION_KEY);
			if (null == source) {
				if (isAssemblySource(folder)) {
					source = internalCreate(folder);
				}
			}
			return source;
		}
	}

	private static BooAssemblySource internalCreate(IFolder folder) throws CoreException {
		BooAssemblySource source = new BooAssemblySource(folder);
		folder.setSessionProperty(ASSEMBLY_SOURCE_SESSION_KEY, source);
		return source;
	}
	
	public static boolean isAssemblySource(Object element) {
		try {
			return element instanceof IFolder
				&& BooAssemblySource.isAssemblySource((IFolder)element);
		} catch (CoreException x) {
		}
		return false;
	}

	public static boolean isAssemblySource(IFolder folder) throws CoreException {
		return folder.getFile(SETTINGS_FILE).exists();
//		synchronized (folder) {
//			return folder.getPersistentProperty(ASSEMBLY_SOURCE_PERSIST_KEY) != null;
//		}
	}
	
	private IFolder _folder;

	private IAssemblyReference[] _references;
	
	private String _outputType;

	private AssemblySourceLanguage _language;

	private IFolder _outputFolder;

	private String _additionalOptions;

	BooAssemblySource(IFolder folder) throws CoreException {
		if (null == folder || !folder.exists()) throw new IllegalArgumentException();
		_folder = folder;
		_language = AssemblySourceLanguage.BOOJAY;
		_outputFolder = defaultOutputFolder();
		refresh(null);
	}

	private IFolder defaultOutputFolder() {
		return _folder.getProject().getFolder("bin");
	}
	
	public void setLanguage(AssemblySourceLanguage language) {
		_language = language;
	}
	
	public AssemblySourceLanguage getLanguage() {
		return _language == null
			? AssemblySourceLanguage.BOOJAY
			: _language;
	}
	
	public void setOutputFolder(IFolder folder) {
		_outputFolder = folder;
	}
	
	public IFolder getOutputFolder() {
		return _outputFolder;
	}
	
	/* (non-Javadoc)
	 * @see monolipse.core.IBooAssemblySource#getFolder()
	 */
	public IFolder getFolder() {
		return _folder;
	}
	
	/* (non-Javadoc)
	 * @see monolipse.core.IBooAssemblySource#getSourceFiles()
	 */
	public IFile[] getSourceFiles() throws CoreException {
		final List files = new ArrayList();
		IResourceVisitor visitor = new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (isBooFile(resource) && resource.exists()) {
					files.add(resource);
				}
				return true;
			}
		};
		_folder.accept(visitor, IResource.DEPTH_INFINITE, IResource.FILE);
		return (IFile[])files.toArray(new IFile[files.size()]);
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(IFolder.class)) {
			return _folder;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see monolipse.core.IBooAssemblySource#setReferences(monolipse.core.IBooAssemblyReference[])
	 */
	public void setReferences(IAssemblyReference... references) {
		if (null == references) throw new IllegalArgumentException("references");
		_references = references;
	}
	
	/* (non-Javadoc)
	 * @see monolipse.core.IBooAssemblySource#getReferences()
	 */
	public IAssemblyReference[] getReferences() {
		return _references;
	}

	/* (non-Javadoc)
	 * @see monolipse.core.IBooAssemblySource#getOutputType()
	 */
	public String getOutputType() {
		return _outputType;
	}
	
	public void setOutputType(String outputType) {
		if (null == outputType) throw new IllegalArgumentException();
		if (!outputType.equals(OutputType.CONSOLE_APPLICATION)
			&& !outputType.equals(OutputType.WINDOWS_APPLICATION)
			&& !outputType.equals(OutputType.LIBRARY)) {
			throw new IllegalArgumentException("outputType");
		}
		_outputType = outputType;
	}

	/* (non-Javadoc)
	 * @see monolipse.core.IBooAssemblySource#getOutputFile()
	 */
	public IFile getOutputFile() {
		return _outputFolder.getFile(_folder.getName() + getOutputAssemblyExtension());
	}
	
	/* (non-Javadoc)
	 * @see monolipse.core.IBooAssemblySource#refresh(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void refresh(IProgressMonitor monitor) throws CoreException {
		IWorkspaceRunnable action = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IFile file = getSettingsFile();
				file.refreshLocal(IResource.DEPTH_ZERO, monitor);
				if (!file.exists()) {
					useDefaultSettings();
					save(monitor); 
				} else {
					loadSettingsFrom(file);
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(action, monitor);
	}
	
	static public class AssemblySourceRemembrance {
		public String language;
		public String outputType;
		public IRemembrance[] references;
		public String outputFolder;
		public String additionalOptions;
		public AssemblySourceRemembrance(BooAssemblySource source) {
			language = source.getLanguage().id();
			outputType = source.getOutputType();
			references = new IRemembrance[source._references.length];
			outputFolder = source.getOutputFolder().getFullPath().toPortableString();
			additionalOptions = source.getAdditionalOptions();
			for (int i=0; i<references.length; ++i) {
				references[i] = source._references[i].getRemembrance();
			}
		}
		
		/**
		 * public no arg constructor for xstream deserialization
		 * on less capable virtual machines.
		 */
		public AssemblySourceRemembrance() {
		}
		
		public IAssemblyReference[] activateReferences() throws CoreException {
			IAssemblyReference[] asmReferences = new IAssemblyReference[references.length];
			for (int i=0; i<asmReferences.length; ++i) {
				asmReferences[i] = (IAssemblyReference) references[i].activate();
			}
			return asmReferences;
			                                                                
		}
	}
	
	/* (non-Javadoc)
	 * @see monolipse.core.IBooAssemblySource#save()
	 */
	public void save(IProgressMonitor monitor) throws CoreException {
		XStream stream = createXStream();
		String xml = stream.toXML(new AssemblySourceRemembrance(this));
		IFile file = getSettingsFile();
		if (!file.exists()) {
			file.create(encode(xml), true, monitor);
			file.setCharset(SETTINGS_CHARSET, monitor);
		} else {
			file.setContents(encode(xml), true, true, monitor);
		}
	}

	private void loadSettingsFrom(IFile file) throws CoreException {
		AssemblySourceRemembrance remembrance = (AssemblySourceRemembrance) createXStream().fromXML(decode(file));
		final String language = remembrance.language;
		_language = isEmptyOrNull(language)
				? AssemblySourceLanguage.BOO
				: AssemblySourceLanguage.forId(language);
		_outputType = remembrance.outputType;
		_references = remembrance.activateReferences();
		_additionalOptions = remembrance.additionalOptions;
		
		String path = remembrance.outputFolder;
		_outputFolder = path == null ? defaultOutputFolder() : WorkspaceUtilities.getFolder(path);
	}
	
	private boolean isEmptyOrNull(String language) {
		return language == null || language.isEmpty();
	}

	private IFile getSettingsFile() {
		return _folder.getFile(SETTINGS_FILE);
	}
	
	private XStream createXStream() {
		XStream stream = new XStream(new DomDriver());
		stream.alias("settings", AssemblySourceRemembrance.class);
		return stream;
	}

	private InputStream encode(String xml) throws CoreException {
		try {
			return new ByteArrayInputStream(xml.getBytes(SETTINGS_CHARSET));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			WorkspaceUtilities.throwCoreException(e);
		}
		return null;
	}

	private Reader decode(IFile file) throws CoreException {
		try {
			return new InputStreamReader(file.getContents(), file.getCharset());
		} catch (IOException e) {
			e.printStackTrace();
			WorkspaceUtilities.throwCoreException(e);
		}
		return null;
	}

	private void useDefaultSettings() {
		_references = new IAssemblyReference[0];
		_outputType = OutputType.CONSOLE_APPLICATION;
		_language = null;
	}

	private String getOutputAssemblyExtension() {
		return OutputType.LIBRARY.equals(getOutputType()) ? ".dll" : ".exe";
	}
	
	boolean isBooFile(IResource resource) {
		if (IResource.FILE != resource.getType()) return false;
		final String extension = resource.getFileExtension();
		if (extension == null) return false;
		return extension.equalsIgnoreCase(expectedSourceFileExtension());
	}

	private String expectedSourceFileExtension() {
		return getLanguage().fileExtension();
	}

	public static IAssemblySource getContainer(IResource resource) throws CoreException {
		IContainer parent = resource.getParent();
		while (null != parent && IResource.FOLDER == parent.getType()) {
			BooAssemblySource source = get((IFolder)parent);
			if (null != source) return source;
			parent = parent.getParent();
		}
		return null;
	}

	public boolean visitReferences(IAssemblyReferenceVisitor visitor) throws CoreException {
		for (int i=0; i<_references.length; ++i) {
			if (!_references[i].accept(visitor)) return false;
		}
		return true;
	}
	
	public String toString() {
		return _folder.getFullPath().toString();
	}

	public static boolean references(IAssemblySource l,
			final IAssemblySource r) throws CoreException {
		IAssemblyReference[] references = l.getReferences();
		for (int i = 0; i < references.length; ++i) {
			IAssemblyReference reference = references[i];
			if (reference instanceof IAssemblySourceReference) {
				if (r == ((IAssemblySourceReference) reference)
						.getAssemblySource()) {
					return true;
				}
			}
		}
		return false;
	}

	public String getAdditionalOptions() {
		return _additionalOptions == null ? "" : _additionalOptions;
	}

	public void setAdditionalOptions(String additionalOptions) {
		_additionalOptions = additionalOptions;
	}
}
