package jp.ac.osaka_u.ist.sdl.rminer.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FormatDocument {
	private SourceCode origin;
	private TokenizedCode code;
	private List<TokenDecorator> decorators;

	public FormatDocument(SourceCode origin, TokenizedCode code) {
		this.origin = origin;
		this.code = code;
		this.decorators = new ArrayList<>();
	}

	public void addDecorator(TokenDecorator decorator) {
		this.decorators.add(decorator);
	}

	public void removeDecorator(TokenDecorator decorator) {
		this.decorators.remove(decorator);
	}

	public void write(Path path) throws IOException {
		try(Writer writer = Files.newBufferedWriter(path)) {
			write(writer);
		}
	}

	public void write(Writer writer) throws IOException {
		Stream<Token> tokenStream = code.stream()
			.flatMap(Document::stream)
			.sorted((a, b) -> Integer.compare(a.getStartPosition(), b.getStartPosition()));

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(origin.openStream()))) {

			int idx = 0;
			for(Token token : toIterable(tokenStream)) {
				write(reader, writer, token.getStartPosition() - idx);

				String originalToken = read(reader, token.getEndPosition() - token.getStartPosition());
				String decoratingToken = originalToken;
				for(TokenDecorator decorator : decorators) {
					if(decorator.match(token)) {
						decoratingToken = decorator.decorate(token, decoratingToken);
					}
				}

				writer.write(decoratingToken);

				idx = token.getEndPosition();
			}
			write(reader, writer, Integer.MAX_VALUE);
		}
	}

	private String read(Reader reader, int len) throws IOException {
		char[] buf = new char[len];
		int cnt = 0;
		while(cnt < len) {
			int size = reader.read(buf, cnt, len - cnt);

			if(size < 0) break;

			cnt += size;
		}
		return new String(buf, 0, cnt);
	}

	private void write(Reader reader, Writer writer, int len) throws IOException {
		char[] buf = new char[1024];
		int cnt = 0;
		while(cnt < len) {
			int size = reader.read(buf, 0, Math.min(buf.length, len - cnt));

			if(size < 0) return;

			writer.write(buf, 0, size);
			cnt += size;
		}
	}

	public static <T> Iterable<T> toIterable(Stream<T> stream) {
		return () -> stream.iterator();
	}
}
