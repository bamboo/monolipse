package monolipse.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import monolipse.core.IAssemblyReference;
import monolipse.ui.BooUI;
import monolipse.ui.IBooUIConstants;

public class BooExplorerLabelProvider implements ILabelProvider {

	private final ILabelProvider _delegate = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();

	static class OverlayedImage extends CompositeImageDescriptor {

		private Image _bg;
		
		private ImageDescriptor _topRight;

		private ImageDescriptor _bottomLeft;

		public OverlayedImage(Image background, ImageDescriptor topRight, ImageDescriptor bottomLeft) {
			_bg = background;
			_bottomLeft = bottomLeft;
			_topRight = topRight;
		}

		protected void drawCompositeImage(int width, int height) {
			drawImage(_bg.getImageData(), 0, 0);
			if (null != _topRight) {
				drawImage(_topRight.getImageData(), 0, 0);
			}
			if (null != _bottomLeft) {
				drawImage(_bottomLeft.getImageData(), 0, 8);
			}

		}

		protected Point getSize() {
			return new Point(_bg.getBounds().width, _bg.getBounds().height);
		}
		
		public int hashCode() {
			int code = _bg.hashCode();
			if (null != _topRight) {
				code ^= _topRight.hashCode();
			}
			if (null != _bottomLeft) {
				code ^= _bottomLeft.hashCode();
			}
			return code;
		}
		
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof OverlayedImage)) return false;
			OverlayedImage other = (OverlayedImage)obj;
			return _bg.equals(other._bg)
				&& equals(_topRight, other._topRight)
				&& equals(_bottomLeft, other._bottomLeft);
		}

		private boolean equals(ImageDescriptor lhs, ImageDescriptor rhs) {
			if (lhs == null) return rhs == null;
			return lhs.equals(rhs);
		}

	}

	public Image getImage(Object element) {
		if (element instanceof ReferenceContainer) {
			return BooUI.getImage(IBooUIConstants.REFERENCES);
		}
		if (element instanceof IAssemblyReference) {
			return BooUI.getImage(IBooUIConstants.ASSEMBLY_REFERENCE);
		}	
	
		Image image = _delegate.getImage(element);
		ImageDescriptor errorOverlay = getErrorOverlay(element);
		return null == errorOverlay
			? image
			: cache(new OverlayedImage(image, null, errorOverlay));
	}
	
	Map _imageCache = new HashMap();

	private Image cache(ImageDescriptor descriptor) {
		Image cached = (Image)_imageCache.get(descriptor);
		if (null == cached) {
			cached = descriptor.createImage();
			_imageCache.put(descriptor, cached);
		}
		return cached;
	}

	private ImageDescriptor getErrorOverlay(Object element) {
		ImageDescriptor overlay = null;
		try {
			IResource resource = ((IResource) ((IAdaptable) element).getAdapter(IResource.class));
			IMarker[] markers = resource.findMarkers(
					IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			// need to distinguish between errors and warnings
			if (markers.length > 0) {
				overlay = BooUI.getImageDescriptor(IBooUIConstants.WARNING);
				for (int i = 0; i < markers.length; i++) {
					IMarker marker = markers[i];
					if (marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
						overlay = BooUI.getImageDescriptor(IBooUIConstants.ERROR);
						break;
					}
				}
			}
		} catch (CoreException e) {
		}
		return overlay;
	}

	public String getText(Object element) {
		if (element instanceof ReferenceContainer) {
			return "References";
		}
		if (element instanceof IAssemblyReference) {
			return ((IAssemblyReference)element).getAssemblyName();
		}
		return _delegate.getText(element);
	}

	public void addListener(ILabelProviderListener listener) {
		_delegate.addListener(listener);
	}

	public void dispose() {
		_delegate.dispose();
	}

	public boolean isLabelProperty(Object element, String property) {
		return _delegate.isLabelProperty(element, property);
	}

	public void removeListener(ILabelProviderListener listener) {
		_delegate.removeListener(listener);
	}

}
