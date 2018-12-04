package jp.ac.osaka_u.ist.sdl.rminer.parser.java;

import org.eclipse.jdt.core.dom.CompilationUnit;

import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCode;

/**
 * ファイルごとに文書を分割するDocumentGenerator
 */
public class FileDocumentGenerator extends AbstractDocumentGenerator {
	@Override
	public boolean visit(CompilationUnit node) {
		setOperation(() -> {
			SourceCode code = super.getOriginalSourceCode();
			putDocumentProperty(PROPERTY_PATH_KEY, code.getFileName());
		});
		super.visit(node);
		setOperation(() -> {
			separateDocument();
		});
		return false;
	}
}
