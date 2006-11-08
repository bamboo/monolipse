namespace monolipse.server

import Boo.Lang.Interpreter
import Boo.Lang.Compiler
import Boo.Lang.Compiler.Ast
import Boo.Lang.Compiler.TypeSystem

def describeEntity(entity as IEntity):
	ie = entity as IInternalEntity
	return describeNode(ie.Node) if ie is not null
	return InteractiveInterpreter.DescribeEntity(entity)
	
def describeNode(node as Node):
	field = node as Field
	return describeField(field) if field is not null
	
	method = node as Method
	return describeMethod(method) if method is not null
	
	p = node as Property
	return describeProperty(p) if p is not null
	
	member = node as TypeMember
	return member.Name if member is not null
	
	return node.ToString()
	
def describeField(field as Field):
	return "${field.Name}${optionalTypeReference(field.Type)}"

def describeProperty(p as Property):
	parameters = ""
	if len(p.Parameters) > 0:
		parameters = "(${describeParameters(p.Parameters)})"	
	return "${p.Name}${parameters}${optionalTypeReference(p.Type)}"
	
def describeMethod(method as Method):
	returnType = optionalTypeReference(method.ReturnType)
	return "${method.Name}(${describeParameters(method.Parameters)})${returnType}"

def describeParameters(parameters as ParameterDeclarationCollection):
	return join(describeParameter(p) for p in parameters, ', ')
	
def describeParameter(p as ParameterDeclaration):
	return "${p.Name}${optionalTypeReference(p.Type)}"
	
def optionalTypeReference(t as TypeReference):
	return "" if t is null
	
	external = TypeSystemServices.GetOptionalEntity(t) as ExternalType
	if external is not null:
		return " as ${InteractiveInterpreter.GetBooTypeName(external.ActualType)}"
	return " as ${t}" if t is not null