namespace monolipse.server

import System.IO
import Boo.Lang.Compiler.Ast

class OutlineVisitor(DepthFirstVisitor):
	
	_writer as TextWriter
	
	def constructor(writer as TextWriter):
		_writer = writer
		
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
		WriteTypeMember(node)
		Visit(node.Getter)
		Visit(node.Setter)
		WriteEndNode()	
		
	override def OnEvent(node as Event):
		WriteMemberNode(node)
		
	def WriteMemberNode(node as TypeMember):
		WriteBeginNode()
		WriteTypeMember(node)
		WriteEndNode()
		
	def WriteTypeDefinition(node as TypeDefinition):
		WriteBeginNode()
		WriteTypeMember(node)
		Visit(node.Members)
		WriteEndNode()
		
	def WriteBeginNode():
		_writer.WriteLine("BEGIN-NODE")
		
	def WriteEndNode():
		_writer.WriteLine("END-NODE")
		
	def WriteTypeMember(node as TypeMember):
		_writer.WriteLine("${node.NodeType}:${describeNode(node)}:${node.LexicalInfo.Line}")
