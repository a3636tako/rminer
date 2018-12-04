package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.Sequence;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.blame.BlameCommand;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.blame.BlameResult;

public class StableCommits {
	private Commit baseCommit;
	private ComparatorAlgorithms<?> algorithms;
	private List<Commit> stableCommits;
	private BlameCommand.ExecutorType type;

	public StableCommits(Commit baseCommit) {
		this(baseCommit, BlameCommand.ExecutorType.JGIT, ComparatorAlgorithms.DEFAULT);
	}

	public StableCommits(Commit baseCommit, BlameCommand.ExecutorType type, ComparatorAlgorithms<?> algorithms) {
		this.baseCommit = baseCommit;
		this.type = type;
		this.algorithms = algorithms;
		try {
			this.stableCommits = analyze();
		} catch(IOException | GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Commit> getStableCommits() {
		return stableCommits;
	}

	private List<Commit> analyze() throws IOException, GitAPIException {
		GitRepository repository = baseCommit.getRepository();
		Map<Commit, Bucket> bucketMap = new HashMap<>();
		for(GitFile file : baseCommit.retrieveFiles()) {
			BlameResult blameResult = runBlame(algorithms, repository, file);
			if(blameResult != null) {
				toCommitMap(repository, blameResult, bucketMap);
			} else {
				System.err.println("blameResult equals null. file:" + file == null ? "null" : file.getPath());
			}
		}

		return bucketMap.values()
			.stream()
			.filter(this::isStableCommit)
			.map(Bucket::getCommit)
			.collect(Collectors.toList());
	}

	private <T extends Sequence> BlameResult runBlame(ComparatorAlgorithms<T> algorithms, GitRepository repository, GitFile file) throws GitAPIException, IOException {
		BlameCommand<T> command = new BlameCommand<T>(repository).setExecutorType(type)
			.setFilePath(file.getPath())
			.setStartCommit(baseCommit.getId())
			.setFollowFileRenames(true)
			.setDiffAlgorithm(algorithms.getDiffAlgorithm())
			.setAlgorithms(algorithms);

		if(algorithms.getComparator() instanceof RawTextComparator) {
			command.setTextComparator((RawTextComparator)algorithms.getComparator());
		}

		return command.call();
	}

	private void toCommitMap(GitRepository repository, BlameResult blame, Map<Commit, Bucket> result) {
		int length = blame.getResultSize();
		for(int i = 0; i < length; i++) {
			Commit commit = blame.getSourceCommit(i);
			Bucket bucket = result.getOrDefault(commit, new Bucket(commit));
			bucket.addRange(blame.getSourcePath(i), blame.getSourceLine(i));
			result.put(commit, bucket);
		}
	}

	/**
	 * あるコミットにおける変更内容が baseCommitの時点ですべて残っているかを確認する
	 */
	private boolean isStableCommit(Bucket bucket) {
		List<ChangedFile> diffs = bucket.commit.diff(bucket.commit.getParent());
		for(ChangedFile diff : diffs) {
			// ファイルの削除の場合false
			GitFile path = diff.getBaseFile();
			if(path == null) {
				return false;
			}

			// ファイルが残っていない場合false
			RangedSet<Integer> rangedSet = bucket.map.get(path.getPath());
			if(rangedSet == null) {
				return false;
			}

			// Diffの比較に失敗したらfalse
			EditList list;
			try {
				list = diff.diff(algorithms);
			} catch(IOException e) {
				e.printStackTrace();
				return false;
			}

			// すべて残っていなければfalse
			RangedSet<Integer> edits = RangedSet.create();
			list.forEach(e -> edits.addRange(e.getBeginB(), e.getEndB()));
			execludeBlankLines(edits, diff.getBaseFile());
			RangedSet<Integer> intersect = edits.intersect(rangedSet);
			if(edits.size() != intersect.size()) {
				return false;
			}

		}

		return true;
	}

	private void execludeBlankLines(RangedSet<Integer> rangedSet, GitFile file) {
		if(!(algorithms.getComparator() instanceof RawTextComparator)) {
			return;
		}
		try {
			RawText rawText = new RawText(file.loadByteData());
			IntStream.range(0, rawText.size())
				.filter(idx -> {
					String text = rawText.getString(idx);
					boolean res = text.chars()
						.allMatch(Character::isWhitespace);
					return res;
				})
				.forEach(idx -> {
					rangedSet.removeRange(idx, idx + 1);
				});
		} catch(IOException e) {

		}
	}

	private static class Bucket {
		Commit commit;
		Map<String, RangedSet<Integer>> map;

		public Bucket(Commit commit) {
			this.commit = commit;
			map = new HashMap<>();
		}

		public void addRange(String filePath, int idx) {
			get(filePath).add(idx);
		}

		private RangedSet<Integer> get(String key) {
			RangedSet<Integer> set = map.get(key);
			if(set == null) {
				set = RangedSet.create();
				map.put(key, set);
			}
			return set;
		}

		public Commit getCommit() {
			return commit;
		}
	}
}
