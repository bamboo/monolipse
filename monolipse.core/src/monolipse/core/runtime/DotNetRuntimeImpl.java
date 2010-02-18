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
		return "c:/WINDOWS/Microsoft.NET/Framework/v3.5";
	}

	public IAssemblyReference[] listGlobalAssemblyCache() throws IOException {
		throw new NotImplementedException();
	}

	public IAssemblyReference getGlobalAssemblyCacheReference(String name, String version, String culture, String token) {
		throw new NotImplementedException();
	}

	public IRemembrance getRemembrance() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isDotnet() {
		return true;
	}
}
