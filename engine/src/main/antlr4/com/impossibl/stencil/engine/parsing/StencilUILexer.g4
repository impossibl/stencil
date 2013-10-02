
lexer grammar StencilUILexer;

@members {
	public static final int STEXTS = DEFAULT_MODE;
	
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
	OUTPUTS_MODE,
	TEXTS_MODE,
	TEXTS_END
}

SCOMMENT: '$*' .*? '*$' -> type(COMMENT);

HEADERS_MODE: '$$' -> pushMode(OUTPUTS);
SOUTPUTS_MODE: '$' -> pushMode(OUTPUTS), type(OUTPUTS_MODE);
STEXTS_END: '}' -> popMode, type(TEXTS_END);

STEXT: (STEXTESC|~('$'|'}'))+;
fragment STEXTESC: '\\{' | '\\}';


mode DTEXTS;

DCOMMENT: '$*' .*? '*$' -> type(COMMENT);

DOUTPUTS_MODE: '$$' -> pushMode(OUTPUTS), type(OUTPUTS_MODE);
DTEXTS_END: '}}' -> popMode, type(TEXTS_END);

DTEXT_KEY1: '$' -> type(DTEXT);
DTEXT_KEY2: '}' -> type(DTEXT);

DTEXT: (DTEXTESC|~('$'|'}'))+;
fragment DTEXTESC: '\\{{' | '\\}}';

mode OUTPUTS;

COMMENT: '$*' .*? '*$';

DTEXTS_MODE: '{{' -> pushMode(DTEXTS), type(TEXTS_MODE);
STEXTS_MODE: {statementBlocks<0}? '{' -> pushMode(STEXTS), type(TEXTS_MODE);
OUTPUTS_END: {statementBlocks<=0}? ';' {resetStatementBlocks(); popMode();};

FUNC: 'func' {incStatementBlocks(); setType(KEYWORD);};

KEYWORD:
		'export'
	| 'macro'
	| 'func'
	| 'if'
	| 'else'
	| 'foreach'
	| 'in'
	| 'while'
	| 'break'
	| 'continue'
	| 'switch'
	| 'case'
	| 'default'
	| 'isa'
	| 'instanceof'
	| 'var'
	| 'return'
	| 'with'
	| 'assign'
	| 'before'
	| 'after'
	| 'replace'
	| 'include'
	| 'import'
	| 'global'
	| 'as'
	| 'null'
	| 'true'
	| 'false'
;

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

WS: (' '|'\t'|'\r'|'\n')+;

ANY: .;
