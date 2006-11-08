package monolipse.ui.tests;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Platform;

import monolipse.core.BooCore;
import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblySource;
import monolipse.core.IAssemblySourceReference;
import monolipse.core.ILocalAssemblyReference;

public class BooCoreTestCase extends AbstractBooTestCase {

	public void testCreateAssemblyReference() throws Exception {
		IAssemblySource bar = addAssemblySource("src/Bar");
		IAssemblySourceReference barReference = (IAssemblySourceReference) BooCore.createAssemblyReference(bar);
		assertNotNull(barReference);
		assertSame(bar, barReference.getAssemblySource());
		assertSame(barReference, BooCore.createAssemblyReference(bar));
	}
	
	public void testFileAdapters() throws Exception {
		assertNull(getAdapter(getFile("lib/TestClass.dll"), IAssemblyReference.class));
		
		final IFile resource = copyResourceTo("TestClass.dll", "lib");
		
		ILocalAssemblyReference reference = (ILocalAssemblyReference)getAdapter(resource, IAssemblyReference.class);
		assertNotNull(reference);
		assertSame(resource, reference.getFile());
		assertSame(reference, getAdapter(resource, IAssemblyReference.class));
	}
	
	public void testAssemblySourceAdapters() throws Exception {
		final String path = "src/Bar";
		
		final IFolder folder = getFolder(path);
		assertNull(getAdapter(folder, IAssemblyReference.class));
		
		final IAssemblySource source = addAssemblySource(path);
		IAssemblySourceReference reference = (IAssemblySourceReference) getAdapter(folder, IAssemblyReference.class);
		assertNotNull(reference);
		assertSame(source, reference.getAssemblySource());
		assertSame(reference, getAdapter(folder, IAssemblyReference.class));
	}

	private Object getAdapter(Object adaptable, Class adapterClass) {
		return Platform.getAdapterManager().getAdapter(adaptable, adapterClass);
	}
}
