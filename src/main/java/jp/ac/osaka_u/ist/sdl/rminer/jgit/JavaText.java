package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.Sequence;

import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;
import jp.ac.osaka_u.ist.sdl.rminer.parser.java.JavaParser;

public class JavaText extends Sequence {

	//private char[] data;
	private List<Token> tokens;

	public JavaText(SourceCode code) throws IOException {
		JavaParser parser = new JavaParser();
		Map<String, String> options = new HashMap<>();
		options.put("unit", "lexer");
		options.put("javaVersion", "1.8");
		parser.initialize(options);

		this.tokens = parser.tokenize(code)
			.tokenStream()
			.collect(Collectors.toList());
		//this.data = code.loadCharData();
	}

	public String getToken(int idx) {
		return tokens.get(idx)
			.getSurface();
	}

	public List<Token> tokens(){
		return tokens;
	}
	
	@Override
	public int size() {
		return tokens.size();
	}
}
