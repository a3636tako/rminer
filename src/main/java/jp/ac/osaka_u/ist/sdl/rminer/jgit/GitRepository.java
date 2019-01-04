package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * Gitリポジトリを表すクラス
 */
public class GitRepository {
	private Path path;
	private Repository repository;
	private ConcurrentHashMap<Long, ObjectReader> readerMap;

	/**
	 * リポジトリを開く
	 * @param repo リポジトリへのパス
	 * @throws IOException
	 */
	public GitRepository(Path path) throws IOException {
		this.path = path;
		this.repository = new FileRepositoryBuilder().setGitDir(new File(this.path + "/.git"))
			.build();
		this.readerMap = new ConcurrentHashMap<>();
	}

	/**
	 * Constructor for test.
	 */
	GitRepository(Path path, Repository repository, ObjectReader reader) {
		this.path = path;
		this.repository = repository;
		this.readerMap = new ConcurrentHashMap<>();
		this.readerMap.put(Thread.currentThread().getId(), reader);
	}

	/**
	 * リポジトリを開く
	 * @param repo リポジトリへのパス
	 * @throws IOException
	 */
	public GitRepository(String repo) throws IOException {
		this(Paths.get(repo));
	}

	public Path getPath() {
		return path;
	}

	/**
	 * JGitインスタンスの取得
	 * @return JGitのRepositoryインスタンス
	 */
	public Repository getRepository() {
		return repository;
	}

	public Commit searchMergeBase(Commit c1, Commit c2) {
		Commit mergeBaseCommit;
		try(RevWalk walk = new RevWalk(repository)) {
			RevCommit rev1 = walk.parseCommit(c1.getId());
			RevCommit rev2 = walk.parseCommit(c2.getId());

			walk.setRevFilter(RevFilter.MERGE_BASE);
			walk.markStart(rev1);
			walk.markStart(rev2);

			RevCommit result = walk.next();
			if(result == null) {
				return null;
			}
			mergeBaseCommit = new Commit(this, result.getId());

		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}

		return mergeBaseCommit;
	}

	/**
	 * コミットIDやタグ、ブランチ名からコミットを取得
	 * @param id コミットIDやタグ、ブランチ名
	 * @return コミット。見つからなかった場合null
	 */
	public Commit getCommit(String id) {
		ObjectId objectId = null;

		try {
			objectId = repository.resolve(id);
		} catch(IOException e) {
			e.printStackTrace();
		}

		if(objectId == null) {
			return null;
		} else {
			return new Commit(this, objectId);
		}
	}

	public Commit getCommit(RevCommit commit) {
		return new Commit(this, commit.getId());
	}

	public List<Commit> getAllCommits() {
		List<Commit> result = new ArrayList<>();

		try(RevWalk revWalk = new RevWalk(repository)) {
			List<Ref> allRefs = repository.getRefDatabase()
				.getRefsByPrefix(RefDatabase.ALL);

			for(Ref ref : allRefs) {
				revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
			}
			for(RevCommit commit : revWalk) {
				result.add(new Commit(this, commit.getId()));
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public ObjectReader getObjectReader() {
		return readerMap.computeIfAbsent(Thread.currentThread()
			.getId(), id -> this.repository.newObjectReader());
	}

}
