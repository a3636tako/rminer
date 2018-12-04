package jp.ac.osaka_u.ist.sdl.rminer.parser.java;

import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;
import jp.ac.osaka_u.ist.sdl.rminer.parser.TokenDecorator;

public class JavaDecorator implements TokenDecorator {

	@Override
	public boolean match(Token token) {
		return true;
	}

	@Override
	public String decorate(Token token, String origin) {
		switch(LexerTokenType.valueOf(token.getProperty(JavaParser.LEXER_TOKEN_TYPE_KEY))){
		case BRACKET:
			return "<span style='font-weight:bold;'>" + origin + "</span>";
		case COMMENT:
			return origin;
		case IDENTIFIER:
			return origin;
		case KEYWORD:
			return "<span style='color:#a71d5d;'>" + origin + "</span>";
		case LITERAL:
			return "<span style='color:#183691;'>" + origin + "</span>";
		case OPERATOR:
			return "<span style='font-weight:bold;'>" + origin + "</span>";
		case SPACE:
			return origin;
		case UNKNOWN:
			return origin;
		default:
			return origin;
		}
	}

}
