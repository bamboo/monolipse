package monolipse.core;

import java.io.IOException;

import monolipse.core.internal.BooAssemblyReference;
import monolipse.core.internal.BooAssemblyReferenceAdapterFactory;
import monolipse.core.internal.BooAssemblySource;
import monolipse.core.internal.BooAssemblySourceAdapterFactory;
import monolipse.core.internal.BooProject;
import monolipse.core.internal.BooProjectAdapterFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;



/**
 * The main plugin class to be used in the desktop.
 */
public class BooCore extends Plugin {
	
	public static final String ID_PLUGIN = "monolipse.core";
	
	/**
	 * Boo Project Nature ID
	 */
	public static final String ID_NATURE = BooCore.ID_PLUGIN + ".booNature";
	
	private static BooCore _plugin;
	
	/**
	 * The constructor.
	 */
	public BooCore() {
		_plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		registerAdapters();
		registerPreferenceListeners();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		_plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static BooCore getDefault() {
		return _plugin;
	}
	
	void registerAdapters() {
		IAdapterManager adapterManager = Platform.getAdapterManager();
		adapterManager.registerAdapters(new BooProjectAdapterFactory(), IProject.class);
		adapterManager.registerAdapters(new BooAssemblySourceAdapterFactory(), IFolder.class);
		adapterManager.registerAdapters(new BooAssemblyReferenceAdapterFactory(), IFile.class);
	}
	
	public static void logException(Exception e) {
		e.printStackTrace();
		BooCore plugin = getDefault();
		if (null == plugin) return;
		plugin.getLog().log(new Status(Status.ERROR, ID_PLUGIN, -1, e.getMessage(), e));
	}
	
	public static void logInfo(String message) {
		BooCore plugin = getDefault();
		if (null == plugin) return;
		System.out.println(message);
		plugin.getLog().log(new Status(Status.INFO, ID_PLUGIN, -1, message, null));
	}
	
	private void registerPreferenceListeners() {
	}

	public static IAssemblySource getAssemblySourceContainer(IResource resource) {
		try {
			return BooAssemblySource.getContainer(resource);
		} catch (CoreException e) {
			logException(e);
		}
		return null;
	}

	public static IAssemblySource createAssemblySource(IFolder folder) throws CoreException {
		return BooAssemblySource.create(folder);
	}

	public static IAssemblySource getAssemblySource(IFolder folder) throws CoreException {
		return BooAssemblySource.get(folder);
	}

	public static boolean isAssemblySource(Object selectedElement) {
		return BooAssemblySource.isAssemblySource(selectedElement);
	}

	public static IMonoProject createProject(IProject project) throws CoreException {
		return BooProject.create(project);
	}

	public static IAssemblyReference[] listGlobalAssemblyCache() throws IOException {
		return getRuntime().listGlobalAssemblyCache();
	}

	public static IAssemblyReference createAssemblyReference(IFile file) throws CoreException {
		return BooAssemblyReference.get(file);
	}
	
	public static IAssemblyReference createAssemblyReference(IAssemblySource source) throws CoreException {
		return BooAssemblyReference.get(source);
	}

	static IMonoRuntime _runtime;
	
	public static synchronized IMonoRuntime getRuntime() {
		if (null == _runtime) {			
			_runtime = MonoRuntime.newInstance(getMonoHome());
		}
		return _runtime;
	}

	private static String getMonoHome() {
		return System.getProperty("MONO_HOME", "/opt/local");
	}
}