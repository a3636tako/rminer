package jp.ac.osaka_u.ist.sdl.rminer.parser.java;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.util.PublicScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.ac.osaka_u.ist.sdl.rminer.parser.Document;
import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;
import jp.ac.osaka_u.ist.sdl.rminer.parser.TokenizedCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Tokenizer;

/**
 * Javaソースコードをトークン化
 */
public class JavaParser implements Tokenizer {
	private static Logger log = LoggerFactory.getLogger(JavaParser.class);

	public final static String LEXER_TOKEN_TYPE_KEY = "lexerTokenType";
	public final static String LINE_NUMBER_START = "lineNumberStart";
	public final static String LINE_NUMBER_END = "lineNumberEnd";
	public final static String FILE_NAME = "fileName";
	public final static String LINEFEED_TOKEN = "<lf>";

	private String unit;
	private String javaVersion;
	private boolean splitIdentifier;
	private boolean normalizeIdentifier;
	private boolean tokenizeLinefeed;
	private boolean identifyTokenType;
	private boolean contextSensitiveSymbols;

	/**
	 * 2つのパラメータを受け取る
	 *
	 * unit : 文書の単位(lexer, file, class, method) splitIdentifier: 識別子分割 (true,
	 * flase) normalizeIdentifier: 識別子正規化 (true, false)
	 */
	@Override
	public void initialize(Map<String, String> parameters) {
		this.unit = parameters.get("unit");
		if(this.unit == null) {
			this.unit = parameters.getOrDefault("documentUnit", "method");

		}
		this.splitIdentifier = Boolean.parseBoolean(parameters.getOrDefault("splitIdentifier", "false"));
		this.normalizeIdentifier = Boolean.parseBoolean(parameters.getOrDefault("normalizeIdentifier", "false"));
		this.tokenizeLinefeed = Boolean.parseBoolean(parameters.getOrDefault("tokenizeLinefeed", "false"));
		this.identifyTokenType = Boolean.parseBoolean(parameters.getOrDefault("identifyTokenType", "false"));
		this.contextSensitiveSymbols = Boolean
			.parseBoolean(parameters.getOrDefault("contextSensitiveSymbols", "false"));
		this.javaVersion = parameters.getOrDefault("javaVersion", "");
		
		log.info("unit:" + this.unit);
		log.info("javaVersion:" + this.javaVersion);
		log.info("splitIdentifier:" + this.splitIdentifier);
		log.info("normalizeIdentifier:" + this.normalizeIdentifier);
		log.info("tokenizeLinefeed:" + this.tokenizeLinefeed);
		log.info("identifyTokenType:" + this.identifyTokenType);
		log.info("contextSensitiveSymbols:" + this.contextSensitiveSymbols);
	}

	@Override
	public boolean isTokenizable(SourceCode sourceCode) {
		return sourceCode.getFileName()
			.endsWith(".java");
	}

	@Override
	public TokenizedCode tokenize(SourceCode sourceCode) {

		try {
			// まずは字句解析
			TokenizedCode code = lex(sourceCode);

			AbstractDocumentGenerator generator;
			switch(unit){
			case "lexer":
				return code;
			case "method":
				generator = new MethodDocumentGenerator();
				break;
			case "class":
				generator = new ClassDocumentGenerator();
				break;
			case "file":
				generator = new FileDocumentGenerator();
				break;
			default:
				System.err.println("Unrecognized unit name:" + unit);
				throw new RuntimeException();
			}

			if(this.normalizeIdentifier) {
				generator.setNormalizeIdentifier(true);
			} else if(this.splitIdentifier) {
				generator.setSplitIdentifier(true);
			}

			generator.setIdentifyTokenType(this.identifyTokenType);
			generator.setContextSensitiveSymbols(contextSensitiveSymbols);

			return generator.parse(sourceCode, code);

		} catch(Exception e) {
			System.err.println("Can't parse" + sourceCode.getFileName());
			System.err.println(e.getMessage());
			e.printStackTrace();
			return new TokenizedCode(Collections.emptyList());
		}
	}

	/**
	 * 字句解析
	 *
	 * @throws InvalidInputException
	 */
	private TokenizedCode lex(SourceCode sourceCode) throws InvalidInputException {
		IScanner sc = createScanner(false, false, false, true);

		try {
			sc.setSource(sourceCode.loadCharData());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		Document.Builder builder = new Document.Builder();

		int prevend = 1;
		while(true) {
			int tokenType;
			tokenType = sc.getNextToken();
			if(tokenType == ITerminalSymbols.TokenNameEOF) break;

			String surface = new String(sc.getCurrentTokenSource());
			int startPosition = sc.getCurrentTokenStartPosition();
			int endPosition = sc.getCurrentTokenEndPosition() + 1;

			int startLine = sc.getLineNumber(sc.getCurrentTokenStartPosition());
			int endLine = sc.getLineNumber(sc.getCurrentTokenEndPosition());

			if(tokenizeLinefeed && startLine != prevend) {
				Map<String, String> prop = new HashMap<>();
				prop.put(LEXER_TOKEN_TYPE_KEY, LexerTokenType.LINE_FEED.name());
				prop.put(LINE_NUMBER_START, String.valueOf(startLine));
				prop.put(LINE_NUMBER_END, String.valueOf(startLine));

				Token token = new Token(LINEFEED_TOKEN, startPosition, startPosition, prop);
				builder.add(token);

			}
			prevend = endLine;

			Map<String, String> prop = new HashMap<>();
			prop.put(LEXER_TOKEN_TYPE_KEY, LexerTokenType.toType(tokenType)
				.name());
			prop.put(LINE_NUMBER_START, String.valueOf(startLine));
			prop.put(LINE_NUMBER_END, String.valueOf(endLine));

			Token token = new Token(surface, startPosition, endPosition, prop);
			builder.add(token);
		}

		Document doc = builder.build();

		TokenizedCode tokenizedCode = new TokenizedCode(Collections.singletonList(doc));
		tokenizedCode.putProperty(FILE_NAME, sourceCode.getFileName());

		return tokenizedCode;
	}

	private IScanner createScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace, boolean assertMode, boolean recordLineSeparator) {
		// use default workspace compliance
		long complianceLevelValue = CompilerOptions.versionToJdkLevel(javaVersion);
		if(complianceLevelValue == 0) complianceLevelValue = ClassFileConstants.JDK1_4; // fault-tolerance
		//System.err.println("Java version set to " + CompilerOptions.versionFromJdkLevel(complianceLevelValue) + "(" + complianceLevelValue + ")");

		PublicScanner scanner = new PublicScanner(tokenizeComments, tokenizeWhiteSpace, false/*nls*/, complianceLevelValue, complianceLevelValue, null/*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/);
		scanner.recordLineSeparator = recordLineSeparator;
		scanner.returnOnlyGreater = true;
		return scanner;
	}
}
