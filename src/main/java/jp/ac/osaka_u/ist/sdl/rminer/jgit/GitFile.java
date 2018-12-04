package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * あるコミット時点でのファイルを表す
 */
public class GitFile {
	public static interface GitFileBuilder<T extends GitFile> {
		TreeFilter getFilter();

		T build(Commit commit, TreeWalk walk);

		static GitFileBuilder<GitFile> DEFAULT_FILE_BUILDER = new GitFileBuilder<GitFile>() {
			@Override
			public TreeFilter getFilter() {
				return TreeFilter.ALL;
			}

			@Override
			public GitFile build(Commit commit, TreeWalk walk) {
				return new GitFile(commit, walk.getPathString(), walk.getObjectId(0));
			}
		};
	}

	private String path;
	private ObjectId objectId;
	private Commit commit;

	GitFile(Commit commit, String path, ObjectId objectId) {
		this.path = path;
		this.objectId = objectId;
		this.commit = commit;
	}

	/**
	 * ファイルのパス
	 * @return パス
	 */
	public String getPath() {
		return path;
	}

	/**
	 * ファイルのID
	 * @return ID
	 */
	public ObjectId getObjectId() {
		return objectId;
	}

	/**
	 * 中身を読み込むストリームを開く
	 * @return ファイルのInputStream
	 * @throws IOException
	 */
	public InputStream openStream() throws IOException {
		ObjectLoader loader = commit.getRepository()
			.getObjectReader()
			.open(objectId, Constants.OBJ_BLOB);
		return loader.openStream();
	}

	/**
	 * ファイルの中身を文字配列として読み込む
	 * @return 文字配列
	 * @throws IOException
	 */
	public char[] loadCharData() throws IOException {
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

	public byte[] loadByteData() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try(InputStream is = openStream()) {
			int nRead;
			byte[] data = new byte[1024];
			while((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
		}
		buffer.flush();
		return buffer.toByteArray();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		GitFile other = (GitFile)obj;
		if(objectId == null) {
			if(other.objectId != null) return false;
		} else if(!objectId.equals(other.objectId)) return false;
		return true;
	}
}
