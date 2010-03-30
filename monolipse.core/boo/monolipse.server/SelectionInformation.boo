namespace monolipse.server

import Boo.Lang.Compiler.Ast

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
	static def HoverInformationFor(source as string, line as int, column as int):
		resolution = ExpressionResolution.ForCodeString(source)
		selector = SelectionInformation(resolution.NodeInformationProvider, line + 1, column + 1)
		resolution.RunInResolvedCompilerContext: selector.VisitAllowingCancellation(resolution.OriginalCompileUnit)
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
		nodeInfo = _nodeInfoProvider.TooltipFor(node)
		if nodeInfo is null:
			return
		_info = nodeInfo
		Cancel()
		
	private def GetNodeRectangle(node as Node, length as int):
		endLine = (node.LexicalInfo.Line if node.EndSourceLocation.Line == -1 else node.EndSourceLocation.Line)
		return Rectangle(node.LexicalInfo.Line, node.LexicalInfo.Column, endLine, node.LexicalInfo.Column + length)
