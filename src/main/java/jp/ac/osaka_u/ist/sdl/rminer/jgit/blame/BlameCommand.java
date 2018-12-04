package jp.ac.osaka_u.ist.sdl.rminer.jgit.blame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.SequenceComparator;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.CoreConfig.AutoCRLF;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.util.IO;
import org.eclipse.jgit.util.io.AutoLFInputStream;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.ComparatorAlgorithms;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitRepository;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.SequenceLoader;

public class BlameCommand<T extends Sequence> {
	public static enum ExecutorType {
		JGIT, NATIVE, CUSTOM
	}

	private GitRepository repository;
	private String filePath;
	private DiffAlgorithm diffAlgorithm;
	private RawTextComparator textComparator;
	private AnyObjectId startCommit;
	private boolean follow;
	private AnyObjectId endCommit;
	private Collection<ObjectId> endCommits;
	private ExecutorType type;

	private ComparatorAlgorithms<T> algorithms;

	public BlameCommand(GitRepository repository) {
		this.repository = repository;
	}

	public BlameCommand<T> setFilePath(String filePath) {
		this.filePath = filePath;
		return this;
	}

	public BlameCommand<T> setDiffAlgorithm(DiffAlgorithm diffAlgorithm) {
		this.diffAlgorithm = diffAlgorithm;
		return this;
	}

	public BlameCommand<T> setTextComparator(RawTextComparator textComparator) {
		this.textComparator = textComparator;
		return this;
	}

	public BlameCommand<T> setAlgorithms(ComparatorAlgorithms<T> algorithms) {
		this.algorithms = algorithms;
		return this;
	}

	public BlameCommand<T> setStartCommit(AnyObjectId commit) {
		this.startCommit = commit;
		return this;
	}

	public BlameCommand<T> setFollowFileRenames(boolean follow) {
		this.follow = follow;
		return this;
	}

	public BlameCommand<T> reverse(AnyObjectId start, AnyObjectId end) throws IOException {
		this.startCommit = start;
		this.endCommit = end;
		return this;
	}

	public BlameCommand<T> reverse(AnyObjectId start, Collection<ObjectId> end) throws IOException {
		this.startCommit = start;
		this.endCommits = end;
		return this;
	}

	public BlameCommand<T> setExecutorType(ExecutorType type) {
		this.type = type;
		return this;
	}

	public BlameResult call() throws GitAPIException, IOException {
		if(type == ExecutorType.NATIVE) {
			return new NativeBlameResult(repository, startCommit, filePath);

		} else if(type == ExecutorType.CUSTOM) {
			BlameGenerator<T> generator = new BlameGenerator<>(repository, algorithms.getSequenceLoader(), filePath);
			generator.setDiffAlgorithm(algorithms.getDiffAlgorithm());
			generator.setTextComparator(algorithms.getComparator());

			if(startCommit != null)
				generator.push(null, startCommit);
			else {
				generator.push(null, repository.getRepository()
					.resolve(Constants.HEAD));
				if(!repository.getRepository()
					.isBare()) {
					DirCache dc = repository.getRepository()
						.readDirCache();
					int entry = dc.findEntry(filePath);
					if(0 <= entry)
						generator.push(null, dc.getEntry(entry)
							.getObjectId());

					File inTree = new File(repository.getRepository()
						.getWorkTree(), filePath);
					if(repository.getRepository()
						.getFS()
						.isFile(inTree)) {
						T rawText = getRawText(algorithms.getSequenceLoader(), inTree);
						generator.push(null, rawText);
					}
				}
			}

			return generator.computeBlameResult();
		} else {
			try(Git git = new Git(repository.getRepository())) {
				org.eclipse.jgit.api.BlameCommand blameCommand = git.blame();
				if(filePath != null) blameCommand.setFilePath(filePath);
				if(diffAlgorithm != null) blameCommand.setDiffAlgorithm(diffAlgorithm);
				if(textComparator != null) blameCommand.setTextComparator(textComparator);
				blameCommand.setFollowFileRenames(follow);
				if(endCommit != null) {
					blameCommand.reverse(startCommit, endCommit);
				} else if(endCommits != null) {
					blameCommand.reverse(startCommit, endCommits);
				} else if(startCommit != null) {
					blameCommand.setStartCommit(startCommit);
				}

				return new JGitBlameResult(repository, blameCommand.call());
			}
		}
	}

	private T getRawText(SequenceLoader<T> loader, File inTree) throws IOException, FileNotFoundException {
		T rawText;

		WorkingTreeOptions workingTreeOptions = repository.getRepository()
			.getConfig()
			.get(WorkingTreeOptions.KEY);
		AutoCRLF autoCRLF = workingTreeOptions.getAutoCRLF();
		switch(autoCRLF){
		case FALSE:
		case INPUT:
			// Git used the repo format on checkout, but other tools
			// may change the format to CRLF. We ignore that here.
			rawText = loader.load(toByteArray(new FileInputStream(inTree), (int)inTree.length()));
			break;
		case TRUE:
			try(AutoLFInputStream in = new AutoLFInputStream(new FileInputStream(inTree), true)) {
				// Canonicalization should lead to same or shorter length
				// (CRLF to LF), so the file size on disk is an upper size bound
				rawText = loader.load(toByteArray(in, (int)inTree.length()));
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown autocrlf option " + autoCRLF); //$NON-NLS-1$
		}
		return rawText;
	}

	private static byte[] toByteArray(InputStream source, int upperSizeLimit) throws IOException {
		byte[] buffer = new byte[upperSizeLimit];
		try {
			int read = IO.readFully(source, buffer, 0);
			if(read == upperSizeLimit)
				return buffer;
			else {
				byte[] copy = new byte[read];
				System.arraycopy(buffer, 0, copy, 0, read);
				return copy;
			}
		} finally {
			source.close();
		}
	}
}
