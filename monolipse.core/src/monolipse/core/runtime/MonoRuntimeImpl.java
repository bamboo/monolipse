package monolipse.core.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monolipse.core.IAssemblyReference;
import monolipse.core.IMonoLauncher;
import monolipse.core.IMonoRuntime;
import monolipse.core.IRemembrance;
import monolipse.core.foundation.IOUtilities;
import monolipse.core.internal.GlobalAssemblyCacheReference;

import org.eclipse.core.runtime.Platform;


public class MonoRuntimeImpl implements IMonoRuntime {

	private static final Map _cachedGacReferences = new TreeMap();

	private static boolean _gacInitialized = false;

	static final Pattern ASSEMBLY_NAME_PATTERN = Pattern
			.compile("\\s*([^,]+),\\s*Version=([^,]+),\\s*Culture=([^,]+),\\s*PublicKeyToken=([^,]+)($|,\\s*Custom=null)$");

	public static final String PATH_GACUTIL = "lib/mono/1.0/gacutil.exe";

	public static String getOSDependentRuntimeExecutable(String runtimeLocation)
			throws IOException {
		String path = IOUtilities.combinePath(runtimeLocation, MonoRuntimeImpl.RUNTIME_EXECUTABLE);
		return Platform.OS_WIN32.equals(Platform.getOS())
			? path + ".exe"
			: path;
	}

	private static IAssemblyReference[] toArray(Collection references) {
		return (IAssemblyReference[]) references
				.toArray(new IAssemblyReference[references.size()]);
	}

	String _location;

	public static final String RUNTIME_EXECUTABLE = "bin/mono";

	public MonoRuntimeImpl(String location) {
		if (null == location)
			throw new IllegalArgumentException("location cannot be null");
		_location = location;
	}

	private Object cacheKey(String name, String version, String culture,
			String token) {
		return name + version + culture + token;
	}

	private IAssemblyReference createAndCacheReference(String name,
			String version, String culture, String token) {
		IAssemblyReference reference = new GlobalAssemblyCacheReference(name,
				version, culture, token);
		_cachedGacReferences.put(cacheKey(name, version, culture, token),
				reference);
		return reference;
	}

	public IMonoLauncher createLauncher(String executablePath)
			throws IOException {
		MonoLauncherImpl launcher = new MonoLauncherImpl(MonoRuntimeImpl.getOSDependentRuntimeExecutable(_location));
		launcher.add("--debug");
		launcher.add(executablePath);
		return launcher;
	}

	private IAssemblyReference getCachedReference(String name, String version,
			String culture, String token) {
		return (IAssemblyReference) _cachedGacReferences.get(cacheKey(name,
				version, culture, token));
	}

	public IAssemblyReference getGlobalAssemblyCacheReference(String name,
			String version, String culture, String token) {
		IAssemblyReference reference = getCachedReference(name, version,
				culture, token);
		if (null == reference) {
			reference = createAndCacheReference(name, version, culture, token);
		}
		return reference;
	}

	public String getLocation() {
		return _location;
	}

	public IRemembrance getRemembrance() {
		return null;
	}

	private void initializeGlobalAssemblyCache() throws IOException {
		IMonoLauncher launcher = createLauncher(IOUtilities.combinePath(
				getLocation(), PATH_GACUTIL));
		launcher.add("-l");
		Process p = launcher.launch();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p
				.getInputStream()));
		String line = null;
		while (null != (line = reader.readLine())) {
			Matcher m = ASSEMBLY_NAME_PATTERN.matcher(line);
			if (m.matches()) {

				String name = m.group(1);
				String version = m.group(2);
				String culture = m.group(3);
				String token = m.group(4);

				getGlobalAssemblyCacheReference(name, version, culture, token);
			}
		}
		_gacInitialized = true;
	}

	public IAssemblyReference[] listGlobalAssemblyCache() throws IOException {
		if (!_gacInitialized) {
			initializeGlobalAssemblyCache();
		}
		return toArray(_cachedGacReferences.values());
	}

	@Override
	public boolean isDotnet() {
		return false;
	}
}
