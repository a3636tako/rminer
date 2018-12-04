package jp.ac.osaka_u.ist.sdl.rminer.parser.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.ITerminalSymbols;

/**
 * 字句解析によるトークンの種類
 */
@SuppressWarnings("deprecation")
public enum LexerTokenType {
	KEYWORD, BRACKET, LITERAL, OPERATOR, SPACE, COMMENT, IDENTIFIER,

	LINE_FEED, UNKNOWN,
	;

	public static final Map<Integer, LexerTokenType> typeMap;
	static {
		HashMap<Integer, LexerTokenType> cov = new HashMap<>();

		cov.put(ITerminalSymbols.TokenNameWHITESPACE, SPACE);
		cov.put(ITerminalSymbols.TokenNameCOMMENT_LINE, COMMENT);
		cov.put(ITerminalSymbols.TokenNameCOMMENT_BLOCK, COMMENT);
		cov.put(ITerminalSymbols.TokenNameCOMMENT_JAVADOC, COMMENT);

		cov.put(ITerminalSymbols.TokenNameIdentifier, IDENTIFIER);
		cov.put(ITerminalSymbols.TokenNameabstract, KEYWORD);

		/**
		 * "assert" token (added in J2SE 1.4).
		 */
		cov.put(ITerminalSymbols.TokenNameassert, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameboolean, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamebreak, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamebyte, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamecase, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamecatch, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamechar, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameclass, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamecontinue, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamedefault, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamedo, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamedouble, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameelse, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameextends, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamefalse, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamefinal, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamefinally, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamefloat, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamefor, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameif, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameimplements, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameimport, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameinstanceof, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameint, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameinterface, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamelong, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamenative, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamenew, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamenull, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamepackage, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameprivate, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameprotected, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamepublic, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamereturn, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameshort, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamestatic, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamestrictfp, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamesuper, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameswitch, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamesynchronized, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamethis, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamethrow, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamethrows, KEYWORD);
		cov.put(ITerminalSymbols.TokenNametransient, KEYWORD);
		cov.put(ITerminalSymbols.TokenNametrue, KEYWORD);
		cov.put(ITerminalSymbols.TokenNametry, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamevoid, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamevolatile, KEYWORD);
		cov.put(ITerminalSymbols.TokenNamewhile, KEYWORD);
		cov.put(ITerminalSymbols.TokenNameIntegerLiteral, LITERAL);
		cov.put(ITerminalSymbols.TokenNameLongLiteral, LITERAL);
		cov.put(ITerminalSymbols.TokenNameFloatingPointLiteral, LITERAL);
		cov.put(ITerminalSymbols.TokenNameDoubleLiteral, LITERAL);
		cov.put(ITerminalSymbols.TokenNameCharacterLiteral, LITERAL);
		cov.put(ITerminalSymbols.TokenNameStringLiteral, LITERAL);
		cov.put(ITerminalSymbols.TokenNamePLUS_PLUS, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameMINUS_MINUS, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameEQUAL_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameLESS_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameGREATER_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameNOT_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameLEFT_SHIFT, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameRIGHT_SHIFT, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT, OPERATOR);
		cov.put(ITerminalSymbols.TokenNamePLUS_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameMINUS_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameMULTIPLY_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameDIVIDE_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameAND_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameOR_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameXOR_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameREMAINDER_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameLEFT_SHIFT_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameRIGHT_SHIFT_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameOR_OR, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameAND_AND, OPERATOR);
		cov.put(ITerminalSymbols.TokenNamePLUS, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameMINUS, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameNOT, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameREMAINDER, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameXOR, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameAND, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameMULTIPLY, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameOR, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameTWIDDLE, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameDIVIDE, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameGREATER, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameLESS, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameLPAREN, BRACKET);
		cov.put(ITerminalSymbols.TokenNameRPAREN, BRACKET);
		cov.put(ITerminalSymbols.TokenNameLBRACE, BRACKET);
		cov.put(ITerminalSymbols.TokenNameRBRACE, BRACKET);
		cov.put(ITerminalSymbols.TokenNameLBRACKET, BRACKET);
		cov.put(ITerminalSymbols.TokenNameRBRACKET, BRACKET);
		cov.put(ITerminalSymbols.TokenNameSEMICOLON, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameQUESTION, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameCOLON, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameCOMMA, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameDOT, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameEQUAL, OPERATOR);
		cov.put(ITerminalSymbols.TokenNameEOF, UNKNOWN);
		cov.put(ITerminalSymbols.TokenNameERROR, UNKNOWN);

		/**
		 * "enum" keyword (added in J2SE 1.5).
		 * @since 3.0
		 */
		cov.put(ITerminalSymbols.TokenNameenum, KEYWORD);

		/**
		 * "@" token (added in J2SE 1.5).
		 * @since 3.0
		 */
		cov.put(ITerminalSymbols.TokenNameAT, OPERATOR);

		/**
		 * "..." token (added in J2SE 1.5).
		 * @since 3.0
		 */
		cov.put(ITerminalSymbols.TokenNameELLIPSIS, OPERATOR);

		/**
		 * @since 3.1
		 */
		cov.put(ITerminalSymbols.TokenNameconst, KEYWORD);

		/**
		 * @since 3.1
		 */
		cov.put(ITerminalSymbols.TokenNamegoto, KEYWORD); // goto not found in Java ? :)

		/**
		 * @since 3.10
		 */
		cov.put(ITerminalSymbols.TokenNameARROW, OPERATOR);
		/**
		 * @since 3.10
		 */
		cov.put(ITerminalSymbols.TokenNameCOLON_COLON, OPERATOR);

		typeMap = Collections.unmodifiableMap(cov);
	}

	/**
	 * JDTのコードからenumを生成
	 * @param iterminalSymbolsCode JDTのITerminalSymbolsのコード
	 * @return 対応するLexerTokenType
	 */
	public static LexerTokenType toType(int iterminalSymbolsCode) {
		return typeMap.getOrDefault(iterminalSymbolsCode, LexerTokenType.UNKNOWN);
	}
}
