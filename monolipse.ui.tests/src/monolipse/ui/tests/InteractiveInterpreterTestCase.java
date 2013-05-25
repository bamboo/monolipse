package monolipse.ui.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

import junit.framework.*;
import monolipse.core.compiler.CompilerProposal;
import monolipse.core.interpreter.*;

import static org.junit.Assert.*;

public class InteractiveInterpreterTestCase extends TestCase {
	
	InteractiveInterpreter _interpreter;
	
	public void setUp() throws Exception {
		_interpreter = new InteractiveInterpreter();
	}
	
	public void testEval() throws Exception {
		final Object mutex = new Object();
		final ArrayList<String> lines = new ArrayList<String>();
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
			"foo",
			"constructor",
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
		assertArrayEquals(expected, namesOf(proposals));
		
	}

	private String[] namesOf(CompilerProposal[] proposals) {
		String[] actual = new String[proposals.length];
		for (int i=0; i<proposals.length; ++i)
			actual[i] = proposals[i].getName();
		return actual;
	}
	
	public void tearDown() throws Exception {
		_interpreter.dispose();
	}

}
