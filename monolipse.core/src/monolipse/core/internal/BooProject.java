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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import monolipse.core.BooCore;
import monolipse.core.IAssemblySource;
import monolipse.core.IMonoProject;
import monolipse.core.foundation.ArrayUtilities;
import monolipse.core.foundation.WorkspaceUtilities;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.JavaCore;


public class BooProject implements IMonoProject {

	private static final QualifiedName SESSION_KEY = new QualifiedName(
			"monolipse.core.resources", "BooProject");

	public static IMonoProject create(IProject project, IProgressMonitor monitor) throws CoreException {		
		if (project.hasNature(BooCore.ID_NATURE)) {
			return BooProject.get(project);
		}
		ensureNaturesFor(project, monitor, BooCore.ID_NATURE, JavaCore.NATURE_ID);
		return BooProject.get(project);
	}

	private static void ensureNaturesFor(IProject project, IProgressMonitor monitor, String... expectedNatureIds) throws CoreException {
		IProjectDescription description = project.getDescription();		
		String[] natureIds = description.getNatureIds();
		for (String expected : expectedNatureIds)
			if (!project.hasNature(expected))
				natureIds = ArrayUtilities.append(natureIds, expected);
		description.setNatureIds(natureIds);
		project.setDescription(description, monitor);
	}

	public static IMonoProject get(IProject project) throws CoreException {
		IMonoProject p = (IMonoProject) project.getSessionProperty(SESSION_KEY);
		if (p == null && project.hasNature(BooCore.ID_NATURE)) {
			p = new BooProject(project);
			project.setSessionProperty(SESSION_KEY, p);
		}
		return p;
	}

	IProject _project;

	BooProject(IProject project) {
		_project = project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see monolipse.core.IBooProject#addAssemblySource(org.eclipse.core.runtime.IPath)
	 */
	public IAssemblySource addAssemblySource(IPath path)
			throws CoreException {
		IFolder folder = _project.getFolder(path);
		WorkspaceUtilities.createTree(folder);
		return BooAssemblySource.create(folder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see monolipse.core.IBooProject#getAssemblySources()
	 */
	public IAssemblySource[] getAssemblySources() throws CoreException {
		final List<IAssemblySource> sources = new ArrayList<IAssemblySource>();
		IResourceVisitor visitor = new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFolder) {
					IAssemblySource source = BooAssemblySource
							.get((IFolder) resource);
					if (source != null) {
						sources.add(source);
						return false;
					}
				}
				return true;
			}
		};
		_project.accept(visitor, IResource.DEPTH_INFINITE, IResource.FOLDER);
		return toBooAssemblySourceArray(sources);
	}

	private IAssemblySource[] toBooAssemblySourceArray(
			final Collection<IAssemblySource> sources) {
		return sources
				.toArray(new IAssemblySource[sources.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see monolipse.core.IBooProject#getAffectedAssemblySources(org.eclipse.core.resources.IResourceDelta)
	 */
	public IAssemblySource[] getAffectedAssemblySources(IResourceDelta delta)
			throws CoreException {
		final IAssemblySource[] sources = getAssemblySources();
		final Set<IAssemblySource> affected = new HashSet<IAssemblySource>();
		delta.accept(new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				if (IResource.FILE == resource.getType()) {
					IAssemblySource parent = BooCore
							.assemblySourceContaining(resource);
					if (null != parent && !affected.contains(parent)) {
						affected.add(parent);
						addDependents(affected, sources, parent);
						return false;
					}
				}
				return true;
			}

			private void addDependents(final Set<IAssemblySource> affected, final IAssemblySource[] sources, IAssemblySource changed) throws CoreException {
				for (int i=0; i<sources.length; ++i) {
					IAssemblySource source = sources[i];
					if (BooAssemblySource.references(source, changed)) {
						if (!affected.contains(source)) {
							affected.add(source);
							addDependents(affected, sources, source);
						}
					}
				}
			}
		});
		return toBooAssemblySourceArray(affected);
	}

	public IAssemblySource[] getAssemblySourceOrder(
			IAssemblySource... sources) throws CoreException {
		return new TopoSorter(sources).sorted();
	}

	static class TopoSorter {

		private List<IAssemblySource> _sources;

		private List<IAssemblySource> _sorted = new ArrayList<IAssemblySource>();

		public TopoSorter(IAssemblySource[] sources) throws CoreException {
			_sources = new ArrayList<IAssemblySource>(Arrays.asList(sources));
			sort();
		}
		
		public IAssemblySource[] sorted() {
			return _sorted.toArray(new IAssemblySource[_sorted.size()]);
		}
		
		private void sort() throws CoreException {
			while (!_sources.isEmpty()) {
				int index = nextLeaf();
				if (index < 0) {
					throw new IllegalStateException("reference cycle");
				}
				_sorted.add(_sources.get(index));
				_sources.remove(index);
			}
		}

		private int nextLeaf() throws CoreException {
			for (int i=0; i<_sources.size(); ++i) {
				IAssemblySource source = _sources.get(i);
				boolean edge = false;
				for (int j=0; j<_sources.size(); ++j) {
					if (BooAssemblySource.references(source, _sources.get(j))) {
						edge = true;
						break;
					}
				}
				if (!edge) return i;
			}
			return -1;
		}
	}
}
