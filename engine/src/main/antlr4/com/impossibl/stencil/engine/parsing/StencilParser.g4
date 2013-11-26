
parser grammar StencilParser;

options {
	tokenVocab=StencilLexer;
}



template:
	hdr=header contents+=content+
;

content:
	  def=definition
	| out=output
;



header:
	TEXTWS* hdrSig=headerSignature? (TEXTWS* hdrGlobals+=headerGlobal | TEXTWS* hdrImports+=headerImport)* TEXTWS*
;

headerSignature:
	HEADERS_MODE callSig=callableSignature prepSig=prepareSignature? OUTPUTS_END
;

headerGlobal:
	HEADERS_MODE GLOBAL ids+=ID (COMMA ids+=ID)* OUTPUTS_END
;

headerImport:
	HEADERS_MODE IMPORT importers+=importer (COMMA importers+=importer)* OUTPUTS_END
;

importer:
		qName=qualifiedName (AS id=ID)?				#typeImporter
	|	stringLit=stringLiteral (AS id=ID)?		#templateImporter 
;




output:
		textOut=textOutput
	| dynOut=dynamicOutput
;

textOutput:
		values+=(TEXTWS|TEXT)+
;

dynamicOutput:
	OUTPUTS_MODE
	(
			exprOut=expressionOutput
		| declOut=declarationOutput
		| incOut=includeOutput
		| assignOut=assignOutput
		| ifOut=ifOutput
		| foreachOut=foreachOutput
		| whileOut=whileOutput
		| breakOut=breakOutput
		| contOut=continueOutput
		| switchOut=switchOutput
		| withOut=withOutput
	)
	OUTPUTS_END
;

expressionOutput:
		expr=expression prep=prepareInvocation?
;

declarationOutput:
		VAR vars+=variableDecl (COMMA vars+=variableDecl)*
;

includeOutput:
		INCLUDE stringLit=stringLiteral call=callableInvocation? prep=prepareInvocation?
;

assignOutput:
		ASSIGN assignments+=assignment (COMMA assignments+=assignment)*
;

ifOutput:
	IF expr=expression thenBlock=outputBlock ('else' elseBlock=outputBlock)?
;

foreachOutput:
	FOREACH value=variableDecl (COMMA iterator=variableDecl)? IN expr=expression iterBlock=outputBlock ('else' elseBlock=outputBlock)?
;

whileOutput:
	WHILE expr=expression block=outputBlock
;

breakOutput:
	BREAK
;

continueOutput:
	CONTINUE
;

switchOutput:
	SWITCH expr=expression cases+=switchOutputCase+
;

withOutput:
	WITH expr+=expression (COMMA expr+=expression)* block=outputBlock
;



switchOutputCase:
		CASE expr=expression block=outputBlock		#switchOutputValueCase
	|	DEFAULT block=outputBlock									#switchOutputDefaultCase
;




definition:
	OUTPUTS_MODE
	(
			callDef=callableDefinition
		| exportDef=exportDefinition
	)
	OUTPUTS_END
;

exportDefinition:
	EXPORT vars+=variableDecl (COMMA vars+=variableDecl)*
;

callableDefinition:
		FUNC id=ID callSig=callableSignature blockStmt=blockStatement													#functionDefinition
	| MACRO id=ID callSig=callableSignature? prepSig=prepareSignature? block=outputBlock		#macroDefinition
;




statement:
		blockStmt=blockStatement
	| exprStmt=expressionStatement
	| assignmentStmt=assignmentStatement
	| declStmt=declarationStatement
	| returnStmt=returnStatement
	| ifStmt=ifStatement
	| foreachStmt=foreachStatement
	| whileStmt=whileStatement
	| breakStmt=breakStatement
	| continueStmt=continueStatement
	| switchStmt=switchStatement
	| withStmt=withStatement
;

blockStatement:
	BLOCK_OPEN stmts+=statement* BLOCK_CLOSE
;

expressionStatement:
	expr=expression SEMI
;

assignmentStatement:
	assignmnt=assignment SEMI
;

