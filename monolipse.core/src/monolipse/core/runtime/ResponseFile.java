package monolipse.core.runtime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import monolipse.core.BooCore;

public class ResponseFile {
	
	private final File _file;
	private BufferedWriter _writer;

	public ResponseFile() throws IOException {
		_file = File.createTempFile("monolipse", "tmp");
		_file.deleteOnExit();
		_writer = new BufferedWriter(new FileWriter(_file));
	}
	
	public void add(String arg) throws IOException {
		_writer.write(arg);
		_writer.newLine();
	}
	
	public void close() {
		if (null == _writer) {
			return;
		}
		try {
			_writer.close();
		} catch (IOException e) {
			BooCore.logException(e);
		}
		_writer = null;
	}
	
	public String toString() {
		return "@" + _file.getAbsolutePath();
	}

}
