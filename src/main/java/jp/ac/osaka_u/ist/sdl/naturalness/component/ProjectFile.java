package jp.ac.osaka_u.ist.sdl.naturalness.component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jp.ac.osaka_u.ist.sdl.naturalness.NaturalnessException;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitFile;
import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCode;

/**
 * 解析対象の1つのファイルを表す
 */
public class ProjectFile implements SourceCode {
	private Path path;
	private Path root;
	private GitFile file;

	public ProjectFile(Path path) {
		this(Paths.get("."), path);
	}

	public ProjectFile(Path root, Path path) {
		this.root = root;
		this.path = path;
	}

	public ProjectFile(GitFile file) {
		this.file = file;
	}

	@Override
	public InputStream openStream() {
		try {
			if(file != null) {
				return file.openStream();
			} else {
				return Files.newInputStream(root.resolve(path));
			}
		} catch(IOException e) {
			throw new NaturalnessException(e);
		}
	}

	@Override
	public String getFileName() {
		if(file != null)
			return file.getPath();
		return path.toString();
	}
}
