package monolipse.core.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.*;

import monolipse.core.*;
import monolipse.core.launching.IProcessMessageHandler;
import monolipse.core.launching.ProcessMessage;

import org.eclipse.core.runtime.CoreException;

public class CompilerServices extends AbstractBooServiceClient {

	public static synchronized CompilerServices getInstance() throws CoreException {
		if (null == _instance) {
			_instance = new CompilerServices();
		}
		return _instance;
	}
	
	private static CompilerServices _instance;
	
	private CompilerServices() throws CoreException {
	}

	public String expandMacros(String code, AssemblySourceLanguage language) {
		assertBooLanguage(language);
		return rpc("EXPAND-" + language.toString() + "-MACROS", code);
	}

	private void assertBooLanguage(AssemblySourceLanguage language) {
		if (language != AssemblySourceLanguage.BOO
			&& language != AssemblySourceLanguage.BOOJAY)
			throw new IllegalArgumentException();
	}
	
	public String expand(String code, AssemblySourceLanguage language) {
		assertBooLanguage(language);
		return rpc("EXPAND-" + language.toString(), code);
	}

	private String rpc(final String messageName, String payload) {
		final String messageResponse = messageName + "-RESPONSE";
		final Exchanger<String> returnValue = new Exchanger<String>();
		try {
			setMessageHandler(messageResponse, new IProcessMessageHandler() {
				public void handle(ProcessMessage message) {
					try {
						returnValue.exchange(message.payload, 3, TimeUnit.SECONDS);
					} catch (Exception e) {
						BooCore.logException(e);
					}
				}
			});
			send(messageName, payload);
			return returnValue.exchange(null, 3, TimeUnit.SECONDS);
		} catch (Exception e) {
			BooCore.logException(e);
		} finally {
			setMessageHandler(messageResponse, null);
		}
		return null;
	}

	public OutlineNode getOutline(String text) throws IOException {
		final String outlineString = rpc("GET-OUTLINE", text);
		return outlineString == null
			? null
			: parseOutline(outlineString);
	}
	
	private OutlineNode parseOutline(String text) {
		OutlineNode node = new OutlineNode();
		BufferedReader reader = new BufferedReader(new StringReader(text));
		String line = null;
		try {
			while (null != (line = reader.readLine())) {
				if (line.equals("BEGIN-NODE")) {
					node = node.create();
				} else if (line.equals("END-NODE")) {
					node = node.parent();
				} else {
					String[] parts = line.split(":");
					for (String attribute: parts) {
						String[] values = attribute.split("=");
						if (values[0].equals("type")) 
							node.type(values[1]);
						if (values[0].equals("name")) 
							node.name(values[1]);
						if (values[0].equals("startline")) 
							node.startLine(Integer.parseInt(values[1]));
						if (values[0].equals("endline")) 
							node.endLine(Integer.parseInt(values[1]));
						if (values[0].equals("visibility")) 
							node.visibility(values[1]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return node;
	}

	protected String getProposalsMessageId() {
		return "GET-COMPILER-PROPOSALS";
	}

	public String getHoverInformation(String code, int offset, int length) {
		String paramSeparator = "<<<-><-><->>>";
		return rpc("GET-HOVER-INFORMATION", code + paramSeparator + String.valueOf(offset) + paramSeparator + String.valueOf(length));
	}
}
