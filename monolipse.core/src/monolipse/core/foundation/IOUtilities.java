package monolipse.core.foundation;

import java.io.File;
import java.io.IOException;

public class IOUtilities {

	public static String combinePath(String parent, String relativePath) throws IOException {
		return new File(parent, relativePath).getCanonicalPath();
	}

}
