package jp.ac.osaka_u.ist.sdl.naturalness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jp.ac.osaka_u.ist.sdl.rminer.parser.Document;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;
import jp.ac.osaka_u.ist.sdl.rminer.parser.TokenizedCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.java.JavaParser;
import jp.ac.osaka_u.ist.sdl.rminer.parser.java.LexerTokenType;

public class LineTokenConverter {

	private final String key;
	private final String probabilityKey;
	private final String entropyKey;
	private final double threshold;

	public LineTokenConverter(String key, String probabilityKey, String entropyKey, double threshold) {
		this.key = key;
		this.probabilityKey = probabilityKey;
		this.entropyKey = entropyKey;
		this.threshold = threshold;
	}

	public TokenizedCode convert(TokenizedCode original) {
		List<Document> docs = original.stream()
			.map(this::convertDocument)
			.collect(Collectors.toList());

		return new TokenizedCode(docs, original.getProperties());
	}

	private Document convertDocument(Document doc) {

		List<Token> result = conertLineToken(doc, t -> t.getProperty(JavaParser.LEXER_TOKEN_TYPE_KEY)
			.equals(LexerTokenType.LINE_FEED.name()), list -> {
				Token first = list.get(0);
				Token last = list.get(list.size() - 1);
				double sum = list.stream()
					.mapToDouble(t -> Double.parseDouble(t.getProperty(key)))
					.sum();
				int start = first.getStartPosition();
				int end = last.getEndPosition();
				Map<String, String> prop = new HashMap<>();
				prop.putAll(first.getProperties());
				prop.put(probabilityKey, String.valueOf(convertScore(sum)));
				prop.put(entropyKey, String.valueOf(sum / list.size()));
				return new Token("", start, end, prop);
			});

		Document.Builder builder = new Document.Builder();
		builder.addAll(result);
		return builder.build();
	}

	public static <T, X> List<X> conertLineToken(Iterable<T> document, Predicate<T> isLinefeed, Function<List<T>, X> converter) {
		List<X> result = new ArrayList<>();
		List<T> buffer = new ArrayList<>();
		for(T token : document) {
			buffer.add(token);
			if(isLinefeed.test(token)) {
				result.add(converter.apply(buffer));
				buffer.clear();
			}
		}

		if(!buffer.isEmpty()) {
			result.add(converter.apply(buffer));
		}

		return result;
	}

	private double convertScore(double v) {
		return Math.max(threshold - v, 0);
	}
}
