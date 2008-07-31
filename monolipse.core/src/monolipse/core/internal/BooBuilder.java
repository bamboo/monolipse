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
import java.util.Map;

import monolipse.core.*;
import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.core.runtime.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class BooBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = BooCore.ID_PLUGIN + ".booBuilder";

	private static final String MARKER_TYPE = BooCore.ID_PLUGIN + ".booProblem";
	
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
			IMarker marker = resource.createMarker(MARKER_TYPE);
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
			clean(sources[i], monitor);
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
			resource.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_INFINITE);
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
		if (!validateRuntime())	return cantBeBuilt(source, "the location for the mono runtime is not set.");
		IAssemblyReference[] references = source.getReferences();
		for (int i=0; i<references.length; ++i) {
			IAssemblyReference r = references[i];
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
		IMarker[] markers = source.getFolder().findMarkers(MARKER_TYPE, false, IResource.DEPTH_INFINITE);
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
		
		if (!ensureCanBeBuilt(source)) return;
		try {
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
					copyFileToFolder(file, outputFolder, monitor);
				}
				return true;
			}
		});
	}

	private void copyLocalReferences(final IAssemblySource source, final IFolder folder, final IProgressMonitor monitor) throws CoreException {
		source.visitReferences(new AssemblyReferenceVisitor() {
			public boolean visit(ILocalAssemblyReference reference) throws CoreException {
				copyLocalReference(reference, folder, monitor);
				return true;
			}
			
			public boolean visit(IAssemblySourceReference reference) throws CoreException {
				if (project(source) != project(reference.getAssemblySource())) {
					copyFileToFolder(reference.getAssemblySource().getOutputFile(), folder, monitor);
				}
				return true;
			}
		});
	}
	

	private IProject project(final IAssemblySource source) {
		return source.getFolder().getProject();
	}

	private void copyLocalReference(ILocalAssemblyReference reference, IFolder folder, IProgressMonitor monitor) throws CoreException {
		copyFileToFolder(reference.getFile(), folder, monitor);
	}

	private void copyFileToFolder(IFile sourceFile, IFolder folder, IProgressMonitor monitor) throws CoreException {
		String name = sourceFile.getName();
		IFile targetFile = folder.getFile(name);
		if (targetFile.exists()) {
			if (!isNewer(sourceFile, targetFile)) return;
			targetFile.delete(true, monitor);
		}
		sourceFile.copy(targetFile.getFullPath(), true, monitor);
		targetFile.setDerived(true);
	}

	boolean isNewer(IFile sourceFile, IFile targetFile) {
		return sourceFile.getModificationStamp() > targetFile.getModificationStamp();
	}

	private CompilerError[] launchCompiler(IAssemblySource source, IFile[] files) throws IOException, CoreException {
		CompilerLauncher launcher = CompilerLauncher.createLauncher(source);
		launcher.addSourceFiles(files);
		return launcher.run();
	}

	
	int reportErrors(IAssemblySource source, CompilerError[] errors) throws IOException {
		
		for (int i=0; i<errors.length; ++i) {
			CompilerError error = errors[i];
			if (error.path == null) {
				addErrorMarker(source, error.message);
			} else {
				addMarker(error.path, error.message, error.line, error.severity == CompilerError.ERROR
							? IMarker.SEVERITY_ERROR
							: IMarker.SEVERITY_WARNING);
			}
		}
		return errors.length;
	}

	private void addErrorMarker(IAssemblySource source, String message) {
		addMarker(source.getFolder(), message, -1, IMarker.SEVERITY_ERROR);
	}
}
