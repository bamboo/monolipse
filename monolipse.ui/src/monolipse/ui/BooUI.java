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
package monolipse.ui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import monolipse.core.BooCore;
import monolipse.ui.internal.PreferencesAdapter;

public class BooUI extends AbstractUIPlugin {
		
	public static final String ID_PLUGIN = "monolipse.ui";

	// The shared instance.
	private static BooUI _plugin;

	private static PreferencesAdapter _corePreferenceStore;

	private static ResourceBundle _resourceBundle;
	
	/**
	 * The constructor.
	 */
	public BooUI() {
		_plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		_plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static BooUI getDefault() {
		return _plugin;
	}
	
	protected void initializeImageRegistry(ImageRegistry registry) {
		
		declareRegistryImage(registry, IBooUIConstants.ASSEMBLY_REFERENCE, "icons/Reference.png");
		declareRegistryImage(registry, IBooUIConstants.WARNING, "icons/WarningDecorator.gif");
		declareRegistryImage(registry, IBooUIConstants.ERROR, "icons/ErrorDecorator.gif");
		declareRegistryImage(registry, IBooUIConstants.ASSEMBLY_SOURCE_DECORATOR, "icons/AssemblySourceDecorator.gif");
		declareRegistryImage(registry, IBooUIConstants.REFERENCES, "icons/OpenReferenceFolder.png");
		declareRegistryImage(registry, IBooUIConstants.PROJECT, "icons/NewBooProject.png");
		declareRegistryImage(registry, IBooUIConstants.CLASS, "icons/class.png");
		declareRegistryImage(registry, IBooUIConstants.INTERFACE, "icons/interface.png");
		declareRegistryImage(registry, IBooUIConstants.ENUM, "icons/enum.png");
		declareRegistryImage(registry, IBooUIConstants.NAMESPACE, "icons/namespace.png");
		declareRegistryImage(registry, IBooUIConstants.FIELD, "icons/field.png");
		declareRegistryImage(registry, IBooUIConstants.METHOD, "icons/method.png");
		declareRegistryImage(registry, IBooUIConstants.PROPERTY, "icons/property.png");
		declareRegistryImage(registry, IBooUIConstants.EVENT, "icons/event.png");
		declareRegistryImage(registry, IBooUIConstants.CALLABLE, "icons/callable.png");
		declareRegistryImage(registry, IBooUIConstants.STRUCT, "icons/struct.png");
	}
	
	private final static void declareRegistryImage(ImageRegistry registry, String key, String path) {
		registry.put(key, loadImageDescriptor(path));
	}

	public static Image getImage(String key) {
		return getDefault().getImageRegistry().get(key);
	}
	
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getImageRegistry().getDescriptor(key);
	}


	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	private static ImageDescriptor loadImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(ID_PLUGIN, path);
	}

	public static IPreferenceStore getCorePreferenceStore() {
		if (null == _corePreferenceStore) {
			_corePreferenceStore = new PreferencesAdapter(getCore()
					.getPluginPreferences());
		}
		return _corePreferenceStore;
	}
	
	public static void saveCorePreferences() {
		getCore().savePluginPreferences();
	}

	private static BooCore getCore() {
		return BooCore.getDefault();
	}
	
	public static void logException(Exception e) {
		e.printStackTrace();
		getDefault().getLog().log(new Status(Status.ERROR, ID_PLUGIN, -1, e.getLocalizedMessage(), e));
	}
	
	public static void logInfo(String message) {
		getDefault().getLog().log(new Status(Status.INFO, ID_PLUGIN, -1, message, null));
	}

	public static ResourceBundle getResourceBundle() {
		if (null == _resourceBundle) {
			try {
				_resourceBundle = new PropertyResourceBundle(FileLocator.openStream(getDefault().getBundle(), new Path("plugin.properties"), true));
			} catch (IOException e) {
				logException(e);
				_resourceBundle = new EmptyResourceBundle();
			}
		}
		return _resourceBundle;
	}
	
	static class EmptyResourceBundle extends ResourceBundle {

		protected Object handleGetObject(String key) {
			return null;
		}

		public Enumeration getKeys() {
			return null;
		}
	};

}
