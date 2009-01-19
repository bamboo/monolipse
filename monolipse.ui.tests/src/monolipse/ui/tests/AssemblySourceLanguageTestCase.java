package monolipse.ui.tests;

import monolipse.core.AssemblySourceLanguage;
import junit.framework.TestCase;

public class AssemblySourceLanguageTestCase extends TestCase {
	
	public void testValueOrder() {
		final AssemblySourceLanguage[] values = AssemblySourceLanguage.values();
		
		assertOrder(
				values,
				AssemblySourceLanguage.BOOJAY,
				AssemblySourceLanguage.BOO,
				AssemblySourceLanguage.CSHARP,
				AssemblySourceLanguage.CSHARP_1_1);
	}

	private void assertOrder(AssemblySourceLanguage[] actual, AssemblySourceLanguage... expected) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i]);
		}
	}

}
