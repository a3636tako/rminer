package jp.ac.osaka_u.ist.sdl.rminer.parser.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Statement;

import jp.ac.osaka_u.ist.sdl.rminer.parser.Document;
import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;
import jp.ac.osaka_u.ist.sdl.rminer.parser.TokenizedCode;

/**
 * 字句解析されたソースコードの各トークンに対して、構文的な情報を付加する
 * 文書に分割する
 */
public class AbstractDocumentGenerator extends TokenizeVistor {
	/** 識別子分割用正規表現 */
	private static final Pattern IDENTIFIER_SPLIT_PATTERN = Pattern
		.compile("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z])(?=[A-Z])|_|(?<=[0-9])(?=[a-zA-Z])|(?<=[a-zA-Z])(?=[0-9])");

	/** Documentの識別子となる属性のKey */
	public static final String PROPERTY_PATH_KEY = "path";

	private Document.Builder documentBuilder;
	private List<Document> documents;
	private ASTParser parser = ASTParser.newParser(AST.JLS8);
	private Iterator<Token> tokenIterator;
	private Token current;
	private SourceCode origin;
	private TokenizedCode lexicalDocument;
	private boolean normalizeIdentifier;
	private boolean splitIdentifier;
	private boolean identifyTokenType;
	private boolean contextSensitiveSymbols;

	/**
	 * 字句解析されたソースコードの各トークンに対して、構文的な情報を付加する
	 * @param origin もととなるソースコード
	 * @param lexicalDocument 字句解析結果
	 * @return 構文情報を付加したトークン列
	 * @throws IOException
	 */
	public TokenizedCode parse(SourceCode origin, TokenizedCode lexicalDocument) throws IOException {
		this.origin = origin;
		this.lexicalDocument = lexicalDocument;

		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);

		parser.setCompilerOptions(options);
		parser.setSource(origin.loadCharData());
		documentBuilder = null;
		documents = new ArrayList<>();

		tokenIterator = lexicalDocument.tokens()
			.iterator();
		nextToken();

		ASTNode unit = parser.createAST(null);
		unit.accept(this);

		return new TokenizedCode(documents, lexicalDocument.getProperties());
	}

	public SourceCode getOriginalSourceCode() {
		return origin;
	}

	public TokenizedCode getLexicalDocument() {
		return lexicalDocument;
	}

	public boolean isNormalizeIdentifier() {
		return normalizeIdentifier;
	}

	/**
	 * 識別子を正規化するかどうか
	 */
	public void setNormalizeIdentifier(boolean normalizeIdentifier) {
		this.normalizeIdentifier = normalizeIdentifier;
	}

	public boolean isSplitIdentifier() {
		return splitIdentifier;
	}

	/**
	 * 識別子を分割するかどうか
	 */
	public void setSplitIdentifier(boolean splitIdentifier) {
		this.splitIdentifier = splitIdentifier;
	}

	public boolean isIdentifyTokenType() {
		return identifyTokenType;
	}

	/**
	 * 識別子にトークンタイプを付与するかどうか
	 */
	public void setIdentifyTokenType(boolean identifyTokenType) {
		this.identifyTokenType = identifyTokenType;
	}

	public boolean isContextSensitiveSymbols() {
		return contextSensitiveSymbols;
	}

	public void setContextSensitiveSymbols(boolean contextSensitiveSymbols) {
		this.contextSensitiveSymbols = contextSensitiveSymbols;
	}

	@Override
	public Token getCurrentToken() {
		return current;
	}

	@Override
	public void nextToken() {
		current = tokenIterator.hasNext() ? tokenIterator.next() : null;
	}

	@Override
	public void consumedToken(Statement statement, Token token, ParserTokenType type) {

		if(documentBuilder == null) {
			documentBuilder = new Document.Builder();
		}

		if(isSplitIdentifier()) {
			splitToken(statement, token, type);
		} else {
			if(isNormalizeIdentifier()) {
				token = normalizeToken(statement, token, type);
			} else {
				token = new Token(generateTokenSurface(statement, token.getSurface(), type), token
					.getStartPosition(), token.getEndPosition(), token.getProperties());
			}

			super.consumedToken(statement, token, type);
			documentBuilder.add(token);
		}
	}

	private Token normalizeToken(Statement statement, Token token, ParserTokenType type) {
		//System.err.print(token + " " +  type);
		String surface;
		switch(type){
		case TYPE_IDENTIFIER:
			surface = "<TYPE>";
			break;
		case VARIABLE_IDENTIFIER:
			surface = "<VARIABLE>";
			break;
		case QUALIFY_IDENTIFIER:
			surface = "<PACKAGE>";
			break;
		case LITERAL:
			surface = "<LITERAL>";
			break;
		default:
			surface = token.getSurface();
			break;
		}
		//System.err.println("=>" + surface);
		return new Token(generateTokenSurface(statement, surface, type), token.getStartPosition(), token
			.getEndPosition(), token.getProperties());
	}

	private void splitToken(Statement statement, Token token, ParserTokenType type) {
		if(type == ParserTokenType.TYPE_IDENTIFIER || type == ParserTokenType.VARIABLE_IDENTIFIER || type == ParserTokenType.QUALIFY_IDENTIFIER) {
			int idx = token.getStartPosition();
			for(String split : iterable(IDENTIFIER_SPLIT_PATTERN.splitAsStream(token.getSurface())
				.map(String::toLowerCase))) {
				Token ntoken = new Token(generateTokenSurface(statement, split, type), idx, idx + split.length(), token
					.getProperties());
				super.consumedToken(statement, ntoken, type);
				documentBuilder.add(ntoken);
				idx += split.length();
			}
		} else {
			Token nToken = new Token(generateTokenSurface(statement, token.getSurface(), type), token
				.getStartPosition(), token.getEndPosition(), token.getProperties());
			super.consumedToken(statement, nToken, type);
			documentBuilder.add(nToken);
		}
	}

	/**
	 * 文書を区切るときに呼び出す
	 */
	protected void separateDocument() {
		if(documentBuilder != null) {
			documents.add(documentBuilder.build());
		}
		documentBuilder = null;
	}

	/**
	 * 現在の文書に属性を設定する
	 * @param key 属性のkey
	 * @param value 属性値
	 */
	protected void putDocumentProperty(String key, String value) {
		if(documentBuilder == null) {
			documentBuilder = new Document.Builder();
		}
		documentBuilder.putProperty(key, value);
	}

	public static <T> Iterable<T> iterable(Stream<T> stream) {
		return () -> stream.iterator();
	}

	private String generateTokenSurface(Statement statement, String surface, ParserTokenType type) {

		if(identifyTokenType) {
			surface = type.name() + "-" + surface;
		}

		if(contextSensitiveSymbols) {
			if(type == ParserTokenType.BRACKET || type == ParserTokenType.OPERATOR) {
				surface = "(" + super.getStatementType(statement) + ")" + surface;
			} else {
				surface = "()" + surface;
			}
		}

		return surface;
	}

}
