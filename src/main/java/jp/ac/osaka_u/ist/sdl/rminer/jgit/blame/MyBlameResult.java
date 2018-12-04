package jp.ac.osaka_u.ist.sdl.rminer.jgit.blame;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.Commit;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.ComparatorAlgorithms;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitRepository;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.JavaText;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.JavaTextComparator;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.JavaTextLoader;
import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCodeFile;

public class MyBlameResult<T extends Sequence> implements BlameResult {

	private static final Logger log = LoggerFactory.getLogger(MyBlameResult.class);
	private GitRepository repository;
	private BlameGenerator<T> generator;
	private List<Element> results;
	private final String resultPath;

	private T resultContents;

	private int lastLength;

	public MyBlameResult(GitRepository repository, BlameGenerator<T> generator, String filePath, T contents) throws IOException {
		this.repository = repository;
		this.resultPath = filePath;
		this.resultContents = contents;
		this.generator = generator;

		int cnt = contents.size();
		results = IntStream.range(0, cnt)
			.mapToObj(v -> new Element())
			.collect(Collectors.toList());

	}

	@Override
	public int getResultSize() {
		return results.size();
	}

	@Override
	public Commit getSourceCommit(int i) {
		return results.get(i).sourceCommit;
	}

	@Override
	public String getSourcePath(int i) {
		return results.get(i).sourcePath;
	}

	@Override
	public int getSourceLine(int i) {
		return results.get(i).sourceLine - 1;
	}

	static class Element {
		Commit sourceCommit;
		String sourcePath;
		int sourceLine;

	}

	public static <T extends Sequence> MyBlameResult<T> create(GitRepository repository, BlameGenerator<T> gen) throws IOException {
		String path = gen.getResultPath();
		T contents = gen.getResultContents();
		if(contents == null) {
			gen.close();
			return null;
		}
		return new MyBlameResult<>(repository, gen, path, contents);
	}

	public void computeAll() throws IOException {
		BlameGenerator<T> gen = generator;
		if(gen == null)
			return;

		try {
			while(gen.next())
				loadFrom(gen);
		} finally {
			gen.close();
			generator = null;
		}

	}

	private void loadFrom(BlameGenerator<T> gen) {
		RevCommit srcCommit = gen.getSourceCommit();
		//PersonIdent srcAuthor = gen.getSourceAuthor();
		//PersonIdent srcCommitter = gen.getSourceCommitter();
		String srcPath = gen.getSourcePath();
		int srcLine = gen.getSourceStart();
		int resLine = gen.getResultStart();
		int resEnd = gen.getResultEnd();

		for(; resLine < resEnd; resLine++) {
			// Reverse blame can generate multiple results for the same line.
			// Favor the first one selected, as this is the oldest and most
			// likely to be nearest to the inquiry made by the user.
			Element element = results.get(resLine);
			if(element.sourceLine != 0)
				continue;

			element.sourceCommit = repository.getCommit(srcCommit);
			//sourceAuthors[resLine] = srcAuthor;
			//sourceCommitters[resLine] = srcCommitter;
			element.sourcePath = srcPath;

			// Since sourceLines is 1-based to permit hasSourceData to use 0 to
			// mean the line has not been annotated yet, pre-increment instead
			// of the traditional post-increment when making the assignment.
			element.sourceLine = ++srcLine;
		}
	}

	public static void main(String[] args) throws Exception {
		GitRepository repository = new GitRepository("../");
		ComparatorAlgorithms<JavaText> algorithms = ComparatorAlgorithms.of(DiffAlgorithm
			.getAlgorithm(SupportedAlgorithm.HISTOGRAM), new JavaTextComparator(), new JavaTextLoader());

		BlameResult result = new BlameCommand<JavaText>(repository)
			.setExecutorType(BlameCommand.ExecutorType.CUSTOM)
			.setFilePath("exam/src/main/java/exam/Main.java")
			.setAlgorithms(algorithms)
			.call();
		JavaText text = new JavaText(new SourceCodeFile(Paths.get("../exam/src/main/java/exam/Main.java")));
		for(int i = 0; i < result.getResultSize(); i++) {
			System.out.printf("%4d, %s, %d, %s, %s\n", i, result.getSourceCommit(i).getId().getName(), result.getSourceLine(i), result.getSourcePath(i), text.getToken(i));
		}
	}
}
