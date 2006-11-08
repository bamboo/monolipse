package monolipse.core.foundation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class IOUtilities {

	public static String combinePath(String parent, String relativePath)
			throws IOException {
		return new File(parent, relativePath).getCanonicalPath();
	}

	public static String toString(java.io.InputStream stream)
			throws IOException {
		return toString(new InputStreamReader(stream));
	}

	public static String toString(InputStream stream, String charset)
			throws IOException {
		return toString(new InputStreamReader(stream, charset));
	}

	public static String toString(InputStreamReader reader)
			throws IOException {

		final BufferedReader bufferedReader = new BufferedReader(reader);
		final StringWriter writer = new StringWriter();
		String line = null;
		while (null != (line = bufferedReader.readLine())) {
			writer.write(line);
			writer.write("\n");
		}
		return writer.toString();
	}

}
