namespace monolipse.server

import System.IO
import Boo.Lang.Compiler.Ast

import System.Collections.Generic

class OutlineVisitor(DepthFirstVisitor):
	
	_writer as TextWriter
	_imports = List[of Import]()
	
	def constructor(writer as TextWriter):
		_writer = writer
		
	override def OnModule(node as Module):
		ProcessImports(node)
		VisitCollection(node.Members)
		Visit(node.Globals)
		
	private def ProcessImports(node as Module):
		VisitCollection(node.Imports)
		WriteImportsCollection()

	private def WriteImportsCollection():
		WriteBeginNode()
		_writer.WriteLine("type=ImportCollection:name=import declarations:startline=${startLine()}:endline=${startLine() + len(_imports)}")
		for importNode in _imports:
			WriteBeginNode()
			WriteNodeLine(importNode)
			WriteEndNode()
			
		WriteEndNode()
	
	private def startLine():
		value = int.MaxValue
		_imports.ForEach do (node):
			if node.LexicalInfo.Line < value: 
				value = node.LexicalInfo.Line
				
		return value
				
	override def OnImport(node as Import):
		_imports.Add(node)
		
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
		WriteNodeLine(node, GetVisibility(node))
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
		_writer.WriteLine("type=${node.NodeType}:name=${describeNode(node)}:startline=${node.LexicalInfo.Line}:endline=${node.EndSourceLocation.Line}")
		
	def WriteNodeLine(node as Node, visibility as string):
		_writer.WriteLine("type=${node.NodeType}:name=${describeNode(node)}:startline=${node.LexicalInfo.Line}:endline=${node.EndSourceLocation.Line}:visibility=${visibility}")

	def GetVisibility(node as TypeMember):
		if node.IsVisibilitySet:
			return "Internal" if node.IsInternal
			return "Protected" if node.IsProtected
			return "Private" if node.IsPrivate
			return "Public" if node.IsPublic
			
		return "Internal"
		