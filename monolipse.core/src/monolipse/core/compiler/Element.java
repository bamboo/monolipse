package monolipse.core.compiler;

public class Element {

	private String _file;
	private int _line;
	private int _column;

	private Element(String message) {
		String[] parts = message.trim().split(":");
		for (String attribute: parts) {
			String[] values = attribute.split("=");
			if (values[0].equals("file")) 
				_file = values[1];
			if (values[0].equals("line")) 
				_line = Integer.parseInt(values[1]);
			if (values[0].equals("column")) 
				_column = Integer.parseInt(values[1]);
		}
	}
	
	public String toString() {
		return "Element: " + "File: " + _file + " Line: " + _line + " Column: " + _column;
	}

	public static Element fromRpcResult(String rpcMessage) {
		return new Element(rpcMessage);
	}

	public String getFile() {
		return _file;
	}

	public int getLine() {
		return _line;
	}

	public int getColumn() {
		return _column;
	}
}
