package jp.ac.osaka_u.ist.sdl.rminer.jgit.blame;

import java.io.IOException;

import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.lib.PersonIdent;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.Commit;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitRepository;

public class JGitBlameResult implements BlameResult {
	private org.eclipse.jgit.blame.BlameResult result;
	private GitRepository repository;

	public JGitBlameResult(GitRepository repository, org.eclipse.jgit.blame.BlameResult result) {
		this.repository = repository;
		this.result = result;
	}

	public int hashCode() {
		return result.hashCode();
	}

	public boolean equals(Object obj) {
		return result.equals(obj);
	}

	public String getResultPath() {
		return result.getResultPath();
	}

	public RawText getResultContents() {
		return result.getResultContents();
	}

	public void discardResultContents() {
		result.discardResultContents();
	}

	public boolean hasSourceData(int idx) {
		return result.hasSourceData(idx);
	}

	public boolean hasSourceData(int start, int end) {
		return result.hasSourceData(start, end);
	}

	public Commit getSourceCommit(int idx) {
		return repository.getCommit(result.getSourceCommit(idx));
	}

	public PersonIdent getSourceAuthor(int idx) {
		return result.getSourceAuthor(idx);
	}

	public PersonIdent getSourceCommitter(int idx) {
		return result.getSourceCommitter(idx);
	}

	public String getSourcePath(int idx) {
		return result.getSourcePath(idx);
	}

	public int getSourceLine(int idx) {
		return result.getSourceLine(idx);
	}

	public void computeAll() throws IOException {
		result.computeAll();
	}

	public int computeNext() throws IOException {
		return result.computeNext();
	}

	public int lastLength() {
		return result.lastLength();
	}

	public void computeRange(int start, int end) throws IOException {
		result.computeRange(start, end);
	}

	public String toString() {
		return result.toString();
	}

	@Override
	public int getResultSize() {
		return getResultContents().size();
	}

}
