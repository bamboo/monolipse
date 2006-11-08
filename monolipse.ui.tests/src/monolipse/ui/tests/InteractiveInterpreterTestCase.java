package monolipse.ui.tests;

import java.util.ArrayList;

import junit.framework.TestCase;
import monolipse.core.compiler.CompilerProposal;
import monolipse.core.interpreter.InteractiveInterpreter;
import monolipse.core.interpreter.IInterpreterListener;

public class InteractiveInterpreterTestCase extends TestCase {
	
	InteractiveInterpreter _interpreter;
	
	public void setUp() throws Exception {
		_interpreter = new InteractiveInterpreter();
	}
	
	public void testEval() throws Exception {
		final Object mutex = new Object();
		final ArrayList lines = new ArrayList();
		_interpreter.addListener(new IInterpreterListener() {
			public void evalFinished(String result) {
				synchronized (mutex) {
					lines.add(result);
					mutex.notify();
				}
			}
		});
		synchronized (mutex) {
			_interpreter.eval("print 'Hello'");
			mutex.wait(3000);
		}
		assertEquals(1, lines.size());
		assertEquals("Hello", lines.get(0).toString().trim());
	}
	
	public void testGetCompletionProposals() throws Exception {
		final Object mutex = new Object();
		_interpreter.addListener(new IInterpreterListener() {
			public void evalFinished(String result) {
				synchronized (mutex) {
					mutex.notify();
				}
			}
		});
		synchronized (mutex) {
			_interpreter.eval("class Foo:\n\tdef foo():\n\t\tpass\nf = Foo()");
			mutex.wait(3000);
		}
		CompilerProposal[] proposals = _interpreter.getCompletionProposals("f.__codecomplete__");
		String[] expected = new String[] {
				"foo",
				"Equals",
				"Equals", // static version
				"GetHashCode",
				"GetType",
				"ToString",
				"ReferenceEquals", // static method
		};
		assertEquals(expected.length, proposals.length);
		for (int i=0; i<expected.length; ++i) {
			assertEquals(expected[i], proposals[i].getName());
		}
	}
	
	public void tearDown() throws Exception {
		_interpreter.dispose();
	}

}
