package jp.ac.osaka_u.ist.sdl.rminer.parser;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 1つのトークン（単語）を表すクラス
 */
public class Token {
	private final String surface;
	private final int startPosition;
	private final int endPosition;
	private final Map<String, String> properties;

	/**
	 * トークン作成
	 * @param surface トークンの見た目。これをもとに同一トークンの判定が行われる
	 * @param startPosition もととなるソースコード中での開始位置
	 * @param endPosition もととなるソースコード中での終端位置
	 * @param properties このトークンに関する属性
	 */
	@JsonCreator
	public Token(@JsonProperty("surface") String surface, @JsonProperty("startPosition") int startPosition, @JsonProperty("endPosition") int endPosition, @JsonProperty("properties") Map<String, String> properties) {

		this.surface = surface;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.properties = new HashMap<>(properties);
	}

	public String getSurface() {
		return surface;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public int getEndPosition() {
		return endPosition;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endPosition;
		result = prime * result + startPosition;
		result = prime * result + ((surface == null) ? 0 : surface.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof Token)) {
			return false;
		}
		Token other = (Token)obj;
		if(endPosition != other.endPosition) {
			return false;
		}
		if(startPosition != other.startPosition) {
			return false;
		}
		if(surface == null) {
			if(other.surface != null) {
				return false;
			}
		} else if(!surface.equals(other.surface)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "(" + surface + ", (" + startPosition + ", " + endPosition + ")," + properties + ")";
	}
}
