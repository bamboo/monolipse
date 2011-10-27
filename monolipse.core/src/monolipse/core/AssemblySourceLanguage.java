package monolipse.core;

public enum AssemblySourceLanguage {
	BOOJAY("boojay"),
	BOO("boo"),
	CSHARP("cs"),
	CSHARP_1_1("cs11");
	
	private final String _id;

	AssemblySourceLanguage(String id) {
		_id = id;
	}
	
	public String id() {
		return _id;
	}

	public static AssemblySourceLanguage forId(String id) {
		for (AssemblySourceLanguage language : values()) {
			if (language.id().equals(id))
				return language;
		}
		throw new IllegalArgumentException(id);
	}

	public String fileExtension() {
		return isBoo()
			? "boo"
			: "cs";
		
	}

	public boolean isBoo() {
		return this == BOO || this == BOOJAY;
	}
	
}