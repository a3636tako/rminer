package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import java.io.IOException;

import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.Sequence;

/**
 * 変更されたファイルのペア
 */
public class ChangedFile {
	private GitFile baseFile;
	private GitFile compareFile;

	public ChangedFile(GitFile baseFile, GitFile compareFile) {
		this.baseFile = baseFile;
		this.compareFile = compareFile;
	}

	/**
	 * 基準となるコミット時点でのファイルを取得
	 * @return ファイル
	 */
	public GitFile getBaseFile() {
		return baseFile;
	}

	/**
	 * 比較対象のコミット時点でのファイルを取得
	 * @return ファイル
	 */
	public GitFile getCompareFile() {
		return compareFile;
	}

	/**
	 * ファイルの差分を計算する
	 * @throws IOException
	 */
	public <T extends Sequence> EditList diff(ComparatorAlgorithms<T> algorithms) throws IOException {
		SequenceLoader<T> loader = algorithms.getSequenceLoader();
		T textB = loader.load(load(baseFile));
		T textA = loader.load(load(compareFile));

		return algorithms.getDiffAlgorithm()
			.diff(algorithms.getComparator(), textA, textB);
	}

	private static byte[] load(GitFile file) throws IOException {
		if(file == null) return new byte[0];
		return file.loadByteData();
	}
}
