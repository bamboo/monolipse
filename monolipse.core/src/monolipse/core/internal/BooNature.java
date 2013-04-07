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

import java.util.HashMap;
import java.util.Map;

import monolipse.core.foundation.ArrayUtilities;
import monolipse.core.foundation.JavaModelUtilities;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;


public class BooNature implements IProjectNature {
	
	private IProject _project;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		addBuilderTo(_project);
		addExclusionPatternsToSourceFoldersOf(_project);
	}

	private void addExclusionPatternsToSourceFoldersOf(IProject project) throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null)
			return;
		addExclusionPatternsToSourceFoldersOf(javaProject);
	}

	private void addExclusionPatternsToSourceFoldersOf(IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry[] classpath = javaProject.getRawClasspath();
		Map<IClasspathEntry, IClasspathEntry> modified = addExclusionPatternsTo(classpath);
		if (modified.isEmpty())
			return;
		javaProject.setRawClasspath(replaceModifiedClasspathEntries(classpath, modified), null);
	}

	private IClasspathEntry[] replaceModifiedClasspathEntries(
			IClasspathEntry[] classpath,
			Map<IClasspathEntry, IClasspathEntry> modifiedEntries) {
		
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry originalEntry = classpath[i];
			IClasspathEntry modified = modifiedEntries.get(originalEntry);
			if (modified != null)
				classpath[i] = modified;
		}
		return classpath;
	}

	private Map<IClasspathEntry, IClasspathEntry> addExclusionPatternsTo(IClasspathEntry[] classpath) {
		Map<IClasspathEntry, IClasspathEntry> modified = new HashMap<IClasspathEntry, IClasspathEntry>();
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE)
				continue;
			
			IPath[] exclusionPatterns = entry.getExclusionPatterns();
			IPath[] newExclusionPatterns = addExclusionPatternsTo(exclusionPatterns);
			if (exclusionPatterns == newExclusionPatterns)
				continue;
			
			IClasspathEntry newSourceEntry = JavaCore.newSourceEntry(
					entry.getPath(),
					entry.getInclusionPatterns(),
					newExclusionPatterns,
					entry.getOutputLocation(),
					entry.getExtraAttributes());
			
			modified.put(entry, newSourceEntry);
		}
		return modified;
	}

	private IPath[] addExclusionPatternsTo(IPath[] exclusionPatterns) {
		IPath[] newExclusionPatterns = exclusionPatterns;
		for (String pattern : new String[] { "**/.monolipse", "**/*.boo" }) {
			if (!JavaModelUtilities.exclusionPatternsContains(pattern, exclusionPatterns))
				newExclusionPatterns = ArrayUtilities.append(newExclusionPatterns, new Path(pattern));
		}
		return newExclusionPatterns;
	}

	public static void addBuilderTo(IProject project) throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		
		if (hasBuilder(commands))
			return;

		ICommand command = desc.newCommand();
		command.setBuilderName(BooBuilder.BUILDER_ID);
		
		desc.setBuildSpec((ICommand[])ArrayUtilities.append(commands, command));
		project.setDescription(desc, null);
	}

	private static boolean hasBuilder(ICommand[] commands) {
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(BooBuilder.BUILDER_ID)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(BooBuilder.BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return _project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this._project = project;
	}

}
