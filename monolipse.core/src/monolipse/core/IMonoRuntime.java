package monolipse.core;

import java.io.IOException;

public interface IMonoRuntime extends IMemorable {

	IMonoLauncher createLauncher(String executablePath) throws IOException;

	String getLocation();

	IAssemblyReference[] listGlobalAssemblyCache() throws IOException;

	IAssemblyReference getGlobalAssemblyCacheReference(String name, String version, String culture, String token);
}
