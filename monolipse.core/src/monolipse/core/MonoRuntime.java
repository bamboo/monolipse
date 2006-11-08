package monolipse.core;

import monolipse.core.runtime.MonoRuntimeImpl;

public class MonoRuntime {
	
	public static IMonoRuntime newInstance(String location) {
		return new MonoRuntimeImpl(location);
	}	

}
