package jp.ac.osaka_u.ist.sdl.rminer.jgit.blame;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.Commit;

public interface BlameResult {

	int getResultSize();

	Commit getSourceCommit(int i);

	String getSourcePath(int i);

	int getSourceLine(int i);

}
