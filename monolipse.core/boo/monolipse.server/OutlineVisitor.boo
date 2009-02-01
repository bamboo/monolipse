namespace monolipse.server

import System.IO
import Boo.Lang.Compiler.Ast

class OutlineVisitor(DepthFirstVisitor):
	
	_writer as TextWriter
	
	def constructor(writer as TextWriter):
		_writer = writer
		
	override def OnModule(node as Module):
		VisitCollection(node.Members)
		Visit(node.Globals)
		
	override def OnMacroStatement(node as MacroStatement):
		WriteBeginNode()
		WriteNodeLine(node)
		Visit(node.Body)
		WriteEndNode()
		
	override def OnClassDefinition(node as ClassDefinition):
		WriteTypeDefinition(node)
		
	override def OnInterfaceDefinition(node as InterfaceDefinition):
		WriteTypeDefinition(node)
		
	override def OnStructDefinition(node as StructDefinition):
		WriteTypeDefinition(node)
		
	override def OnEnumDefinition(node as EnumDefinition):
		WriteTypeDefinition(node)
		
	override def OnCallableDefinition(node as CallableDefinition):
		WriteMemberNode(node)
		
	override def OnMethod(node as Method):
		WriteMemberNode(node)
		
	override def OnField(node as Field):
		WriteMemberNode(node)
		
	override def OnConstructor(node as Constructor):
		WriteMemberNode(node)
		
	override def OnDestructor(node as Destructor):
		WriteMemberNode(node)
		
	override def OnProperty(node as Property):
		WriteBeginNode()
		WriteNodeLine(node)
		Visit(node.Getter)
		Visit(node.Setter)
		WriteEndNode()	
		
	override def OnEvent(node as Event):
		WriteMemberNode(node)
		
	def WriteMemberNode(node as TypeMember):
		WriteBeginNode()
		WriteNodeLine(node)
		WriteEndNode()
		
	def WriteTypeDefinition(node as TypeDefinition):
		WriteBeginNode()
		WriteNodeLine(node)
		VisitCollection(node.Members)
		WriteEndNode()
		
	def WriteBeginNode():
		_writer.WriteLine("BEGIN-NODE")
		
	def WriteEndNode():
		_writer.WriteLine("END-NODE")
		
	def WriteNodeLine(node as Node):
		_writer.WriteLine("${node.NodeType}:${describeNode(node)}:${node.LexicalInfo.Line}")
