package jp.ac.osaka_u.ist.sdl.naturalness.component;

import java.nio.file.Path;
import java.util.Map;

/**
 * 言語モデル推定器のクラス
 */
public interface LanguageModelEstimator {
	/**
	 * モデル推定器を初期化
	 * @param parameter パラメータ
	 */
	void initialize(Map<String, String> parameter);

	/**
	 * モデルを推定
	 * @param dataPath 推定用データファイルへのパス
	 * @param outputPath 推定結果を書き込むファイル
	 */
	void estimate(Path dataPath, Path outputPath);
}
