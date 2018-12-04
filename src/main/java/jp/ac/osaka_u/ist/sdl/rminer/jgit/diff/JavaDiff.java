package jp.ac.osaka_u.ist.sdl.rminer.jgit.diff;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.SequenceComparator;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.ComparatorAlgorithms;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.JavaText;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.JavaTextComparator;
import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCodeFile;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;

public class JavaDiff<T extends Sequence> {

	DiffAlgorithm algorithm;
	SequenceComparator<T> comparator;
	BlockGet<T> blockGet;

	public String getBlock(T target, int begin, int end) {
		return blockGet.get(target, begin, end);
	}

	public JavaDiff(ComparatorAlgorithms<T> algos, BlockGet<T> blockGet) {
		this.algorithm = algos.getDiffAlgorithm();
		this.comparator = algos.getComparator();
		this.blockGet = blockGet;
	}

	public void output(T a, T b) {
		EditList diff = algorithm.diff(comparator, a, b);
		for(Edit e : diff) {
			System.out.printf("old : %d-%d>>>>>\n", e.getBeginA(), e.getEndA());
			System.out.print(getBlock(a, e.getBeginA(), e.getEndA()));
			System.out.println("=========================");
			System.out.print(getBlock(b, e.getBeginB(), e.getEndB()));
			System.out.printf("<<<<< new : %d-%d\n", e.getBeginB(), e.getEndB());
		}
	}

	public static interface BlockGet<T extends Sequence> {
		String get(T val, int begin, int end);
	}

	public static void main(String[] args) throws IOException {
		JavaText texta = new JavaText(new SourceCodeFile(Paths.get(args[0])));
		JavaText textb = new JavaText(new SourceCodeFile(Paths.get(args[1])));

		ComparatorAlgorithms<JavaText> algorithms = ComparatorAlgorithms
			.of(DiffAlgorithm.getAlgorithm(SupportedAlgorithm.HISTOGRAM), new JavaTextComparator(), null);
		JavaDiff<JavaText> javaDiff = new JavaDiff<>(algorithms, (target, begin, end) -> {
			String before = target.tokens()
				.subList(Math.max(0, begin - 3), begin)
				.stream()
				.map(Token::getSurface)
				.collect(Collectors.joining(" "));

			String center = target.tokens()
				.subList(begin, end)
				.stream()
				.map(Token::getSurface)
				.collect(Collectors.joining(" "));
			
			String after = target.tokens()
				.subList(end, Math.min(target.size(), end + 3))
				.stream()
				.map(Token::getSurface)
				.collect(Collectors.joining(" "));
			return before + " \033[34m " + center + " \033[0m " + after + "\n";
		});
		javaDiff.output(texta, textb);
	}
}
