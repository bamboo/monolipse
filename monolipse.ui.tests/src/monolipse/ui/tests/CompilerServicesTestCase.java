package monolipse.ui.tests;

import java.io.IOException;

import monolipse.core.compiler.CompilerServices;
import monolipse.core.compiler.OutlineNode;
import monolipse.core.foundation.IOUtilities;

public class CompilerServicesTestCase extends AbstractBooTestCase {
	
	private CompilerServices builder;

	public void setUp() throws Exception {
		super.setUp();
		builder = CompilerServices.getInstance();
	}
	
	protected void tearDown() throws Exception {
		builder.dispose();
		super.tearDown();
	}
	
	public void testGetOutline() throws Exception {
		OutlineNode outline = builder.getOutline(loadResourceAsString("Outline.boo"));
		OutlineNode[] children = outline.children();
		assertEquals(2, children.length);
		
		assertEquals("Foo", children[0].name());
		assertEquals(OutlineNode.CLASS, children[0].type());
		assertEquals(3, children[0].line());
		assertEquals("global()", children[1].name());
		assertEquals(17, children[1].line());
		assertEquals(OutlineNode.METHOD, children[1].type());
	}
	
	public void testGetCompletionProposals() {
		
	}

	private String loadResourceAsString(String resource) throws IOException {
		return IOUtilities.toString(getResourceStream(resource));
	}

}
