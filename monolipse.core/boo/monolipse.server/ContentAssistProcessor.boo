namespace monolipse.server

import Boo.Lang.Compiler
import Boo.Lang.Compiler.Pipelines
import Boo.Lang.Compiler.Ast
import Boo.Lang.Compiler.IO
import Boo.Lang.Compiler.Steps
import Boo.Lang.Compiler.TypeSystem

class ContentAssistProcessor(ProcessMethodBodiesWithDuckTyping):
	
	static final MemberAnchor = '__codecomplete__'
	
	static def getProposals(source as string):
		
		contentAssist = ContentAssistProcessor(source)
		compiler = BooCompiler()		
		compiler.Parameters.Pipeline = configurePipeline(contentAssist)
		compiler.Parameters.Input.Add(StringInput("none", source))
		compiler.Run()
		return contentAssist.Members

	[getter(Members)]
	_members = array(IEntity, 0)

	_code as string
	
	def constructor(code as string):
		_code = code
	
	override protected def ProcessMemberReferenceExpression(node as MemberReferenceExpression):
		if node.Name == MemberAnchor:
			_members = FilterSuggestions(getCompletionNamespace(node))
		else:
			super(node)
			
	def FilterSuggestions(entity as IEntity):
		ns = entity as INamespace
		return array(IEntity, 0) if ns is null
		return FilteredMembers(TypeSystemServices.GetAllMembers(ns))
		
	def FilteredMembers(members as (IEntity)):
		return array(
				item
				for item in members
				unless IsSpecial(item) or not IsAccessible(item))

	def IsSpecial(entity as IEntity):
		for prefix in ".", "___", "add_", "remove_", "raise_", "get_", "set_":
			return true if entity.Name.StartsWith(prefix)
			
	def IsAccessible(entity as IEntity):
		member = entity as IAccessibleMember
		return true if member is null or member.IsPublic
		
		declaringType = member.DeclaringType
		return true if	declaringType is self.CurrentType
		return true if member.IsInternal and member isa IInternalEntity
		return true if member.IsProtected and self.CurrentType.IsSubclassOf(declaringType)
		return false			
		
	protected def getCompletionNamespace(expression as MemberReferenceExpression) as INamespace:		
		target as Expression = expression.Target
		
		if target.ExpressionType is not null:
			if target.ExpressionType.EntityType != EntityType.Error:
				return cast(INamespace, target.ExpressionType)
		return cast(INamespace, TypeSystemServices.GetOptionalEntity(target))
	
	protected static def configurePipeline(hunter):
		pipeline = ResolveExpressions(BreakOnErrors: false)
		pipeline.Replace(Boo.Lang.Compiler.Steps.ProcessMethodBodiesWithDuckTyping, hunter)
		return pipeline
		