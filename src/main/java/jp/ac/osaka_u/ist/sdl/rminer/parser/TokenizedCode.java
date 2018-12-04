package jp.ac.osaka_u.ist.sdl.rminer.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * トークン化されたソースコードを表すクラス
 */
public class TokenizedCode implements Iterable<Document> {
	private List<Document> documents;
	private Map<String, String> properties;

	public TokenizedCode(List<Document> documents) {
		this.documents = documents;
		this.properties = new HashMap<>();
	}

	@JsonCreator
	public TokenizedCode(@JsonProperty("documents") List<Document> documents, @JsonProperty("properties") Map<String, String> property) {
		this.documents = documents;
		this.properties = new HashMap<>(property);
	}

	@Override
	public Iterator<Document> iterator() {
		return documents.iterator();
	}

	public Stream<Document> stream() {
		return documents.stream();
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

	public List<Document> getDocuments() {
		return documents;
	}

	public Iterable<Token> tokens() {
		return () -> tokenStream().iterator();
	}

	public Stream<Token> tokenStream() {
		return documents.stream()
			.flatMap(Document::stream);
	}

	@Override
	public String toString() {
		return "TokenizedCode [documents=" + documents + ", properties=" + properties + "]";
	}

}
