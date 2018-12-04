package jp.ac.osaka_u.ist.sdl.rminer.kenja;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Kenja {
	private static ObjectMapper mapper = new ObjectMapper();

	public Kenja() {
	}

	public static List<ExtractMethod> loadExtractMethod(String jsonPath) throws IOException {
		return mapper.readValue(new File(jsonPath), new TypeReference<List<ExtractMethod>>() {});
	}
}
