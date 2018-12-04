package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.SequenceComparator;

public interface ComparatorAlgorithms<T extends Sequence> {
	DiffAlgorithm getDiffAlgorithm();

	SequenceComparator<T> getComparator();

	SequenceLoader<T> getSequenceLoader();

	public static ComparatorAlgorithms<RawText> DEFAULT = of(DiffAlgorithm
		.getAlgorithm(SupportedAlgorithm.HISTOGRAM), RawTextComparator.DEFAULT, RawText::new);

	public static <T extends Sequence> ComparatorAlgorithms<T> of(DiffAlgorithm algorithm, SequenceComparator<T> comparator,SequenceLoader<T> loader) {
		return new ComparatorAlgorithms<T>() {

			@Override
			public DiffAlgorithm getDiffAlgorithm() {
				return algorithm;
			}

			@Override
			public SequenceComparator<T> getComparator() {
				return comparator;
			}

			@Override
			public SequenceLoader<T> getSequenceLoader() {
				return loader;
			}
		};
	}

	public static interface ExceptionFunction<T, R, E extends Exception> {
		R apply(T t) throws E;
	}
}
