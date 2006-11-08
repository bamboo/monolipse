package monolipse.core.internal;

import org.eclipse.core.runtime.CoreException;

import monolipse.core.BooCore;
import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblyReferenceVisitor;
import monolipse.core.IGlobalAssemblyCacheReference;
import monolipse.core.IMemorable;
import monolipse.core.IRemembrance;

public class GlobalAssemblyCacheReference implements IGlobalAssemblyCacheReference {

	private String _name;
	private String _version;
	private String _culture;
	private String _token;

	public GlobalAssemblyCacheReference(String name, String version, String culture, String token) {
		_name = name;
		_version = version;
		_culture = culture;
		_token = token;
	}

	public String getAssemblyName() {
		return _name;
	}

	public String getCompilerReference() {
//		return "\"" + _name + ", Version=" + _version + ", Culture=" + _culture + ", PublicKeyToken=" + _token + "\"";
		return _name;
	}

	public String getType() {
		return IAssemblyReference.GAC;
	}

	public String getVersion() {
		return _version;
	}

	public String getToken() {
		return _token;
	}
	
	static public class Remembrance implements IRemembrance {
		public String name;
		public String version;
		public String culture;
		public String token;
		
		public Remembrance(String name, String version, String culture, String token) {
			this.name = name;
			this.version = version;
			this.culture = culture;
			this.token = token;
		}
		
		/**
		 * public no arg constructor for xstream deserialization
		 * on less capable virtual machines.
		 */
		public Remembrance() {
		}

		public IMemorable activate() throws CoreException {
			return BooCore.getRuntime().getGlobalAssemblyCacheReference(name, version, culture, token);
		}
	}
		
	public IRemembrance getRemembrance() {
		return new Remembrance(_name, _version, _culture, _token);
	}

	public boolean accept(IAssemblyReferenceVisitor visitor) throws CoreException {
		return visitor.visit(this);
	}	
}
