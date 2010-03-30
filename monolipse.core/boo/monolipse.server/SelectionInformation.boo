namespace monolipse.server

import Boo.Lang.Compiler
import Boo.Lang.Compiler.Pipelines
import Boo.Lang.Compiler.Ast
import Boo.Lang.Compiler.TypeSystem

import Boo.Lang.PatternMatching

class Rectangle:
	_x1 as int
	_y1 as int
	_x2 as int
	_y2 as int

	def constructor():
		self(0, 0, 0, 0)
	
	def constructor(x1 as int, y1 as int, x2 as int, y2 as int):
		_x1 = x1
		_y1 = y1
		_x2 = x2
		_y2 = y2
		
	def Contains(x as int, y as int):
		return (x >= _x1 and x <= _x2 and y >= _y1 and y <= _y2)

	override def ToString():
		return "${_x1}, ${_y1}, ${_x2}, ${_y2}"

class SelectionInformation(DepthFirstVisitor):
	
	static def getHoverInformation(source as string, line as int, column as int):
		
		originalCompileUnit = Boo.Lang.Parser.BooParser.ParseString("none", source)
		
		compiler = BooCompiler()		
		compiler.Parameters.Pipeline = ResolveExpressions(BreakOnErrors: false)
		ctx = compiler.Run(originalCompileUnit.CloneNode())
		
		resolvedCompileUnit = ctx.CompileUnit
		selector = SelectionInformation(NodeInformationProvider(resolvedCompileUnit), line + 1, column + 1)
		ctx.Run: selector.VisitAllowingCancellation(originalCompileUnit)
		return selector.Info
		
	[getter(Info)]
	_info as string = ""

	_nodeInfoProvider as NodeInformationProvider
	_line as int
	_column as int
	
	def constructor(nodeInfoProvider as NodeInformationProvider, line as int, column as int):
		_nodeInfoProvider = nodeInfoProvider
		_line = line
		_column = column
		
	override def LeaveMemberReferenceExpression(node as MemberReferenceExpression):
		OnReferenceExpression(node)
		
	override def OnReferenceExpression(node as ReferenceExpression):
		MatchNode(node, len(node.Name))
		
	override def OnSimpleTypeReference(node as SimpleTypeReference):
		MatchNode(node, len(node.Name))
		
	def MatchNode(node as Node, length as int):
		region = GetNodeRectangle(node, length)
		if not region.Contains(_line, _column):
			return
		nodeInfo = _nodeInfoProvider.InfoFor(node)
		if nodeInfo is null:
			return
		_info = nodeInfo
		Cancel()
		
	private def GetNodeRectangle(node as Node, length as int):
		endLine = (node.LexicalInfo.Line if node.EndSourceLocation.Line == -1 else node.EndSourceLocation.Line)
		return Rectangle(node.LexicalInfo.Line, node.LexicalInfo.Column, endLine, node.LexicalInfo.Column + length)

		
class NodeInformationProvider(DepthFirstVisitor):
	
	_compileUnit as CompileUnit
	
	def constructor(resolvedCompileUnit as CompileUnit):
		_compileUnit = resolvedCompileUnit
	
	def InfoFor(node as Node):
		node = Resolve(node)
		if node is null:
			return "?"
			
		match TypeSystemServices.GetOptionalEntity(node):
			case ILocalEntity(Name: name, Type: t):
				return "${name} as ${t} - ${node.GetAncestor[of Method]().FullName}"
			case m=IMethod():
				return m.ToString()
			case IField(FullName: name, Type: t) | IProperty(FullName: name, Type: t):
				return "${name} as ${t}"
			case IType(FullName: name):
				return name
			case ITypedEntity(Type: IType(FullName: name)):
				return name
			otherwise:
				return "?"
						
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
		
						
	
