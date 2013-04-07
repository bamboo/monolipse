package monolipse.ui.tests;

import monolipse.core.BooCore;
import monolipse.core.IAssemblySource;
import monolipse.core.IMonoProject;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class BooProjectTestCase extends AbstractBooTestCase {

	public void testProjectAdapter() {
		IMonoProject adapted = (IMonoProject) _project.getProject().getAdapter(IMonoProject.class);
		assertSame(_booProject, adapted);
	}
	
	public void testAddAssemblySource() throws Exception {
		assertEquals(0, _booProject.getAssemblySources().length);
		
		IAssemblySource foo = addAssemblySource("src/Foo");
		assertNotNull(foo);
		IFolder folder = foo.getFolder();
		assertNotNull(folder);
		assertTrue(folder.exists());
		
		IAssemblySource[] sources = _booProject.getAssemblySources();
		assertEquals(1, sources.length);
		assertSame(foo, sources[0]);
		
		assertSame(foo, folder.getAdapter(IAssemblySource.class));
		
		IAssemblySource bar = addAssemblySource("src/Bar");
		sources = _booProject.getAssemblySources();
		assertEquals(2, sources.length);
		assertContains(foo, sources);
		assertContains(bar, sources);
	}
	
	public void testAssemblySourceOrder() throws Exception {
		IAssemblySource foo = addAssemblySource("src/Foo");
		IAssemblySource bar = addAssemblySource("src/Bar");
		IAssemblySource baz = addAssemblySource("src/Baz");
		IAssemblySource bang = addAssemblySource("src/Bang");

		foo.setReferences(BooCore.createAssemblyReference(bar));
		baz.setReferences(BooCore.createAssemblyReference(foo));
		bang.setReferences(BooCore.createAssemblyReference(foo));
		
		// (baz, bang) -> foo -> bar
		// build order should be:
		// bang, bar, foo, baz
		
		IAssemblySource[] order = _booProject.getAssemblySourceOrder(bang, foo, bar, baz);
		assertSame(bar, order[0]);
		assertSame(foo, order[1]);
		assertSame(bang, order[2]);
		assertSame(baz, order[3]);
	}
	
	static class ResourceChangeListener implements IResourceChangeListener {
		private IResourceDelta _delta;
		
		public IResourceDelta getDelta() {
			return _delta;
		}
		
		public void resourceChanged(IResourceChangeEvent event) {
			synchronized (this) {
				try {
					_delta = event.getDelta();
				} finally {
					notify();
				}
			}
		}
	}
	
	public void testGetAffectedAssemblySources() throws Exception {
		final IAssemblySource foo = addAssemblySource("src/Foo");
		final IAssemblySource bar = addAssemblySource("src/Bar");
		final IAssemblySource baz = addAssemblySource("src/Baz");
		foo.setReferences(BooCore.createAssemblyReference(bar));
		baz.setReferences(BooCore.createAssemblyReference(foo));
		
		final ResourceChangeListener listener = new ResourceChangeListener();
		final IWorkspace workspace = getProject().getWorkspace();
		workspace.addResourceChangeListener(listener);
		try {
			synchronized (listener) {
				WorkspaceJob job = new WorkspaceJob("addFile") {
					public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
						try {
							copyResourceTo("Bar.boo", "src/Bar");
						} catch (Exception e) {
							fail(e.toString());
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				listener.wait();
			}
		} finally {
			workspace.removeResourceChangeListener(listener);
		}
		
		assertNotNull(listener.getDelta());
		
		IAssemblySource[] sources = _booProject.getAffectedAssemblySources(listener.getDelta());
		assertNotNull(sources);
		assertEquals(3, sources.length);
		assertContains(foo, sources);
		assertContains(bar, sources);
		assertContains(baz, sources);
	}

	void assertContains(Object element, Object[] array) {
		for (int i=0; i<array.length; ++i) {
			if (element == array[i]) return;
		}
		fail("Element '" + element + "' not found.");
	}
}
