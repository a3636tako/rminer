package jp.ac.osaka_u.ist.sdl.rminer.parser;

//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import jp.ac.osaka_u.ist.sdl.rminer.parser.java.JavaParser;
import jp.ac.osaka_u.ist.sdl.rminer.parser.java.LexerTokenType;

public class JavaParserTest {
	private static final String TEST_CODE = "" + "package test.test;\n" + "import java.util.List;\n" + "public class A<T>{\n" + "	public List<A<T>> getGeneratedList(int value){\n" + "		return null;\n" + "	}\n" + "}\n";

	private static final List<Token> TEST_CODE_TOKENS;
	static {
		ArrayList<Token> tokens = new ArrayList<>();
		tokens.add(new Token("package", 0, 7, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.KEYWORD.name())));
		tokens.add(new Token("test", 8, 12, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token(".", 12, 13, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("test", 13, 17, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token(";", 17, 18, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("import", 19, 25, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.KEYWORD.name())));
		tokens.add(new Token("java", 26, 30, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token(".", 30, 31, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("util", 31, 35, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token(".", 35, 36, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("List", 36, 40, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token(";", 40, 41, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("public", 42, 48, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.KEYWORD.name())));
		tokens.add(new Token("class", 49, 54, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.KEYWORD.name())));
		tokens.add(new Token("A", 55, 56, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token("<", 56, 57, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("T", 57, 58, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token(">", 58, 59, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("{", 59, 60, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.BRACKET.name())));
		tokens.add(new Token("public", 62, 68, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.KEYWORD.name())));
		tokens.add(new Token("List", 69, 73, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token("<", 73, 74, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("A", 74, 75, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token("<", 75, 76, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("T", 76, 77, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token(">", 77, 78, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token(">", 78, 79, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("getGeneratedList", 80, 96, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token("(", 96, 97, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.BRACKET.name())));
		tokens.add(new Token("int", 97, 100, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.KEYWORD.name())));
		tokens.add(new Token("value", 101, 106, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.IDENTIFIER.name())));
		tokens.add(new Token(")", 106, 107, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.BRACKET.name())));
		tokens.add(new Token("{", 107, 108, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.BRACKET.name())));
		tokens.add(new Token("return", 111, 117, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.KEYWORD.name())));
		tokens.add(new Token("null", 118, 122, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.KEYWORD.name())));
		tokens.add(new Token(";", 122, 123, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.OPERATOR.name())));
		tokens.add(new Token("}", 125, 126, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.BRACKET.name())));
		tokens.add(new Token("}", 127, 128, Collections
			.singletonMap(JavaParser.LEXER_TOKEN_TYPE_KEY, LexerTokenType.BRACKET.name())));

		TEST_CODE_TOKENS = tokens;
	}

	@Test
	public void test() throws IOException {
		JavaParser parser = new JavaParser();
		Map<String, String> params = new HashMap<>();
		params.put("unit", "lexer");
		params.put("identifier", "none");
		parser.initialize(params);

		SourceCode sourceCode = loadSourceCode();
		TokenizedCode code = parser.tokenize(sourceCode);

		Iterator<Token> actual = code.tokens()
			.iterator();
		Iterator<Token> expected = TEST_CODE_TOKENS.iterator();

		while(expected.hasNext()) {
			Token a = actual.next();
			Token b = expected.next();
			assertThat(a.getSurface()).isEqualTo(b.getSurface());
			assertThat(a.getStartPosition()).isEqualTo(b.getStartPosition());
			assertThat(a.getEndPosition()).isEqualTo(b.getEndPosition());
			assertThat(a.getProperty(JavaParser.LEXER_TOKEN_TYPE_KEY))
				.isEqualTo(b.getProperty(JavaParser.LEXER_TOKEN_TYPE_KEY));
		}
		assertThat(actual.hasNext()).isFalse();
	}

	private SourceCode loadSourceCode() {
		return new SourceCode() {

			@Override
			public InputStream openStream() throws IOException {
				return new ByteArrayInputStream(TEST_CODE.getBytes());
			}

			@Override
			public String getFileName() {
				return "test/test/A.java";
			}

		};
	}

}
