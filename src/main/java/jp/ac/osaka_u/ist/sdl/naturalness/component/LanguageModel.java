package jp.ac.osaka_u.ist.sdl.naturalness.component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 言語モデルを表す
 */
public interface LanguageModel {
	/**
	 * 言語モデルを初期化
	 * @param modelPath モデルデータが保存されているパス
	 * @param parameter モデルのパラメータ
	 */
	void initialize(Path modelPath, Map<String, String> parameter);

	/**
	 * 自然さを計測
	 * @param id 計測対象のトークン列
	 * @return 各トークンの自然さ
	 */
	List<Double> estimate(List<Long> id);

	default void finalizeModel() {
	}
}
