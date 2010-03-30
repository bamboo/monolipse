package monolipse.ui.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

import junit.framework.TestCase;
import monolipse.core.compiler.CompilerProposal;
import monolipse.core.interpreter.*;

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
	
	public void testCompletionProposalsForVariable() throws Exception {
		assertCompletionProposals("class Foo:\n\tdef foo():\n\t\tpass\nf = Foo()", "f.", new String[] {
			"constructor",
			"foo",
			"Equals",
			"Equals", // static version
			"GetHashCode",
			"GetType",
			"ToString",
			"ReferenceEquals", // static method
		});
	}
	
	public void testCompletionProposalsForNamespace() throws Exception {
		assertCompletionProposals("namespace Bar\nclass Baz:\n\tpass", "Bar.", new String[] {
			"Baz", "Input1Module", "ParentInterpreter"
		});
	}

	private void assertCompletionProposals(final String code,
			final String insertPoint, String... expected) throws IOException,
			InterruptedException, TimeoutException {
		final Exchanger<Boolean> exchanger = new Exchanger<Boolean>();
		_interpreter.addListener(new IInterpreterListener() {
			public void evalFinished(String result)  {
				try {
					exchanger.exchange(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		_interpreter.eval(code);
		exchanger.exchange(true, 3, TimeUnit.SECONDS);
		
		CompilerProposal[] proposals = _interpreter.getCompletionProposals(insertPoint + "__codecomplete__");
		assertEquals(expected.length, proposals.length);
		for (int i=0; i<expected.length; ++i) {
			assertEquals(expected[i], proposals[i].getName());
		}
	}
	
	public void tearDown() throws Exception {
		_interpreter.dispose();
	}

}
