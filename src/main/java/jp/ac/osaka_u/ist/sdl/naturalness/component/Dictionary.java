package jp.ac.osaka_u.ist.sdl.naturalness.component;

import java.nio.file.Path;

/**
 * 単語とIDの相互変換を行う
 *
 */
public interface Dictionary {
	/**
	 * 単語からIDに変換する
	 * 辞書に存在しない単語の場合、新しくIDを割り当てる
	 * @param str 単語
	 * @return 単語ID
	 */
	long getId(String str);

	/**
	 * getIdで未知語のIDを生成するかどうか
	 * @param generateId
	 */
	void setGeneratingId(boolean generateId);

	boolean isGeneratingId();

	/**
	 * IDから単語に変換する
	 * @param id 単語ID
	 * @return 単語
	 */
	String getSurface(long id);

	/**
	 * ファイルから辞書を読み込む
	 * @param path データのパス
	 */
	void load(Path path);

	/**
	 * ファイルへ辞書を書き込む
	 * @param path データのパス
	 */
	void store(Path path);
	
	int getSize();
}
