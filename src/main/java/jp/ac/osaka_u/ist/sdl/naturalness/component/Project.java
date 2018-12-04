package jp.ac.osaka_u.ist.sdl.naturalness.component;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.ac.osaka_u.ist.sdl.naturalness.NaturalnessException;
import jp.ac.osaka_u.ist.sdl.rminer.parser.TokenizedCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Tokenizer;

/**
 * 解析対象のソースコードの集合を表すクラス
 */
public class Project implements Iterable<ProjectFile> {

	private Path directory;
	private List<ProjectFile> files;
	private List<Predicate<ProjectFile>> filters = new ArrayList<>();

	/**
	 * あるディレクトリ以下のファイルを解析対象としたProjectを生成
	 *
	 * @param directory
	 *            対象ディレクトリ
	 */
	public Project(Path directory) {
		this.directory = directory;
	}

	/**
	 * ファイルのリストを解析対象としたProjectを生成
	 *
	 * @param list
	 *            対象ファイルのリスト
	 */
	public Project(List<ProjectFile> list) {
		this.files = list;
	}

	@Override
	public Iterator<ProjectFile> iterator() {
		return stream().iterator();
	}

	public Stream<ProjectFile> stream() {
		Stream<ProjectFile> stream;
		if(files != null) {
			stream = files.stream();
		} else {
			try {
				stream = Files.walk(directory, FileVisitOption.FOLLOW_LINKS)
					.filter(path -> !Files.isDirectory(path))
					.map(directory::relativize)
					.map(p -> new ProjectFile(directory, p));
			} catch(IOException e) {
				throw new NaturalnessException(e);
			}
		}

		for(Predicate<ProjectFile> filter : filters) {
			stream = stream.filter(filter);
		}

		return stream;
	}

	public void addFileter(Predicate<ProjectFile> filter) {
		filters.add(filter);
	}

	/**
	 * 対象ファイルをトークン化する
	 *
	 * @param tokenizer
	 *            トークナイザ
	 * @return トークン化されたファイルたち
	 */
	public TokenizedProject tokenize(Tokenizer tokenizer) {
		try(Stream<ProjectFile> stream = this.stream()) {
			List<TokenizedCode> docs = stream
				.filter(tokenizer::isTokenizable)
				.map(tokenizer::tokenize)
				.collect(Collectors.toList());

			return new TokenizedProject(docs);
		}
	}

	/**
	 * モデル推定用に対象ファイルをトークン化、ID化する。結果は一時ファイルに書き込まれる
	 *
	 * @param tokenizer
	 *            トークナイザ
	 * @param dictionary
	 *            辞書
	 * @return ID化の結果
	 */
	public IndexedDocumentCollection index(Tokenizer tokenizer, Dictionary dictionary) {
		try {
			return index(tokenizer, dictionary, Files.createTempFile(null, null));
		} catch(IOException e) {
			throw new NaturalnessException(e);
		}
	}

	/**
	 * モデル推定用に対象ファイルをトークン化、ID化する。
	 *
	 * @param tokenizer
	 *            トークナイザ
	 * @param dictionary
	 *            辞書
	 * @param path
	 *            結果を書き込むファイルのパス
	 * @return ID化の結果
	 */
	public IndexedDocumentCollection index(Tokenizer tokenizer, Dictionary dictionary, Path path) {
		return IndexedDocumentCollection.index(path, tokenizer, dictionary, this);
	}
}
