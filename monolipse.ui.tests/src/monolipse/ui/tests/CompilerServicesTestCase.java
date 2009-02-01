package monolipse.ui.tests;

import java.io.IOException;

import monolipse.core.compiler.CompilerServices;
import monolipse.core.compiler.OutlineNode;
import monolipse.core.foundation.IOUtilities;

public class CompilerServicesTestCase extends AbstractBooTestCase {
	
	private CompilerServices subject;

	public void setUp() throws Exception {
		super.setUp();
		subject = CompilerServices.getInstance();
	}
	
	protected void tearDown() throws Exception {
		subject.dispose();
		super.tearDown();
	}
	
	public void testExpandMacros() {
		String code = "print 'Hello World!'";
		String expected = "System.Console.WriteLine('Hello World!')";
		
		assertEquals(expected, subject.expandMacros(code).trim());
	}
	
	public void testGetOutline() throws Exception {
		final OutlineNode outline = outlineFor("Outline.boo");
		final OutlineNode[] children = outline.children();
		assertEquals(3, children.length);
		
		assertOutlineNode(children[0], "Foo", OutlineNode.CLASS, 3);
		assertOutlineNode(children[1], "global()", OutlineNode.METHOD, 17);
	}

	private OutlineNode outlineFor(final String resource) throws IOException {
		return subject.getOutline(loadResourceAsString(resource));
	}

	private void assertOutlineNode(final OutlineNode node,
			final String expectedName, final String expectedType,
			final int expectedLine) {
		assertEquals(expectedName, node.name());
		assertEquals(expectedType, node.type());
		assertEquals(expectedLine, node.line());
	}
	
	private String loadResourceAsString(String resource) throws IOException {
		return IOUtilities.toString(getResourceStream(resource));
	}

}
