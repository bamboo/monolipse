namespace monolipse.server

import System
import Boo.Lang.Compiler
import monolipse.core

service CompilerService:
	
	onMessageWithResponse "GET-OUTLINE":
		module = parse(message.Payload)
		module.Accept(OutlineVisitor(response))
			
	onMessageWithResponse "GET-COMPILER-PROPOSALS":
		proposals = ContentAssistProcessor.getProposals(message.Payload)
		writeTypeSystemEntitiesTo proposals, response
			
	onMessageWithResponse "EXPAND":
		result = compileCodeWithPipeline(message.Payload, Pipelines.Compile())
		writeLine result.CompileUnit.ToCodeString()
		
	onMessageWithResponse "EXPAND-MACROS":
		result = compileCodeWithPipeline(message.Payload, Pipelines.ExpandMacros())
		writeLine result.CompileUnit.ToCodeString()
	

def parse(code as string):
	result = compileCodeWithPipeline(code, Pipelines.Parse())
	return result.CompileUnit.Modules[0]
	
def compileCodeWithPipeline(code as string, pipeline as CompilerPipeline):
	compiler = newCompilerWithPipeline(pipeline)
	compiler.Parameters.Input.Add(Boo.Lang.Compiler.IO.StringInput("code", code))
	return compiler.Run()
	
def newCompilerWithPipeline(pipeline as CompilerPipeline):
	return BooCompiler(CompilerParameters(Pipeline: pipeline))