package monolipse.core.runtime;

public class CompilerError {
	
	public static final int ERROR = 1;
	
	public static final int WARNING = 2;
	
	public int line = -1;
	public String code;
	public String message;
	public int severity = CompilerError.ERROR;
	
	public void setPath(String path) {
		this.path = path.replace('\\', '/');
	}

	public String getPath() {
		return path;
	}
	
	private String path;
}
