package jp.ac.osaka_u.ist.sdl.naturalness.component.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;

import jp.ac.osaka_u.ist.sdl.naturalness.NaturalnessException;
import jp.ac.osaka_u.ist.sdl.naturalness.component.Dictionary;

public class HashMapDictionary implements Dictionary {
	public static final long DEFAULT_ID = 0L;
	public Map<String, Long> index;
	public List<String> invertedIndex;
	public List<Integer> count;
	public boolean generatingId;
	protected static ObjectMapper mapper = new ObjectMapper();

	public HashMapDictionary() {
		this.index = new HashMap<>();
		this.invertedIndex = new ArrayList<>();
		this.invertedIndex.add("<unk>");
		this.count = new ArrayList<>();
		this.count.add(Integer.MAX_VALUE);
	}

	@Override
	public long getId(String str) {
		long val = index.getOrDefault(str, DEFAULT_ID);
		if(val == DEFAULT_ID && generatingId) {
			val = invertedIndex.size();
			invertedIndex.add(str);
			count.add(0);
			index.put(str, val);
		}
		if(val != DEFAULT_ID && generatingId) {
			count.set((int)val, count.get((int)val) + 1);
		}

		return val;
	}

	@Override
	public String getSurface(long id) {
		return invertedIndex.get((int)id);
	}

	@Override
	public void load(Path path) {
		try {
			HashMapDictionary dict = mapper.readValue(path.toFile(), HashMapDictionary.class);
			this.index = dict.index;
			this.invertedIndex = dict.invertedIndex;
			this.count = dict.count;
		} catch(IOException e) {
			throw new NaturalnessException();
		}
	}

	@Override
	public void store(Path path) {
		try {
			mapper.writeValue(path.toFile(), this);
		} catch(IOException e) {
			throw new NaturalnessException(e);
		}
	}

	public HashMapDictionary compress(int minCount) {
		HashMapDictionary dictionary = new HashMapDictionary();
		dictionary.generatingId = true;
		for(int i = 1; i < count.size(); i++) {
			if(count.get(i) >= minCount) {
				String key = invertedIndex.get(i);
				long id = dictionary.getId(key);
				dictionary.count.set((int)id, count.get(i));
			}
		}

		dictionary.generatingId = generatingId;
		return dictionary;
	}

	/**
	 * 出現頻度が高い順にソートし、IDを振り直す
	 * @return
	 */
	public HashMapDictionary sort() {
		HashMapDictionary dictionary = new HashMapDictionary();
		dictionary.generatingId = true;

		//<unk>は除くので1から
		Streams.zip(IntStream.range(1, count.size())
			.boxed(), count.stream(), (idx, count) -> new int[]{idx, count})
			.sorted(Comparator.<int[]>comparingInt(v -> v[1])
				.reversed())
			.forEach(v -> {
				String key = invertedIndex.get(v[0]);
				long id = dictionary.getId(key);
				dictionary.count.set((int)id, v[1]);
			});

		dictionary.generatingId = generatingId;
		return dictionary;
	}

	@Override
	public void setGeneratingId(boolean generateId) {
		this.generatingId = generateId;
	}

	@Override
	public boolean isGeneratingId() {
		return generatingId;
	}

	@Override
	public int getSize() {
		return invertedIndex.size();
	}
}
