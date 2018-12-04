package jp.ac.osaka_u.ist.sdl.rminer.parser.java;

/**
 * 構文解析でのトークンタイプ
 */
public enum ParserTokenType {
	KEYWORD, BRACKET, LITERAL, OPERATOR, COMMENT, TYPE_IDENTIFIER, VARIABLE_IDENTIFIER, QUALIFY_IDENTIFIER,

	UNKNOWN,
	;
}
