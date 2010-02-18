package monolipse.core;

import java.io.*;

import monolipse.core.foundation.*;
import monolipse.core.internal.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
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
	
	public static void logException(Throwable exception) {
		exception.printStackTrace();
		BooCore plugin = getDefault();
		if (null == plugin) return;
		plugin.getLog().log(new Status(Status.ERROR, ID_PLUGIN, -1, exception.getMessage(), exception));
	}
	
	public static void logInfo(String message) {
		BooCore plugin = getDefault();
		if (null == plugin) return;
		System.out.println(message);
		plugin.getLog().log(new Status(Status.INFO, ID_PLUGIN, -1, message, null));
	}
	
	private void registerPreferenceListeners() {
	}

	public static IAssemblySource assemblySourceContaining(IResource resource) {
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
		if (null == _runtime)	
			_runtime = MonoRuntimeFactory.createDefaultRuntime();
		return _runtime;
	}	

	public static IMonoLauncher createLauncherWithRuntimeLocation(String compiler) throws IOException {
		return createLauncher(IOUtilities.combinePath(runtimeLocation(), compiler));
	}

	public static IMonoLauncher createLauncher(String path) throws IOException {
		return getRuntime().createLauncher(path);
	}

	private static String runtimeLocation() {
		return getRuntime().getLocation();
	}

	public static IMonoLauncher createLauncherFromBundle(String resource) throws IOException {
		final String path = resolveBundlePath(resource);
		final IMonoLauncher launcher = getRuntime().createLauncher(path);
//		launcher.setWorkingDir(new File(path).getParentFile());
		return launcher;
	}

	public static String resolveBundlePath(String path) throws IOException {
		return WorkspaceUtilities.getResourceLocalPath(getDefault().getBundle(), path);
	}
}