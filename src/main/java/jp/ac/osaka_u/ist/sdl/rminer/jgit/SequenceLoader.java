package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import java.io.IOException;

public interface SequenceLoader<T> {
	T load(byte[] contents) throws IOException;
}
