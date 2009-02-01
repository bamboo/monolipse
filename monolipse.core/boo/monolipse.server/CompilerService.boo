namespace monolipse.server

import System
import System.IO
import Boo.Lang.Compiler
import Boojay.Compilation
import monolipse.core

service CompilerService:
	
	onMessageWithResponse "GET-OUTLINE":
		module = parse(message.Payload)
		module.Accept(OutlineVisitor(response))
			
	onMessageWithResponse "GET-COMPILER-PROPOSALS":
		proposals = ContentAssistProcessor.getProposals(message.Payload)
		writeTypeSystemEntitiesTo proposals, response
			
	onMessageWithResponse "EXPAND-BOO":
		result = compileCodeWithPipeline(message.Payload, Pipelines.Compile())
		writeExpansionResultTo result, response
		
	onMessageWithResponse "EXPAND-BOOJAY":
		result = compileCodeWithBoojay(message.Payload, BoojayPipelines.BoojayCompilation())
		writeExpansionResultTo result, response
		
	onMessageWithResponse "EXPAND-BOO-MACROS":
		result = compileCodeWithPipeline(message.Payload, Pipelines.ExpandMacros(BreakOnErrors: false))
		writeExpansionResultTo result, response 
		
	onMessageWithResponse "EXPAND-BOOJAY-MACROS":
		result = compileCodeWithBoojay(message.Payload, newExpandBoojayMacrosPipeline())
		writeExpansionResultTo result, response
		
def newExpandBoojayMacrosPipeline():
	pipeline = Pipelines.ExpandMacros(BreakOnErrors: false)
	BoojayPipelines.PatchBooPipeline(pipeline)
	return pipeline
		
def writeExpansionResultTo(result as CompilerContext, response as TextWriter):
	response.WriteLine(result.CompileUnit.ToCodeString())
	if len(result.Errors) > 0:
		response.Write("// ")
		response.WriteLine(join(result.Errors, "\n// "))
	
def parse(code as string):
	result = compileCodeWithPipeline(code, Pipelines.Parse())
	return result.CompileUnit.Modules[0]
	
def compileCodeWithBoojay(code as string, pipeline as CompilerPipeline):
	compiler = newBoojayCompiler(pipeline)
	compiler.Parameters.Input.Add(compilerInputFor(code))
	return compiler.Run()
	
def compileCodeWithPipeline(code as string, pipeline as CompilerPipeline):
	compiler = newCompilerWithPipeline(pipeline)
	compiler.Parameters.Input.Add(compilerInputFor(code))
	return compiler.Run()
	
def compilerInputFor(code as string):
	return Boo.Lang.Compiler.IO.StringInput("code", code)
	
def newCompilerWithPipeline(pipeline as CompilerPipeline):
	return BooCompiler(CompilerParameters(Pipeline: pipeline))