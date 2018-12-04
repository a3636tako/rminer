package jp.ac.osaka_u.ist.sdl.rminer.parser.java;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;

/**
 * メソッドごとに文書を分割するDocumentGenerator
 */
public class MethodDocumentGenerator extends AbstractDocumentGenerator {
	private int methodNestDepth;
	private int bodyNestDepth;
	private int typeNestDepth;
	private String currentClassName;
	private boolean usingToken;

	@Override
	public void consumedToken(Statement statement, Token token, ParserTokenType type) {
		if(methodNestDepth == 0 || !usingToken) return;
		super.consumedToken(statement, token, type);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		setOperation(() -> {
			if(typeNestDepth == 0) {
				currentClassName = node.getName()
					.getIdentifier();
			}
			typeNestDepth++;
		});
		super.visit(node);
		setOperation(() -> {
			typeNestDepth--;
		});
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		setOperation(() -> {
			usingToken = false;
			if(methodNestDepth == 0) {
				SourceCode code = super.getOriginalSourceCode();
				putDocumentProperty(PROPERTY_PATH_KEY, code
					.getFileName() + currentClassName + "." + constructMethodName(node));
			}
			methodNestDepth++;
		});

		super.visit(node);

		setOperation(() -> {
			methodNestDepth--;
			if(methodNestDepth == 0) {
				super.separateDocument();
			}
		});
		return false;
	}

	@Override
	public boolean visit(Block node) {
		setOperation(() -> {
			usingToken = true;
			bodyNestDepth++;
		});

		super.visit(node);

		setOperation(() -> {
			bodyNestDepth--;
		});
		return false;
	}

	private static String constructMethodName(MethodDeclaration node) {

		StringBuilder build = new StringBuilder();
		build.append(node.getName()
			.getIdentifier());
		build.append('(');

		@SuppressWarnings("unchecked")
		List<SingleVariableDeclaration> parameters = node.parameters();
		String parameter = parameters.stream()
			.map(v -> v.getType()
				.toString())
			.collect(Collectors.joining(","));

		build.append(parameter);
		return build.toString();

	}
}
