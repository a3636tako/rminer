package jp.ac.osaka_u.ist.sdl.rminer.parser;

import java.util.Map;

public interface Tokenizer {
	void initialize(Map<String, String> parameters);

	boolean isTokenizable(SourceCode sourceCode);

	TokenizedCode tokenize(SourceCode sourceCode);
}
