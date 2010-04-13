namespace monolipse.server

class ElementInfo:
	public static Unknown = UnknownElementInfo()

	[property(NodeType)]
	_nodeType as string
		
	[property(Name)]
	_name as string
		
	[property(ResolvedType)] 
	_resolvedType as string
		
	[property(Info)]
	_info as string
		
	[property(Documentation)] 
	_documentation as string	

	def ToString():
		return "${NodeType}: ${Name} as ${ResolvedType} - ${Info}\n\n${Documentation}"

class UnknownElementInfo(ElementInfo):
	def ToString():
		return "?"
