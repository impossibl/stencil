
lexer grammar StencilLexer;

@members {
	public static final int STEXTS = DEFAULT_MODE;
	public static final int KEYWORD_FIRST=EXPORT;
	public static final int KEYWORD_LAST=FALSE;
	
	private int statementBlocks=-1;
	
	void resetStatementBlocks() {
		statementBlocks=-1;
	}
	void incStatementBlocks() {
		statementBlocks+=1;
	}
	void decStatementBlocks() {
		statementBlocks-=1;
	}
	
	@Override
	public int popMode() {
		if ( _modeStack.isEmpty() ) {
			pushMode(STEXTS);
		}
		return super.popMode();
	}

}

tokens {
	TEXT,
	TEXTWS,
	OUTPUTS_MODE,
	TEXTS_MODE,
	TEXTS_END
}

SCOMMENT: '$*' .*? '*$' -> channel(HIDDEN);

STEXTESC_OUTS: '\\$' { setText("$"); setType(TEXT); };
STEXTESC_OPEN: '\\{' { setText("{"); setType(TEXT); };
STEXTESC_CLOS: '\\}' { setText("}"); setType(TEXT); };

HEADERS_MODE: '$$' -> pushMode(OUTPUTS);
SOUTPUTS_MODE: '$' -> pushMode(OUTPUTS), type(OUTPUTS_MODE);
STEXTS_END: '}' -> popMode, type(TEXTS_END);

STEXTWS: [ \t\r\n]+ -> type(TEXTWS);

STEXT_KEYS: [\\$}] -> type(TEXT);

STEXT: (~[\\$}])+ -> type(TEXT);

mode DTEXTS;

DCOMMENT: '$*' .*? '*$' -> channel(HIDDEN);

DTEXTESC_OUTS: '\\$$' { setText("$$"); setType(TEXT); };
DTEXTESC_OPEN: '\\{{' { setText("{{"); setType(TEXT); };
DTEXTESC_CLOS: '\\}}' { setText("}}"); setType(TEXT); };

DOUTPUTS_MODE: '$$' -> pushMode(OUTPUTS), type(OUTPUTS_MODE);
DTEXTS_END: '}}' -> popMode, type(TEXTS_END);

DTEXTWS: [ \t\r\n]+ -> type(TEXTWS);

DTEXT_KEYS: [\\$}] -> type(TEXT);

DTEXT: (~[\\$}])+ -> type(TEXT);

mode OUTPUTS;

COMMENT1: '$*' .*? '*$' -> channel(HIDDEN);
COMMENT2: '//'..'\n' -> channel(HIDDEN);

DTEXTS_MODE: '{{' -> pushMode(DTEXTS), type(TEXTS_MODE);
STEXTS_MODE: {statementBlocks<0}? '{' -> pushMode(STEXTS), type(TEXTS_MODE);
OUTPUTS_END: {statementBlocks<=0}? ';' {resetStatementBlocks(); popMode();};

EXPORT: 'export';
MACRO: 'macro';
FUNC: 'func' {incStatementBlocks();};

IF: 'if';
ELSE: 'else';
FOREACH: 'foreach';
IN: 'in';
WHILE: 'while';
BREAK: 'break';
CONTINUE: 'continue';
SWITCH: 'switch';
CASE: 'case';
DEFAULT: 'default';
ISA: 'isa';
INSTANCEOF: 'instanceof';
VAR: 'var';
RETURN: 'return';
WITH: 'with';
ASSIGN: 'assign';
BEFORE: 'before';
AFTER: 'after';
REPLACE: 'replace';
INCLUDE: 'include';
IMPORT: 'import';
GLOBAL: 'global';
AS: 'as';
NULL: 'null';

TRUE: 'true';
FALSE: 'false';

QUESTDOT: '?.';
DOTDOT: '..';
DOT: '.';
SEMI: ';';
COLON: ':';
COMMA: ',';
QUEST: '?';

IDENTICAL: '===';
NIDENTICAL: '!==';
LSHIFT_ASSIGN: '<<=';
RSHIFT_ASSIGN: '>>=';

ADD_ASSIGN: '+=';
SUB_ASSIGN: '-=';
MUL_ASSIGN: '*=';
DIV_ASSIGN: '/=';
MOD_ASSIGN: '%=';
AND_ASSIGN: '&=';
XOR_ASSIGN: '^=';
OR_ASSIGN:  '|=';

INC: '++';
DEC: '--';

LSHIFT: '<<';
RSHIFT: '>>';

EQUAL: '==';
NEQUAL: '!=';
GTEQUAL: '>=';
LTEQUAL: '<=';

LOG_OR:  '||';
LOG_AND: '&&';

ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';
MOD: '%';

LOG_NEG: '!';

BIT_NEG: '~';
BIT_OR:  '|';
BIT_XOR: '^';
BIT_AND: '&';

GT: '>';
LT: '<';

EQL_ASSIGN: '=';

PAREN_OPEN:  '(';
PAREN_CLOSE: ')';

SQUARE_OPEN:  '[';
SQUARE_CLOSE: ']';

BLOCK_OPEN:  '{'  {incStatementBlocks();};
BLOCK_CLOSE: '}'  {decStatementBlocks();};

ID: [a-zA-Z_] [a-zA-Z0-9_]*;

SSTRING: '\'' (SSTRINGESC|.)*? '\'';
DSTRING: '\"' (DSTRINGESC|.)*? '\"';

fragment SSTRINGESC: '\\\'' | '\\\\';
fragment DSTRINGESC: '\\\"' | '\\\\';

INTEGER: INT [sil]?;

FLOAT: (INT '.' INT EXP? | INT EXP) [fd]?; 
	
fragment INT: [0-9]+;
fragment EXP: [Ee] [+\-]? INT;

WS: [ \t\r\n]+ -> channel(HIDDEN);
