/**
 * 
 */
package monolipse.core.launching;

import java.io.BufferedWriter;
import java.io.PrintWriter;

public class ProcessMessage {
	
	public static final String END_MARKER = "END-MESSAGE";
	
	public final String name;
	public final String payload;
	
	public ProcessMessage(String name, String payload) {
		this.name = name;
		this.payload = payload;
	}
	
	public void writeTo(BufferedWriter writer) {
		PrintWriter printer = new PrintWriter(writer);
		printer.println(name);
		printer.println(payload);
		printer.println(ProcessMessage.END_MARKER);
		printer.flush();
	}
}