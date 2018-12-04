package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import org.eclipse.jgit.diff.SequenceComparator;

public class JavaTextComparator extends SequenceComparator<JavaText> {

	@Override
	public boolean equals(JavaText a, int ai, JavaText b, int bi) {
		String as = a.getToken(ai);
		String bs = b.getToken(bi);

		return as.equals(bs);
	}

	@Override
	public int hash(JavaText seq, int ptr) {
		return seq.getToken(ptr)
			.hashCode();
	}

}
