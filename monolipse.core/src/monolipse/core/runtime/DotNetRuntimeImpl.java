package monolipse.core.runtime;

import java.io.IOException;

import monolipse.core.IAssemblyReference;
import monolipse.core.IMonoLauncher;
import monolipse.core.IMonoRuntime;
import monolipse.core.IRemembrance;
import monolipse.core.internal.GlobalAssemblyCacheReference;

public class DotNetRuntimeImpl implements IMonoRuntime {

	private final String location;
	
	public DotNetRuntimeImpl(String location) {
		this.location = location;
	}

	public IMonoLauncher createLauncher(String executablePath) throws IOException {
		return new MonoLauncherImpl(executablePath);
	}

	public String getLocation() {
		return location;
	}

	public IAssemblyReference[] listGlobalAssemblyCache() throws IOException {
		throw new NotImplementedException();
	}

	public IAssemblyReference getGlobalAssemblyCacheReference(String name, String version, String culture, String token) {
		return new GlobalAssemblyCacheReference(name, version, culture, token);
	}

	public IRemembrance getRemembrance() {
		throw new NotImplementedException();
	}

	//@Override
	public boolean isDotnet() {
		return true;
	}
}