declarationStatement:
	VAR vars+=variableDecl (COMMA vars+=variableDecl)* SEMI
;

returnStatement:
	RETURN expr=expression SEMI
;

ifStatement:
	IF expr=expression thenStmt=statement ('else' elseStmt=statement)?
;

foreachStatement:
	FOREACH value=variableDecl (COMMA iterator=variableDecl)? IN expr=expression iterStmt=statement ('else' elseStmt=statement)?
;

whileStatement:
	WHILE expr=expression stmt=statement
;

breakStatement:
	BREAK SEMI
;

continueStatement:
	CONTINUE SEMI
;

switchStatement:
	SWITCH expr=expression BLOCK_OPEN cases+=switchStatementCase+ BLOCK_CLOSE
;

switchStatementCase:
		CASE expr=expression COLON stmt=statement		#switchStatementValueCase
	| DEFAULT COLON stmt=statement								#switchStatementDefaultCase
;

withStatement:
	WITH expr+=expression (COMMA expr+=expression)* stmt=statement
;




expression:
		lit=literal																																									#literalExpression
	| varRef=variableRef																																					#variableRefExpression
	| PAREN_OPEN expr=expression PAREN_CLOSE																											#parenExpression
	| expr=expression sels+=selector+																															#selectorExpression
	| operator=(BIT_NEG|LOG_NEG) expr=expression																									#unaryExpression
	| operator=(ADD|SUB|INC|DEC) expr=expression																									#unaryExpression
	| leftExpr=expression operator=(MUL|DIV|MOD) rightExpr=expression															#binaryExpression
	| leftExpr=expression operator=(ADD|SUB) rightExpr=expression																	#binaryExpression
	| leftExpr=expression operator=(LSHIFT|RSHIFT) rightExpr=expression														#binaryExpression
	| leftExpr=expression operator=(GT|GTEQUAL|LT|LTEQUAL) rightExpr=expression										#binaryExpression
	| expr=expression (ISA|INSTANCEOF) qName=qualifiedName																				#instanceExpression
	| leftExpr=expression operator=(EQUAL|NEQUAL|IDENTICAL|NIDENTICAL) rightExpr=expression				#binaryExpression
	| leftExpr=expression operator=BIT_AND rightExpr=expression																		#binaryExpression
	| leftExpr=expression operator=BIT_XOR rightExpr=expression																		#binaryExpression
	| leftExpr=expression operator=BIT_OR  rightExpr=expression																		#binaryExpression
	| leftExpr=expression operator=LOG_AND rightExpr=expression																		#binaryExpression
	| leftExpr=expression operator=LOG_OR  rightExpr=expression																		#binaryExpression
	| test=expression QUEST
	  	(
	  			trueExpr=expression COLON falseExpr=expression
	  		|	trueExpr=expression
	  		|	COLON falseExpr=expression
	  	)																																													#ternaryExpression
;

variableRef:
	id=ID
;

lValueRef:
	id=ID refSel+=refSelector*
;

selector:
	valSel=valueSelector |
	refSel=refSelector
;

valueSelector:
	  DOT id=ID call=callableInvocation		#methodCallSelector
	| call=callableInvocation							#callSelector
;

refSelector:
	  DOT id=ID SQUARE_OPEN expr=expression SQUARE_CLOSE		#memberIndexSelector
	| DOT id=ID																							#memberSelector
	| QUESTDOT id=ID																				#safeMemberSelector
	| SQUARE_OPEN expr=expression SQUARE_CLOSE							#indexSelector
;




literal:
		nullLiteral
	| booleanLit=booleanLiteral
	| stringLit=stringLiteral
	| numberLit=numberLiteral
	| listLit=listLiteral
	| mapLit=mapLiteral
	| rangeLit=rangeLiteral
;

nullLiteral:
	NULL
;

booleanLiteral:
	value=(TRUE | FALSE)
;

numberLiteral:
	integerLit=integerLiteral | floatingLit=floatingLiteral
;

integerLiteral:
	value=INTEGER
;

floatingLiteral:
	value=FLOAT
;

