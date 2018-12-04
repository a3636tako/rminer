package jp.ac.osaka_u.ist.sdl.rminer.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 1つの文書を表すクラス。
 * 文書とは、トークン列を構文的に適当な単位（ファイル、クラス、メソッドなど）で分割したもの
 */
public class Document implements Iterable<Token> {
	/**
	 * Document構築
	 */
	public static class Builder {
		private final ArrayList<Token> list;
		private final Map<String, String> properties;

		public Builder() {
			list = new ArrayList<>();
			properties = new HashMap<>();
		}

		public void add(Token token) {
			this.list.add(token);
		}

		public void addAll(Collection<Token> tokens) {
			list.addAll(tokens);
		}

		public void putProperty(String key, String value) {
			this.properties.put(key, value);
		}

		public Document build() {
			return new Document(list, properties);
		}
	}

	private final List<Token> tokens;
	private final Map<String, String> properties;

	/**
	 * 文書を作る
	 * @param tokens 属するトークン列
	 * @param properties この文書に関する属性
	 */
	@JsonCreator
	public Document(@JsonProperty("tokens") List<Token> tokens, @JsonProperty("properties") Map<String, String> properties) {
		this.tokens = Collections.unmodifiableList(tokens);
		this.properties = properties;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	@Override
	public Iterator<Token> iterator() {
		return tokens.iterator();
	}

	public Stream<Token> stream() {
		return tokens.stream();
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public String getProperty(String key) {
		return properties.get(key);
	}

	public void putProperty(String key, String value) {
		this.properties.put(key, value);
	}

	@JsonIgnore
	public int getSize() {
		return tokens.size();
	}

	@Override
	public String toString() {
		return "Document [tokens=" + tokens + ", properties=" + properties + "]";
	}
}
