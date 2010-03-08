/*
 * Boo Development Tools for the Eclipse IDE
 * Copyright (C) 2005 Rodrigo B. de Oliveira (rbo@acm.org)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */
package monolipse.core.internal;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import monolipse.core.AssemblyReferenceVisitor;
import monolipse.core.BooCore;
import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblySource;
import monolipse.core.IAssemblySourceReference;
import monolipse.core.ILocalAssemblyReference;
import monolipse.core.IMonoProject;
import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.core.runtime.CompilerError;
import monolipse.core.runtime.CompilerLauncher;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class BooBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = BooCore.ID_PLUGIN + ".booBuilder";

	public static final String BOO_PROBLEM_MARKER_TYPE = BooCore.ID_PLUGIN + ".booProblem";
	
	protected void startupOnInitialize() {
//		BooCore.getDefault().getPluginPreferences().addPropertyChangeListener(new IPropertyChangeListener() {
//			public void propertyChange(PropertyChangeEvent event) {
//				if (BooCore.P_RUNTIME_LOCATION.equals(event.getProperty())) {
//					scheduleProjectRebuild();
//				}
//			}
//		});
		super.startupOnInitialize();
	}
	
//	private void scheduleProjectRebuild() {
//		WorkspaceJob job = new WorkspaceJob("boo build") {
//			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
//				getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
//				return Status.OK_STATUS;
//			}
//		};
//		job.schedule();
//	}
	
	private void addMarker(IResource resource, String message, int lineNumber,
			int severity) {
		try {
			IMarker marker = resource.createMarker(BOO_PROBLEM_MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
			BooCore.logException(e);
		}
	}

	private void addMarker(String path, String message, int lineNumber,
			int severity) {

		String relativePath = path.substring(getProject().getLocation()
				.toOSString().length() + 1);
		relativePath = relativePath.replaceAll("\\\\", "/");
		IFile file = getProject().getFile(relativePath);
		addMarker(file, message, lineNumber, severity);

	}
	
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IAssemblySource[] sources = getAssemblySources();
		for (int i=0; i<sources.length; ++i) {
			try { 
				clean(sources[i], monitor);
			} catch (Exception x) {
				addMarker(sources[i].getFolder(), x.getMessage(), -1, IMarker.SEVERITY_ERROR);
			}
		}
	}

	private void clean(IAssemblySource source, final IProgressMonitor monitor) throws CoreException {
		IFile outputFile = source.getOutputFile();
		final IContainer outputFolder = outputFile.getParent();
		deleteIfExists(outputFile, monitor);
		source.visitReferences(new AssemblyReferenceVisitor() {
			public boolean visit(ILocalAssemblyReference reference) throws CoreException {
				deleteIfExists(outputFolder.getFile(new Path(reference.getFile().getName())), monitor);
				return true;
			}
		});
	}

	private void deleteIfExists(IFile outputFile, IProgressMonitor monitor) throws CoreException {
		if (outputFile.exists()) {
			outputFile.delete(true, monitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	private void deleteMarkers(IResource resource) {
		try {
			resource.deleteMarkers(BOO_PROBLEM_MARKER_TYPE, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			BooCore.logException(e);
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		BooCore.logInfo("full build requested");
		compile(getAssemblySources(), monitor);
	}

	private IAssemblySource[] getAssemblySources() throws CoreException {
		return getBooProject().getAssemblySources();
	}

	private IMonoProject getBooProject() throws CoreException {
		return BooProject.get(getProject());
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		BooCore.logInfo("incremental build requested");
		compile(getBooProject().getAffectedAssemblySources(delta), monitor);
	}
	
	boolean ensureCanBeBuilt(IAssemblySource source) throws CoreException {
		if (!validateRuntime())
			return cantBeBuilt(source, "the location for the mono runtime is not set.");
		
		return ensureReferences(source);
	}

	private boolean ensureReferences(IAssemblySource source)
			throws CoreException {
		for (IAssemblyReference r : source.getReferences()) {;
			if (r instanceof IAssemblySourceReference) {
				if (hasErrors(((IAssemblySourceReference)r).getAssemblySource())) {
					return cantBeBuilt(source, "reference '" + r.getAssemblyName() + "' contains errors.");
				}
			} else if (r instanceof ILocalAssemblyReference) {
				if (!((ILocalAssemblyReference)r).getFile().exists()) {
					return cantBeBuilt(source, "reference '" + r.getAssemblyName() + "' cannot be found.");
				}
			}
		}
		return true;
	}

	private boolean hasErrors(IAssemblySource source) throws CoreException {
		IMarker[] markers = source.getFolder().findMarkers(BOO_PROBLEM_MARKER_TYPE, false, IResource.DEPTH_INFINITE);
		for (int i=0; i<markers.length; ++i) {
			if (IMarker.SEVERITY_ERROR == markers[i].getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
				return true;
			}
		}
		return false;
	}

	private boolean cantBeBuilt(IAssemblySource source, String reason) {
		addErrorMarker(source, "'" + source.getFolder().getName() + "' can't be built because " + reason);
		return false;
	}
	
	boolean validateRuntime() {		
		return null != BooCore.getRuntime();
	}

	void compile(IAssemblySource[] sources, IProgressMonitor monitor) throws CoreException {
		sources = getBooProject().getAssemblySourceOrder(sources);
		for (int i=0; i<sources.length; ++i) {
			compile(sources[i], monitor);
		}
	}
	
	void compile(IAssemblySource source, IProgressMonitor monitor) throws CoreException {
		
		deleteMarkers(source.getFolder());
		
		try {
			if (!ensureCanBeBuilt(source))
				return;
			
			IFile[] files = source.getSourceFiles();
			if (0 == files.length) return;
			
			IFile file = source.getOutputFile();
			WorkspaceUtilities.ensureDerivedParentExists(file);
			CompilerError[] errors = launchCompiler(source, files);
			if (0 == reportErrors(source, errors)) {
				file.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
				if (file.exists()) {
					file.setDerived(true);
				}
				IFolder outputFolder = (IFolder)file.getParent();
				copyLocalReferences(source, outputFolder, monitor);
				copyResources(source, outputFolder, monitor);
			}
		} catch (Exception e) {
			addMarker(source.getFolder(), e.getMessage(), -1, IMarker.SEVERITY_ERROR);
			BooCore.logException(e);
		}
	}
	
	private void copyResources(final IAssemblySource source, final IFolder outputFolder, final IProgressMonitor monitor) throws CoreException {
		source.getFolder().accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (!(resource instanceof IFile)) return true;
				IFile file = (IFile)resource;
				if ("config".equals(file.getFileExtension())) {
					IO.copyFileToFolder(file, outputFolder, monitor);
				}
				return true;
			}
		});
	}

	private void copyLocalReferences(final IAssemblySource source, final IFolder folder, final IProgressMonitor monitor) throws CoreException {
		source.visitReferences(new AssemblyReferenceVisitor() {
			public boolean visit(ILocalAssemblyReference reference) throws CoreException {
				IO.copyFileToFolder(reference.getFile(), folder, monitor);
				return true;
			}
			
			public boolean visit(IAssemblySourceReference reference) throws CoreException {
				if (reference.getAssemblySource().getOutputFolder().equals(folder))
					return true;
				IO.copyFileToFolder(reference.getAssemblySource().getOutputFile(), folder, monitor);
				return true;
			}
		});
	}

	private CompilerError[] launchCompiler(IAssemblySource source, IFile[] files) throws IOException, CoreException {
		CompilerLauncher launcher = CompilerLauncher.createLauncher(source);
		launcher.addSourceFiles(files);
		return launcher.run();
	}

	
	int reportErrors(IAssemblySource source, CompilerError[] errors) throws IOException {
		
		int errorCount = 0;
		for (CompilerError error : errors) {
			
			boolean isError = error.severity == CompilerError.ERROR;
			if (error.path == null)
				addErrorMarker(source, error.message);
			else
				addMarker(error.path, error.message, error.line, isError
							? IMarker.SEVERITY_ERROR
							: IMarker.SEVERITY_WARNING);
			
			if (isError) ++errorCount;
		}
		return errorCount;
	}

	private void addErrorMarker(IAssemblySource source, String message) {
		addMarker(source.getFolder(), message, -1, IMarker.SEVERITY_ERROR);
	}
}
