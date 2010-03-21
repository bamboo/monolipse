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
		
	def Contains(x as int, y as int):
		return (x >= _x1 and x <= _x2 and y >= _y1 and y <= _y2)

	override def ToString():
		return "${_x1}, ${_y1}, ${_x2}, ${_y2}"

class SelectionInformation(ProcessMethodBodiesWithDuckTyping):
	
	static def getHoverInformation(source as string, line as int, column as int):
		selector = SelectionInformation(source, line + 1, column + 1)
		compiler = BooCompiler()		
		compiler.Parameters.Pipeline = configurePipeline(selector)
		compiler.Parameters.Input.Add(StringInput("none", source))
		compiler.Run()
		return selector.Info
		
	[getter(Info)]
	_info as string = ""

	_code as string
	_line as int
	_column as int
	
	def constructor(code as string, line as int, column as int):
		_code = code
		_line = line
		_column = column
	
	override def OnBinaryExpression(node as BinaryExpression):
		super(node)
		return unless node.Operator == BinaryOperatorType.Assign
		entity = TypeSystemServices.GetEntity(node.Left)
		return if entity.Name[0] == "$"
		
		region = GetNodeRectangle(node.Left, entity.Name)
		type = TryGetType(node.Left)
		
		if region.Contains(_line, _column):
			if entity.EntityType == EntityType.Field:
				_info = "${entity.FullName} as ${type}"
			if entity.EntityType == EntityType.Local:
				_info = "${entity.FullName} as ${type} - ${node.GetAncestor[of Method]()}"
		
	private def GetNodeRectangle(node as Expression, name as string):
		endLine = (node.LexicalInfo.Line if node.EndSourceLocation.Line == -1 else node.EndSourceLocation.Line)
		return Rectangle(node.LexicalInfo.Line, node.LexicalInfo.Column, endLine, node.LexicalInfo.Column + len(name))

	private def TryGetType(node as Expression):
		try:
			entity = TypeSystemServices.GetEntity(node)
			typedEntity = entity as ITypedEntity
			return typedEntity.Type if typedEntity
		except e:
			print e
			
		return "?"
		
	protected static def configurePipeline(hunter):
		pipeline = ResolveExpressions(BreakOnErrors: false)
		pipeline.Replace(Boo.Lang.Compiler.Steps.ProcessMethodBodiesWithDuckTyping, hunter)
		return pipeline
