package monolipse.core;

import org.eclipse.core.runtime.CoreException;

/**
 * A remembrance which can be used to activate a memorable object.
 */
public interface IRemembrance {
	IMemorable activate() throws CoreException;
}
