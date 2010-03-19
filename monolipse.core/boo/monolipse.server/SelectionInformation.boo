namespace monolipse.server

import Boo.Lang.Compiler
import Boo.Lang.Compiler.Pipelines
import Boo.Lang.Compiler.Ast
import Boo.Lang.Compiler.IO
import Boo.Lang.Compiler.Steps
import Boo.Lang.Compiler.TypeSystem

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

	override def ToString():
		return "${_x1}, ${_y1}, ${_x2}, ${_y2}"

class SelectionInformation(ProcessMethodBodiesWithDuckTyping):
	
	static def getHoverInformation(source as string, offset as int, length as int):
		selector = SelectionInformation(source, offset, length)
		compiler = BooCompiler()		
		compiler.Parameters.Pipeline = configurePipeline(selector)
		compiler.Parameters.Input.Add(StringInput("none", source))
		compiler.Run()
		return selector.Info

	[getter(Info)]
	_info as string = ""

	_code as string
	_region as Rectangle
	
	def constructor(code as string, offset as int, length as int):
		_code = code
		_region = Rectangle()
	
	override def OnBinaryExpression(node as BinaryExpression):
		super(node)
		endLine = (node.LexicalInfo.Line if node.EndSourceLocation.Line == -1 else node.EndSourceLocation.Line)
		endColumn = (node.LexicalInfo.Column if node.EndSourceLocation.Column == -1 else node.EndSourceLocation.Column)
		nodeRegion = Rectangle(node.LexicalInfo.Line, node.LexicalInfo.Column, endLine, endColumn)
		entity = TypeSystemServices.GetEntity(node.Left)
		print nodeRegion, entity.Name
	
	protected static def configurePipeline(hunter):
		pipeline = ResolveExpressions(BreakOnErrors: false)
		pipeline.Replace(Boo.Lang.Compiler.Steps.ProcessMethodBodiesWithDuckTyping, hunter)
		return pipeline
