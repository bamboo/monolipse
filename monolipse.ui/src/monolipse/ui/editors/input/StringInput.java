package monolipse.ui.editors.input;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class StringInput extends PlatformObject implements IStorageEditorInput {
	
	static class StringStorage extends PlatformObject implements IStorage {
		private String _contents;

		StringStorage(String contents) {
			this._contents = contents;
		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(_contents.getBytes());
		}

		public IPath getFullPath() {
			return null;
		}

		public String getName() {
			int len = Math.min(5, _contents.length());
			return _contents.substring(0, len).concat("...");
		}

		public boolean isReadOnly() {
			return true;
		}
	}
	
	private IStorage _storage;

	public StringInput(String storage) {
		this._storage = new StringStorage(storage);
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return _storage.getName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public IStorage getStorage() {
		return _storage;
	}

	public String getToolTipText() {
		return "String-based file: " + _storage.getName();
	}
}
