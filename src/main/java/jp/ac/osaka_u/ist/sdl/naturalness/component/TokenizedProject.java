package jp.ac.osaka_u.ist.sdl.naturalness.component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jp.ac.osaka_u.ist.sdl.naturalness.NaturalnessException;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Document;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;
import jp.ac.osaka_u.ist.sdl.rminer.parser.TokenizedCode;

/**
 * トークン化されたファイル群
 */
public class TokenizedProject implements Iterable<TokenizedCode> {
	private List<TokenizedCode> docs;

	@JsonCreator
	public TokenizedProject(@JsonProperty("codes") List<TokenizedCode> docs) {
		this.docs = docs;
	}

	public List<TokenizedCode> getCodes() {
		return docs;
	}

	@Override
	public Iterator<TokenizedCode> iterator() {
		return docs.iterator();
	}

	public Stream<TokenizedCode> stream() {
		return docs.stream();
	}

	public Iterable<Token> tokens() {
		return () -> tokenStream().iterator();
	}

	public Stream<Token> tokenStream() {
		return stream()
			.flatMap(TokenizedCode::stream)
			.flatMap(Document::stream);
	}

	public Stream<Token> tokenReverseStream() {
		return stream()
			.flatMap(TokenizedCode::stream)
			.flatMap(Document::reverseStream);
	}

	/**
	 * 自然さを計測し、各トークンの属性に書き込む。自然さは各トークンのscoreというkeyで書き込まれる
	 * @param model 自然さ計測に用いる言語モデル
	 * @param dict 辞書
	 */
	public void query(LanguageModel model, Dictionary dict) {
		query(model, dict, "score");
	}

	public void query(LanguageModel model, Dictionary dict, String resultKey) {
		query(model, dict, "score", false);
	}

	/**
	 * 自然さを計測し、各トークンの属性に書き込む
	 * @param model 自然さ計測に用いる言語モデル
	 * @param dict 辞書
	 * @param resultKey 自然さを書き込む際の、トークンの属性のkey
	 */
	public void query(LanguageModel model, Dictionary dict, String resultKey, boolean isReverse) {
		this.stream()
			.flatMap(TokenizedCode::stream)
			.forEach(document -> {
				List<Long> id = getStream(document, isReverse)
					.map(token -> dict.getId(token.getSurface()))
					.collect(Collectors.toList());

				zipWith(document, model.estimate(id), (token, result) -> {
					token.putProperty(resultKey, String.valueOf(result));
					return token;
				});
			});
	}

	public IndexedDocumentCollection index(Path path, Dictionary dictionary) {
		return index(path, dictionary, false);
	}

	public IndexedDocumentCollection index(Path path, Dictionary dictionary, boolean isReverse) {
		try(BufferedWriter writer = Files.newBufferedWriter(path)) {
			this.stream()
				.flatMap(TokenizedCode::stream)
				.forEach(doc -> outputId(dictionary, writer, doc, isReverse));
		} catch(IOException e) {
			throw new NaturalnessException(e);
		}

		return new IndexedDocumentCollection(path);
	}

	private static void outputId(Dictionary dictionary, BufferedWriter writer, Document doc, boolean isReverse) {
		boolean first = true;
		try {
			for(Token token : (Iterable<Token>)(() -> getStream(doc, isReverse).iterator())) {
				if(!first) writer.write(" ");
				long id = dictionary.getId(token.getSurface());
				writer.write(Long.toString(id));
				first = false;
			}
			writer.newLine();
		} catch(IOException e) {
			throw new NaturalnessException(e);
		}
	}

	public static <T, U, R> List<R> zipWith(Iterable<T> a, Iterable<U> b, BiFunction<T, U, R> func) {
		Iterator<T> i1 = a.iterator();
		Iterator<U> i2 = b.iterator();
		List<R> ret = new ArrayList<>();
		while(true) {
			T v1 = i1.hasNext() ? i1.next() : null;
			U v2 = i2.hasNext() ? i2.next() : null;
			if(v1 == null && v2 == null) {
				break;
			}
			ret.add(func.apply(v1, v2));
		}
		return ret;
	}

	public IndexedDocumentCollection index(Dictionary dictionary) {
		return index(dictionary, false);
	}

	public IndexedDocumentCollection index(Dictionary dictionary, boolean isReverse) {
		try {
			return index(Files.createTempFile(null, null), dictionary, isReverse);
		} catch(IOException e) {
			throw new NaturalnessException(e);
		}
	}

	private static Stream<Token> getStream(Document doc, boolean isReverse) {
		if(isReverse) {
			return doc.reverseStream();
		} else {
			return doc.stream();
		}
	}
}
