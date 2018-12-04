package jp.ac.osaka_u.ist.sdl.rminer.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public interface SourceCode {
	InputStream openStream() throws IOException;

	String getFileName();

	default char[] loadCharData() throws IOException {
		StringBuilder build = new StringBuilder();

		try(BufferedReader buf = new BufferedReader(new InputStreamReader(openStream()))) {
			int c;
			while((c = buf.read()) != -1)
				build.append((char)c);
		}

		char[] ret = new char[build.length()];
		build.getChars(0, build.length(), ret, 0);
		return ret;
	}
}
