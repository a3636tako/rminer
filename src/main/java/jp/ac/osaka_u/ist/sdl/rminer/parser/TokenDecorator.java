package jp.ac.osaka_u.ist.sdl.rminer.parser;

public interface TokenDecorator {
	boolean match(Token token);

	String decorate(Token token, String origin);
}
