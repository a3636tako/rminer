package jp.ac.osaka_u.ist.sdl.naturalness.component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jp.ac.osaka_u.ist.sdl.naturalness.NaturalnessException;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Document;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Tokenizer;

/**
 * ID化された文書の集合を表すクラス
 *
 * @author Ryo Arima
 *
 */
public class IndexedDocumentCollection {

	private Path path;

	/**
	 *
	 * @param path
	 *            保存されているファイルへのPath
	 */
	public IndexedDocumentCollection(Path path) {
		this.path = path;
	}

	/**
	 * この文書集合からモデルを推定する
	 *
	 * @param estimator
	 *            モデル推定器
	 * @param outputModel
	 *            推定結果を書き込むファイル
	 */
	public void estimateModel(LanguageModelEstimator estimator, Path outputModel) {
		estimator.estimate(path, outputModel);
	}

	public double query(LanguageModel model) {
		try(Stream<String> stream = Files.lines(path)) {
			return stream.map(line -> line.split("\\s+"))
				.map(array -> Arrays.stream(array)
					.map(Long::parseLong)
					.collect(Collectors.toList()))
				.flatMap(list -> model.estimate(list)
					.stream())
				.mapToDouble(Double::doubleValue)
				.average()
				.orElse(Double.NaN);
		} catch(IOException e) {
			throw new NaturalnessException();
		}
	}

	public Stream<List<Long>> open() throws IOException {
		return Files.lines(path)
			.map(this::scan)
			.map(stream -> stream.map(Long::parseLong)
				.collect(Collectors.toList()));
	}

	public Path getPath() {
		return path;
	}

	/**
	 * ソースコードをID化して文書集合を生成
	 *
	 * @param path
	 *            ID化した結果を書き込むファイル
	 * @param tokenizer
	 *            ソースコードのトークナイザ
	 * @param dictionary
	 *            辞書
	 * @param project
	 *            対象のソースコード
	 * @return 結果
	 */
	public static IndexedDocumentCollection index(Path path, Tokenizer tokenizer, Dictionary dictionary, Project project) {
		try(BufferedWriter writer = Files.newBufferedWriter(path)) {
			project.stream()
				.filter(file -> tokenizer.isTokenizable(file))
				.flatMap(file -> tokenizer.tokenize(file)
					.stream())
				.forEach(doc -> outputId(dictionary, writer, doc));
		} catch(IOException e) {
			throw new NaturalnessException(e);
		}

		return new IndexedDocumentCollection(path);
	}

	private static void outputId(Dictionary dictionary, BufferedWriter writer, Document doc) {
		boolean first = true;
		try {
			for(Token token : doc) {
				if(!first) writer.write(" ");
				long id = dictionary.getId(token.getSurface());
				writer.write(Long.toString(id));
				first = false;
			}
			writer.newLine();
		} catch(IOException e) {
			throw new NaturalnessException(e);
		}
	}

	private Stream<String> scan(String line) {
		Scanner sc = new Scanner(line);
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(sc, 0), false);
	}
}
