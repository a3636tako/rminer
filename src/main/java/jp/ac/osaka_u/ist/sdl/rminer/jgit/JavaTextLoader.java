package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCode;

public class JavaTextLoader implements SequenceLoader<JavaText> {
	private static class JavaSourceCode implements SourceCode{
		private byte[] contents;
		public JavaSourceCode(byte[] contents) {
			this.contents = contents;
		}
		@Override
		public InputStream openStream() throws IOException {
			return new ByteArrayInputStream(contents);
		}

		@Override
		public String getFileName() {
			return "Java.java";
		}
		
	}
	@Override
	public JavaText load(byte[] contents) throws IOException {
		return new JavaText(new JavaSourceCode(contents));
	}

}
