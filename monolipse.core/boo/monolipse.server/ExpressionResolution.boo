namespace monolipse.server

import Boo.Lang.Compiler
import Boo.Lang.Compiler.Ast
import Boo.Lang.Compiler.TypeSystem

import Boo.Lang.PatternMatching

class ExpressionResolution:
	
	static def ForCodeString(code as string):
		return ForCompileUnit(Boo.Lang.Parser.BooParser.ParseString("code", code))
		
	static def ForCompileUnit(originalCompileUnit as CompileUnit):
		compiler = BooCompiler()		
		compiler.Parameters.Pipeline = Pipelines.ResolveExpressions(BreakOnErrors: false)
		ctx = compiler.Run(originalCompileUnit.CloneNode())
		return ExpressionResolution(originalCompileUnit, ctx)
		
	[getter(OriginalCompileUnit)] _originalCompileUnit as CompileUnit
	_ctx as CompilerContext
	
	private def constructor(originalCompileUnit, ctx):
		_originalCompileUnit = originalCompileUnit
		_ctx = ctx
		
	NodeInformationProvider:
		get: return NodeInformationProvider(_ctx.CompileUnit)
		
	def RunInResolvedCompilerContext(action as System.Action of CompilerContext):
		_ctx.Run(action)

class NodeInformationProvider(DepthFirstVisitor):
""" Provides information about a node. 
"""
	_compileUnit as CompileUnit
	
	def constructor(resolvedCompileUnit as CompileUnit):
		_compileUnit = resolvedCompileUnit
	
	def ElementFor(node as Node):
		node = Resolve(node)
		if node is null:
			return ElementInfo.Unknown
	
		match TypeSystemServices.GetOptionalEntity(node):
			case l=ILocalEntity(Name: name, Type: t):
				return ElementInfo(NodeType: "local", Name: name, ResolvedType: ToString(t), Info: node.GetAncestor[of Method]().FullName, Documentation: DocStringFor(l))
			case c=IConstructor():
				return AddLexicalInfo(c, ElementInfo(NodeType: "constructor", Name: c.Name, ResolvedType: c.ToString(), Documentation: DocStringFor(c, c.DeclaringType)))
			case m=IMethod():
				return AddLexicalInfo(m, ElementInfo(NodeType: "method", Name: m.Name, ResolvedType: m.ToString(), Info: ToString(m.ReturnType), Documentation: DocStringFor(m)))
			case f=IField(FullName: name, Type: t):
				return AddLexicalInfo(f, ElementInfo(NodeType: "field", Name: name, ResolvedType: ToString(t), Documentation: DocStringFor(f)))
			case p=IProperty(FullName: name, Type: t):
				return AddLexicalInfo(p, ElementInfo(NodeType: "property", Name: name, ResolvedType: ToString(t), Documentation: DocStringFor(p)))
			case e=IEvent(FullName: name, Type: t):
				return AddLexicalInfo(e, ElementInfo(NodeType: "event", Name: name, ResolvedType: ToString(t), Documentation: DocStringFor(e)))
			case t=IType():
				return AddLexicalInfo(t, ElementInfo(NodeType: "itype", ResolvedType: ToString(t), Documentation: DocStringFor(t)))
			case te=ITypedEntity(Type: t):
				return AddLexicalInfo(te, ElementInfo(NodeType: "itypedentity", ResolvedType: ToString(te), Documentation: DocStringFor(te)))
			otherwise:
				return ElementInfo.Unknown

	private def AddLexicalInfo(entity as IEntity, info as ElementInfo):
		intern = (entity as IInternalEntity)
		if intern:
			info.File = intern.Node.LexicalInfo.FileName
			info.Line = intern.Node.LexicalInfo.Line
			info.Column = intern.Node.LexicalInfo.Column

		return info
		
	def TooltipFor(node as Node):
		node = Resolve(node)
		if node is null:
			return "?"
			
		match TypeSystemServices.GetOptionalEntity(node):
			case l=ILocalEntity(Name: name, Type: t):
				return FormatHoverText("${name} as ${ToString(t)} - ${node.GetAncestor[of Method]().FullName}", DocStringFor(l))
			case c=IConstructor():
				return FormatHoverText("${c.ToString()}", DocStringFor(c, c.DeclaringType))
			case m=IMethod():
				return FormatHoverText("${m.ToString()} as ${ToString(m.ReturnType)}", DocStringFor(m))
			case f=IField(FullName: name, Type: t):
				return FormatHoverText("${name} as ${ToString(t)}",  DocStringFor(f))
			case p=IProperty(FullName: name, Type: t):
				return FormatHoverText("${name} as ${ToString(t)}",  DocStringFor(p))
			case e=IEvent(FullName: name, Type: t):
				return FormatHoverText("${name} as ${ToString(t)}",  DocStringFor(e))
			case t=IType():
				return FormatHoverText(ToString(t), DocStringFor(t))
			case te=ITypedEntity(Type: t):
				return FormatHoverText(ToString(t), DocStringFor(te))
			otherwise:
				return "?"
	
	def FormatHoverText(header as string, docstring as string):
		result = " ${header} " 
		if not string.IsNullOrEmpty(docstring):
			docstring = docstring.Replace("\n", "<br/>")
			result += "<br/><br/> ${docstring} <br/> " 
		return result
		
	def DocStringFor(*entities as (IEntity)):
		for entity in entities:
			target = entity as IInternalEntity
			if (target is not null and not string.IsNullOrEmpty(target.Node.Documentation)):
				return target.Node.Documentation
		return ""
				
	def NamespaceAt(node as Node) as INamespace:
		match EntityFor(node):
			case type=IType():
				return type
			case ITypedEntity(Type: type):
				return type
			case ns=INamespace():
				return ns
			otherwise:
				return null
				
	def EntityFor(node as Node) as IEntity:
		resolvedNode = Resolve(node)
		if resolvedNode is null:
			return null
		return TypeSystemServices.GetOptionalEntity(resolvedNode)
				
	def ToString(type as IType):
		match type:
			case IType(EntityType: EntityType.Error):
				return "?"
			case ct=ICallableType():
				return ct.GetSignature().ToString()
			otherwise:
				return type.FullName
						
	def Resolve(node as Node):
		_found = null
		_lookingFor = node.LexicalInfo
		VisitAllowingCancellation(_compileUnit)
		return _found
		
	_found as Node
	_lookingFor as LexicalInfo
	
	override def OnReferenceExpression(node as ReferenceExpression):
		MatchNode(node)
		
	override def LeaveMemberReferenceExpression(node as MemberReferenceExpression):
		MatchNode(node)
		
	override def OnSimpleTypeReference(node as SimpleTypeReference):
		MatchNode(node)
		
	private def MatchNode(node as Node):
		if node.LexicalInfo is _lookingFor:
			_found = node
			Cancel()