package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitFile.GitFileBuilder;

/**
 * Gitリポジトリにおける1つのコミット
 */
public class Commit {
	private ObjectId id;
	private GitRepository repository;

	Commit(GitRepository repository, ObjectId id) {
		this.repository = repository;
		this.id = id;
	}

	/**
	 * コミットIDを取得する
	 *
	 * @return コミットID
	 */
	public String getIdString() {
		return id.getName();
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Commit)) {
			return false;
		}
		return id.equals(((Commit)o).getId());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * コミットIDを取得する
	 *
	 * @return コミットID
	 */
	public ObjectId getId() {
		return id;
	}

	/**
	 * このコミットが含まれるリポジトリを取得
	 *
	 * @return GitRepositoryインスタンス
	 */
	public GitRepository getRepository() {
		return repository;
	}

	/**
	 * このコミットの親コミットを取得する
	 *
	 * @return 親コミット。存在しない場合null
	 */
	public Commit getParent() {
		try(RevWalk walk = new RevWalk(repository.getRepository())) {
			RevCommit cb = walk.parseCommit(this.getId());
			if(cb.getParentCount() >= 1) {
				RevCommit parent = cb.getParent(0);
				if(parent != null) return new Commit(repository, parent.toObjectId());
			}
			return null;

		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * このコミットのマージ側の親コミットを取得する
	 *
	 * @return 親コミット。存在しない場合null
	 */
	public Commit getMergeParent() {
		try(RevWalk walk = new RevWalk(repository.getRepository())) {
			RevCommit cb = walk.parseCommit(this.getId());

			if(cb.getParentCount() >= 2) {
				RevCommit parent = cb.getParent(1);
				if(parent != null) return new Commit(repository, parent.toObjectId());
			}
			return null;

		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * このコミット時点でのファイルを取得する
	 *
	 * @return ファイルのリスト
	 * @throws IOException
	 */
	public List<GitFile> retrieveFiles() throws IOException {
		return retrieveFiles(GitFileBuilder.DEFAULT_FILE_BUILDER);
	}

	/**
	 * このコミット時点でのファイルを取得する
	 *
	 * @param build
	 *            独自のファイルビルダ
	 * @return ファイルリスト
	 * @throws IOException
	 */
	public <T extends GitFile> List<T> retrieveFiles(GitFileBuilder<T> build) throws IOException {
		ArrayList<T> files = new ArrayList<>();
		synchronized(repository.getRepository()) {
			try(RevWalk rw = new RevWalk(repository.getRepository());
				TreeWalk tw = new TreeWalk(repository.getRepository())) {
				RevTree t = rw.parseCommit(id)
					.getTree();
				tw.addTree(t);
				tw.setRecursive(true);
				tw.setFilter(build.getFilter());
				while(tw.next()) {
					files.add(build.build(this, tw));
				}
			}
		}
		return files;
	}

	/**
	 * 他のコミットとの間で変更のあったファイルを取得する
	 *
	 * @param other
	 *            比較対象
	 * @return ファイルのリスト
	 */
	public List<ChangedFile> diff(Commit other) {
		if(other == null) {
			try {
				return this.retrieveFiles()
					.stream()
					.map(v -> new ChangedFile(v, null))
					.collect(Collectors.toList());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}

		List<DiffEntry> entries;

		try(DiffFormatter df = new DiffFormatter(System.err)) {
			df.setRepository(repository.getRepository());
			entries = df.scan(other.getId(), this.getId());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		return entries.stream()
			.map(v -> new ChangedFile(buildFile(this, v.getNewPath(), v.getNewId()), buildFile(other, v.getOldPath(), v
				.getOldId())))
			.collect(Collectors.toList());
	}

	public String getCommitMessage() {
		try(RevWalk walk = new RevWalk(repository.getRepository())) {
			RevCommit cb = walk.parseCommit(this.getId());
			return cb.getFullMessage();

		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private GitFile buildFile(Commit commit, String path, AbbreviatedObjectId id) {
		if(!path.equals("/dev/null")) {
			return new GitFile(commit, path, id.toObjectId());
		} else {
			return null;
		}
	}

	public RevCommit getRevCommit() {
		try(RevWalk walk = new RevWalk(repository.getRepository())) {
			return walk.parseCommit(this.getId());

		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

}
