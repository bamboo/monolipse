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

import java.io.*;
import java.util.*;

import monolipse.core.*;
import monolipse.core.foundation.*;
import monolipse.core.runtime.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class BooBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = BooCore.ID_PLUGIN + ".booBuilder";

	protected void startupOnInitialize() {
		super.startupOnInitialize();
	}
		
	private void addMarker(String path, String message, int lineNumber, int severity) {

		String relativePath = path.substring(getProject().getLocation().toOSString().length() + 1);
		relativePath = relativePath.replaceAll("\\\\", "/");
		IFile file = getProject().getFile(relativePath);
		BooMarkers.addMarker(file, message, lineNumber, severity);

	}
	
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IAssemblySource[] sources = getAssemblySources();
		for (int i = 0; i < sources.length; ++i) {
			try {
				clean(sources[i], monitor);
			} catch (Exception x) {
				BooMarkers.addMarker(sources[i].getFolder(), x.getMessage(), -1, IMarker.SEVERITY_ERROR);
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
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
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

	private boolean ensureReferences(IAssemblySource source) throws CoreException {
		
		for (IAssemblyReference r : source.getReferences()) {
			if (r instanceof AssemblyReferenceError) {
				return cantBeBuilt(source, "reference '" + r.getRemembrance() + "' couldn't be loaded: " + ((AssemblyReferenceError)r).error().getLocalizedMessage());
			}
			if (r instanceof IAssemblySourceReference) {
				if (BooMarkers.hasErrors(((IAssemblySourceReference)r).getAssemblySource())) {
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

	private boolean cantBeBuilt(IAssemblySource source, String reason) {
		BooMarkers.addErrorMarker(source, "'" + source.getFolder().getName() + "' can't be built because " + reason);
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
		
		BooMarkers.deleteMarkers(source.getFolder());
		
		try {
			if (!ensureCanBeBuilt(source))
				return;
			
			IFile[] srcFiles = source.getSourceFiles();
			if (srcFiles.length == 0)
				return;
			
			ensureOutputFolderFor(source, monitor);
			
			CompilerError[] errors = launchCompiler(source, srcFiles);
			if (reportErrors(source, errors) > 0)
				return;
			
			refreshOutputFolder(source, monitor);
			setDerivedOutputFile(source, monitor);
			
			if (source.getLanguage() == AssemblySourceLanguage.BOO) {
				IFolder outputFolder = (IFolder)source.getOutputFile().getParent();
				copyLocalReferences(source, outputFolder, monitor);
				copyResources(source, outputFolder, monitor);
			}
			
		} catch (Exception e) {
			BooMarkers.addMarker(source.getFolder(), e.getMessage(), -1, IMarker.SEVERITY_ERROR);
			BooCore.logException(e);
		}
	}

	private void setDerivedOutputFile(IAssemblySource source, IProgressMonitor monitor) throws CoreException {
		IFile outputFile = source.getOutputFile();
		if (outputFile.exists())
			outputFile.setDerived(true, monitor);
	}

	private void refreshOutputFolder(IAssemblySource source, IProgressMonitor monitor) throws CoreException {
		IFile outputFile = source.getOutputFile();
		outputFile.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
	}

	private void ensureOutputFolderFor(IAssemblySource source,
			IProgressMonitor monitor) throws CoreException {
		WorkspaceUtilities.ensureDerivedParentExists(source.getOutputFile(), monitor);
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

	private void copyLocalReferences(final IAssemblySource source, final IFolder outputFolder, final IProgressMonitor monitor) throws CoreException {
		source.visitReferences(new AssemblyReferenceVisitor() {
			@Override
			public boolean visit(ILocalAssemblyReference reference) throws CoreException {
				IO.copyFileToFolder(reference.getFile(), outputFolder, monitor);
				return true;
			}
			
			@Override
			public boolean visit(IAssemblySourceReference reference) throws CoreException {
				
				IAssemblySource assemblySource = reference.getAssemblySource();
				if (assemblySource.getOutputFolder().equals(outputFolder))
					return true;
				
				IFile outputFile = assemblySource.getOutputFile();
				IO.copyFileToFolder(outputFile, outputFolder, monitor);
				copyDebugInfoOf(outputFile, outputFolder, monitor);
				
				return true;
			}

			private void copyDebugInfoOf(IFile assemblyFile, final IFolder outputFolder, final IProgressMonitor monitor)
					throws CoreException {
				IFile debugInfoFile = debugInfoFileFor(assemblyFile);
				//debugInfoFile.refreshLocal(0, monitor);
				if (debugInfoFile.exists())
					IO.copyFileToFolder(debugInfoFile, outputFolder, monitor);
			}

			private IFile debugInfoFileFor(IFile assemblyFile) {
				IPath debugInfo = assemblyFile.getProjectRelativePath().addFileExtension(".mdb");
				return assemblyFile.getProject().getFile(debugInfo);
			}
			
			@Override
			public boolean visit(IBooAssemblyReference reference) throws CoreException {
				String assemblyPath = reference.getCompilerReference();
				IFile outputFile = outputFolder.getFile(new Path(assemblyPath).lastSegment());
				if (outputFile.exists())
					return true;
				try {	
					IO.copyFile(assemblyPath, outputFile, monitor);
				} catch (IOException e) {
					e.printStackTrace();
				}
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
				BooMarkers.addErrorMarker(source, error.message);
			else
				addMarker(error.path, error.message, error.line, isError
							? IMarker.SEVERITY_ERROR
							: IMarker.SEVERITY_WARNING);
			
			if (isError) ++errorCount;
		}
		return errorCount;
	}
}
