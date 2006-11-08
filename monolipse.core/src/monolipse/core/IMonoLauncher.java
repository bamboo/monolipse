package monolipse.core;

import java.io.IOException;

public interface IMonoLauncher {
	void setWorkingDir(java.io.File workingDir);
	void add(String arg);
	Process launch() throws IOException ;
}