stringLiteral:
	value=(SSTRING | DSTRING)
;

listLiteral:
		SQUARE_OPEN expr+=expression (COMMA expr+=expression)* SQUARE_CLOSE
	| SQUARE_OPEN SQUARE_CLOSE
;

mapLiteral:
		SQUARE_OPEN namedValues+=namedValue (COMMA namedValues+=namedValue)* SQUARE_CLOSE
	| SQUARE_OPEN EQL_ASSIGN SQUARE_CLOSE
;

rangeLiteral:
		SQUARE_OPEN from=expression DOTDOT to=expression SQUARE_CLOSE
	| SQUARE_OPEN DOTDOT SQUARE_CLOSE
;


namedValue:
	name=simpleName EQL_ASSIGN expr=expression
;

simpleName:
	id=ID|stringLit=stringLiteral
;

qualifiedName:
	ids+=ID (DOT ids+=ID)*
;



callableSignature
@after {
  boolean unbounds=false;
  for(ParameterDeclContext paramDecl : $paramDecls) {
    if( paramDecl.flag != null && paramDecl.flag.getText().equals("*") ) {
      if(unbounds) {
        throw new InvalidSignatureException("macros can only mark a single parameter for unbound", this, _input, _ctx, paramDecl.flag);
      }
      unbounds = true;
    }
  }
}
: PAREN_OPEN (paramDecls+=parameterDecl (COMMA paramDecls+=parameterDecl)*)? PAREN_CLOSE
;

parameterDecl:
		(flag=MUL)? id=ID (EQL_ASSIGN expr=expression)?
;


prepareSignature
@after {
  boolean unbounds=false, unnamed=false;
  for(BlockDeclContext paramDecl : $blockDecls) {
    if( paramDecl.flag != null && paramDecl.flag.getText().equals("*") ) {
      if(unbounds) {
        throw new InvalidSignatureException("macros can only mark a single block for unbound", this, _input, _ctx, paramDecl.flag);
      }
      unbounds = true;
    }
    if( paramDecl.flag != null && paramDecl.flag.getText().equals("+") ) {
      if(unnamed) {
        throw new InvalidSignatureException("macros can only mark a single block for unnamed", this, _input, _ctx, paramDecl.flag);
      }
      unnamed = true;
    }
  }
}
: SQUARE_OPEN (blockDecls+=blockDecl (COMMA blockDecls+=blockDecl)*)? SQUARE_CLOSE
;

blockDecl:
  (flag=(ADD|MUL))? id=ID
;

callableInvocation:
	PAREN_OPEN (posParams=positionalParameters|namedParams=namedParameters) PAREN_CLOSE
;

positionalParameters:
	(exprs+=expression (COMMA exprs+=expression)*)?
;

namedParameters:
	(namedValues+=namedValue (COMMA namedValues+=namedValue)*)?
;

prepareInvocation:
		unnamedBlock=unnamedOutputBlock
	| unnamedBlock=unnamedOutputBlock? namedBlocks+=namedOutputBlock+
;





outputBlock:
	TEXTS_MODE outputs+=output* TEXTS_END
;

paramOutputBlockMode:
	value=(REPLACE | BEFORE | AFTER)
;

paramOutputBlock:
		unnamedBlock=unnamedOutputBlock
	| namedBlock=namedOutputBlock
;

unnamedOutputBlock:
	blockMode=paramOutputBlockMode? block=outputBlock
;

namedOutputBlock:
	blockMode=paramOutputBlockMode? id=ID block=outputBlock
;




assignment:
	lValRef=lValueRef assignmntOper=assignmentOperator expr=expression
;

assignmentOperator:
	EQL_ASSIGN |
	ADD_ASSIGN |
	SUB_ASSIGN |
	MUL_ASSIGN |
	DIV_ASSIGN |
	MOD_ASSIGN |
	OR_ASSIGN  |
	XOR_ASSIGN |
	AND_ASSIGN |
	LSHIFT_ASSIGN |
	RSHIFT_ASSIGN
;



variableDecl:
	id=ID (EQL_ASSIGN expr=expression)?
;

