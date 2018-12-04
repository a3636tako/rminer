package jp.ac.osaka_u.ist.sdl.rminer.parser;

import java.io.IOException;
import java.io.InputStream;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitFile;

public class SourceCodeGitFile implements SourceCode {

	private GitFile file;

	public SourceCodeGitFile(GitFile file) {
		this.file = file;
	}

	@Override
	public InputStream openStream() throws IOException {
		return file.openStream();
	}

	@Override
	public String getFileName() {
		return file.getPath();
	}

}
