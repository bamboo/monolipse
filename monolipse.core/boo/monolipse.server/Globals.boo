namespace monolipse.server

import Boo.Lang.Interpreter.Builtins
import Boo.Lang.Compiler.Ast
import Boo.Lang.Compiler.TypeSystem
import Boo.Lang.Compiler.TypeSystem.Reflection
import Boo.Lang.PatternMatching

def getEntityType(entity as IEntity):
	if EntityType.Type == entity.EntityType:
		type = entity as IType
		return "Interface" if type.IsInterface
		return "Enum" if type.IsEnum
		return "Struct" if type.IsValueType
		return "Callable" if type isa ICallableType
		return "Class"
	return entity.EntityType.ToString()
	
def writeTypeSystemEntitiesTo(entities as (IEntity), response as System.IO.TextWriter):
	for member in entities:
		try:
			response.WriteLine("${getEntityType(member)}:${member.Name}:${describeEntity(member)}")
		except x:
			print x

def describeEntity(entity as IEntity):
	ie = entity as IInternalEntity
	return describeNode(ie.Node) if ie is not null
	return DescribeEntity(entity)
	
def describeNode(node as Node):
	match node:
		case field = Field():
			return describeField(field)
		case method = Method():
			return describeMethod(method)
		case p = Property():
			return describeProperty(p)
		case member = TypeMember():
			return member.Name
		case macro = MacroStatement(Name: macroName, Arguments: args):
			if len(args) > 0: 
				return "${macroName} ${args[0]}"
			return macroName
		otherwise:
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
		return " as ${GetBooTypeName(external.ActualType)}"
	return " as ${t}" if t is not null