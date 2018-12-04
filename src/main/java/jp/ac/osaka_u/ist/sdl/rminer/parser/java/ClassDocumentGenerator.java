package jp.ac.osaka_u.ist.sdl.rminer.parser.java;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;

/**
 * クラスごとに文書を分割するDocumentGenerator
 */
public class ClassDocumentGenerator extends FileDocumentGenerator {
	private int typeNestDepth;

	@Override
	public void consumedToken(Statement statement, Token token, ParserTokenType type) {
		if(typeNestDepth == 0) return;
		super.consumedToken(statement, token, type);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		setOperation(() -> {
			if(typeNestDepth == 0) {
				SourceCode code = super.getOriginalSourceCode();
				putDocumentProperty(PROPERTY_PATH_KEY, code.getFileName() + node.getName()
					.getIdentifier());
			}
			typeNestDepth++;
		});
		super.visit(node);
		setOperation(() -> {
			typeNestDepth--;
			if(typeNestDepth == 0) {
				super.separateDocument();
			}
		});
		return false;
	}
}
