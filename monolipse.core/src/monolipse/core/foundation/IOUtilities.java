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
		return toString(bufferedReader);
	}

	public static String toString(final BufferedReader bufferedReader)
			throws IOException {
		final String lineSeparator = System.getProperty("line.separator");
		final StringWriter writer = new StringWriter();
		for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
			writer.write(line);
			writer.write(lineSeparator);
		}
		return writer.toString();
	}

}
