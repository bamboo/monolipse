package monolipse.core.runtime;

import java.io.IOException;

import monolipse.core.IAssemblyReference;
import monolipse.core.IMonoLauncher;
import monolipse.core.IMonoRuntime;
import monolipse.core.IRemembrance;

public class DotNetRuntimeImpl implements IMonoRuntime {

	public IMonoLauncher createLauncher(String executablePath) throws IOException {
		return new MonoLauncherImpl(executablePath);
	}

	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public IAssemblyReference[] listGlobalAssemblyCache() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public IAssemblyReference getGlobalAssemblyCacheReference(String name, String version, String culture, String token) {
		// TODO Auto-generated method stub
		return null;
	}

	public IRemembrance getRemembrance() {
		// TODO Auto-generated method stub
		return null;
	}
}
