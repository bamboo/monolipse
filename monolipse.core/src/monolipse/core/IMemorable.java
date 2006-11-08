package monolipse.core;

/**
 * Interface for objects which are worth being remembered by the
 * system such as compiler settings, assembly references, etc.
 * 
 * @author rodrigob
 */
public interface IMemorable {
	
	/**
	 * 
	 * @return a remembrance which can be used to restore/activate the memorable object at a later time.
	 */
	IRemembrance getRemembrance();
}
