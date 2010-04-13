package monolipse.core.compiler;

public class Element {

	private String _rpcMessage;

	private Element(String rpcMessage) {
		_rpcMessage = rpcMessage;
	}
	
	public String toString() {
		return "Element =>\n" + _rpcMessage;
	}

	public static Element fromRpcResult(String rpcMessage) {
		return new Element(rpcMessage);
	}
}
