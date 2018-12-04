package jp.ac.osaka_u.ist.sdl.rminer.jgit.diff;

/*
import static org.eclipse.jgit.diff.DiffEntry.Side.NEW;
import static org.eclipse.jgit.diff.DiffEntry.Side.OLD;
import static org.eclipse.jgit.lib.Constants.encodeASCII;
import static org.eclipse.jgit.lib.FileMode.GITLINK;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter.FormatResult;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.SequenceComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.BinaryBlobException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.FileHeader.PatchType;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.pack.PackConfig;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.LfsFactory;

public class CustomDiffFormatter<T extends Sequence> extends DiffFormatter {
	private static final int DEFAULT_BINARY_FILE_THRESHOLD = PackConfig.DEFAULT_BIG_FILE_THRESHOLD;
	private T EMPTY;
	private ContentSource.Pair source;

	private DiffAlgorithm diffAlgorithm;

	private SequenceComparator<T> comparator;
	private final OutputStream out;
	private int context = 3;

	private Repository repository;
	private int binaryFileThreshold = DEFAULT_BINARY_FILE_THRESHOLD;

	private ObjectReader reader;

	private T load(ObjectLoader loader) {
		return null;
	}

	public CustomDiffFormatter(OutputStream out) {
		super(out);
	}

	@Override
	protected OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return super.getOutputStream();
	}

	@Override
	public void setRepository(Repository repository) {
		// TODO Auto-generated method stub
		super.setRepository(repository);
	}

	@Override
	public void setReader(ObjectReader reader, Config cfg) {
		// TODO Auto-generated method stub
		super.setReader(reader, cfg);
	}

	@Override
	public void setContext(int lineCount) {
		// TODO Auto-generated method stub
		super.setContext(lineCount);
	}

	@Override
	public void setAbbreviationLength(int count) {
		// TODO Auto-generated method stub
		super.setAbbreviationLength(count);
	}

	@Override
	public void setDiffAlgorithm(DiffAlgorithm alg) {
		// TODO Auto-generated method stub
		super.setDiffAlgorithm(alg);
	}

	@Override
	public void setDiffComparator(RawTextComparator cmp) {
		this.setDiffComparator((SequenceComparator<RawText>)cmp);
	}

	public void setDiffComparator(SequenceComparator<?> cmp) {

	}

	@Override
	public void setBinaryFileThreshold(int threshold) {
		// TODO Auto-generated method stub
		super.setBinaryFileThreshold(threshold);
	}

	@Override
	public void setOldPrefix(String prefix) {
		// TODO Auto-generated method stub
		super.setOldPrefix(prefix);
	}

	@Override
	public String getOldPrefix() {
		// TODO Auto-generated method stub
		return super.getOldPrefix();
	}

	@Override
	public void setNewPrefix(String prefix) {
		// TODO Auto-generated method stub
		super.setNewPrefix(prefix);
	}

	@Override
	public String getNewPrefix() {
		// TODO Auto-generated method stub
		return super.getNewPrefix();
	}

	@Override
	public boolean isDetectRenames() {
		// TODO Auto-generated method stub
		return super.isDetectRenames();
	}

	@Override
	public void setDetectRenames(boolean on) {
		// TODO Auto-generated method stub
		super.setDetectRenames(on);
	}

	@Override
	public RenameDetector getRenameDetector() {
		// TODO Auto-generated method stub
		return super.getRenameDetector();
	}

	@Override
	public void setProgressMonitor(ProgressMonitor pm) {
		// TODO Auto-generated method stub
		super.setProgressMonitor(pm);
	}

	@Override
	public void setPathFilter(TreeFilter filter) {
		// TODO Auto-generated method stub
		super.setPathFilter(filter);
	}

	@Override
	public TreeFilter getPathFilter() {
		// TODO Auto-generated method stub
		return super.getPathFilter();
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		super.flush();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
	}

	@Override
	public List<DiffEntry> scan(AnyObjectId a, AnyObjectId b) throws IOException {
		return super.scan(a, b);
	}

	@Override
	public List<DiffEntry> scan(RevTree a, RevTree b) throws IOException {
		return super.scan(a, b);
	}

	@Override
	public List<DiffEntry> scan(AbstractTreeIterator a, AbstractTreeIterator b) throws IOException {
		return super.scan(a, b);
	}

	@Override
	public void format(AnyObjectId a, AnyObjectId b) throws IOException {
		super.format(a, b);
	}

	@Override
	public void format(RevTree a, RevTree b) throws IOException {
		super.format(a, b);
	}

	@Override
	public void format(AbstractTreeIterator a, AbstractTreeIterator b) throws IOException {
		super.format(a, b);
	}

	@Override
	public void format(List<? extends DiffEntry> entries) throws IOException {
		super.format(entries);
	}

	@Override
	public void format(DiffEntry ent) throws IOException {
		FormatResult res = createFormatResult(ent);
		format(res.header, res.a, res.b);
	}

	public void format(FileHeader head, T a, T b) throws IOException {
		// Reuse the existing FileHeader as-is by blindly copying its
		// header lines, but avoiding its hunks. Instead we recreate
		// the hunks from the text instances we have been supplied.
		//
		final int start = head.getStartOffset();
		int end = head.getEndOffset();
		if (!head.getHunks().isEmpty())
			end = head.getHunks().get(0).getStartOffset();
		out.write(head.getBuffer(), start, end - start);
		if (head.getPatchType() == PatchType.UNIFIED)
			format(head.toEditList(), a, b);
	}

	public void format(EditList edits, T a, T b) throws IOException {
		for (int curIdx = 0; curIdx < edits.size();) {
			Edit curEdit = edits.get(curIdx);
			final int endIdx = findCombinedEnd(edits, curIdx);
			final Edit endEdit = edits.get(endIdx);

			int aCur = (int) Math.max(0, (long) curEdit.getBeginA() - context);
			int bCur = (int) Math.max(0, (long) curEdit.getBeginB() - context);
			final int aEnd = (int) Math.min(a.size(), (long) endEdit.getEndA() + context);
			final int bEnd = (int) Math.min(b.size(), (long) endEdit.getEndB() + context);

			writeHunkHeader(aCur, aEnd, bCur, bEnd);

			while (aCur < aEnd || bCur < bEnd) {
				if (aCur < curEdit.getBeginA() || endIdx + 1 < curIdx) {
					writeContextLine(a, aCur);
					if (isEndOfLineMissing(a, aCur))
						out.write(noNewLine);
					aCur++;
					bCur++;
				} else if (aCur < curEdit.getEndA()) {
					writeRemovedLine(a, aCur);
					if (isEndOfLineMissing(a, aCur))
						out.write(noNewLine);
					aCur++;
				} else if (bCur < curEdit.getEndB()) {
					writeAddedLine(b, bCur);
					if (isEndOfLineMissing(b, bCur))
						out.write(noNewLine);
					bCur++;
				}

				if (end(curEdit, aCur, bCur) && ++curIdx < edits.size())
					curEdit = edits.get(curIdx);
			}
		}
	}

	@Override
	protected void writeContextLine(T text, int line) throws IOException {
		// TODO Auto-generated method stub
		//super.writeContextLine(text, line);
	}

	@Override
	protected void writeAddedLine(T text, int line) throws IOException {
		// TODO Auto-generated method stub
		//super.writeAddedLine(text, line);
	}

	@Override
	protected void writeRemovedLine(T text, int line) throws IOException {
		// TODO Auto-generated method stub
		//super.writeRemovedLine(text, line);
	}

	@Override
	protected void writeHunkHeader(int aStartLine, int aEndLine, int bStartLine, int bEndLine) throws IOException {
		// TODO Auto-generated method stub
		//super.writeHunkHeader(aStartLine, aEndLine, bStartLine, bEndLine);
	}

	@Override
	protected void writeLine(char prefix, T text, int cur) throws IOException {
		// TODO Auto-generated method stub
		//super.writeLine(prefix, text, cur);
	}

	@Override
	public FileHeader toFileHeader(DiffEntry ent) throws IOException, CorruptObjectException, MissingObjectException {
		// TODO Auto-generated method stub
		return super.toFileHeader(ent);
	}

	class FormatResult {
		FileHeader header;
		T a;
		T b;
	}

	private FormatResult createFormatResult(DiffEntry ent) throws IOException, CorruptObjectException, MissingObjectException {
		final FormatResult res = new FormatResult();
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		final EditList editList;
		final FileHeader.PatchType type;

		//formatHeader(buf, ent);

		if(ent.getOldId() == null || ent.getNewId() == null) {
			// Content not changed (e.g. only mode, pure rename)
			editList = new EditList();
			type = PatchType.UNIFIED;
			res.header = new FileHeader(buf.toByteArray(), editList, type);
			return res;
		}

		//assertHaveReader();

		T aRaw = null;
		T bRaw = null;
		if(ent.getOldMode() == GITLINK || ent.getNewMode() == GITLINK) {
			//aRaw = new RawText(writeGitLinkText(ent.getOldId()));
			//bRaw = new RawText(writeGitLinkText(ent.getNewId()));
		} else {
			try {
				aRaw = open(OLD, ent);
				bRaw = open(NEW, ent);
			} catch(BinaryBlobException e) {
				// Do nothing; we check for null below.
				//formatOldNewPaths(buf, ent);
				buf.write(encodeASCII("Binary files differ\n")); //$NON-NLS-1$
				editList = new EditList();
				type = PatchType.BINARY;
				res.header = new FileHeader(buf.toByteArray(), editList, type);
				return res;
			}
		}

		res.a = aRaw;
		res.b = bRaw;
		editList = diff(res.a, res.b);
		type = PatchType.UNIFIED;

		switch(ent.getChangeType()){
		case RENAME:
		case COPY:
			if(!editList.isEmpty())
				//formatOldNewPaths(buf, ent);
				break;

		default:
			//formatOldNewPaths(buf, ent);
			break;
		}

		res.header = new FileHeader(buf.toByteArray(), editList, type);
		return res;
	}

	private EditList diff(T a, T b) {
		return diffAlgorithm.diff(comparator, a, b);
	}

	private T open(DiffEntry.Side side, DiffEntry entry) throws IOException, BinaryBlobException {
		if(entry.getMode(side) == FileMode.MISSING)
			return EMPTY;

		if(entry.getMode(side)
			.getObjectType() != Constants.OBJ_BLOB)
			return EMPTY;

		AbbreviatedObjectId id = entry.getId(side);
		if(!id.isComplete()) {
			Collection<ObjectId> ids = reader.resolve(id);
			if(ids.size() == 1) {
				id = AbbreviatedObjectId.fromObjectId(ids.iterator()
					.next());
				switch(side){
				case OLD:
					//entry.oldId = id;
					break;
				case NEW:
					//entry.newId = id;
					break;
				}
			} else if(ids.size() == 0)
				throw new MissingObjectException(id, Constants.OBJ_BLOB);
			else throw new AmbiguousObjectException(id, ids);
		}

		ObjectLoader ldr = LfsFactory.getInstance()
			.applySmudgeFilter(repository, source.open(side, entry), entry.getDiffAttribute());
		return load(ldr);
	}

	@Override
	protected void formatGitDiffFirstHeaderLine(ByteArrayOutputStream o, ChangeType type, String oldPath, String newPath) throws IOException {
		// TODO Auto-generated method stub
		super.formatGitDiffFirstHeaderLine(o, type, oldPath, newPath);
	}

	@Override
	protected void formatIndexLine(OutputStream o, DiffEntry ent) throws IOException {
		// TODO Auto-generated method stub
		super.formatIndexLine(o, ent);
	}


	private int findCombinedEnd(List<Edit> edits, int i) {
		int end = i + 1;
		while (end < edits.size()
				&& (combineA(edits, end) || combineB(edits, end)))
			end++;
		return end - 1;
	}

	private boolean combineA(List<Edit> e, int i) {
		return e.get(i).getBeginA() - e.get(i - 1).getEndA() <= 2 * context;
	}

	private boolean combineB(List<Edit> e, int i) {
		return e.get(i).getBeginB() - e.get(i - 1).getEndB() <= 2 * context;
	}

	private static boolean end(Edit edit, int a, int b) {
		return edit.getEndA() <= a && edit.getEndB() <= b;
	}
}
*/
