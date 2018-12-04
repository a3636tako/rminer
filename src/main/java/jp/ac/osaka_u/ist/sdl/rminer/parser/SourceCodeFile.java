package jp.ac.osaka_u.ist.sdl.rminer.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceCodeFile implements SourceCode {
	private Path path;

	public SourceCodeFile(Path path) {
		this.path = path;
	}

	@Override
	public InputStream openStream() throws IOException {
		return Files.newInputStream(path);
	}

	@Override
	public String getFileName() {
		return path.toString();
	}

}
