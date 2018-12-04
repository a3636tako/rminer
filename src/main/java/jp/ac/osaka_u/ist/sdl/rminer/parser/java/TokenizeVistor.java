package jp.ac.osaka_u.ist.sdl.rminer.parser.java;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;

/**
 * トークンにパーサによる意味情報を結びつけるASTVisitor
 */
@SuppressWarnings({"rawtypes", "deprecation"})
abstract public class TokenizeVistor extends ASTVisitor {

	private boolean isTypeName = false;
	private boolean isPackageName = false;
	private int qulifyNest = 0;
	public static final String PROPERTIES_KEY_OF_TYPE = "parserTokenType";
	public static final String STATEMENT_KEY_OF_TYPE = "statement";

	private Deque<VisitElement> tokenQueue;
	private Deque<Statement> currentStatement;

	private static class VisitElement {
		Runnable run;
		ParserTokenType type;
		String surface;
		Statement current;

		public VisitElement(Statement current, ParserTokenType type, String surface) {
			this.current = current;
			this.type = type;
			this.surface = surface;
		}

		public VisitElement(Runnable run) {
			this.run = run;
		}
	}

	public TokenizeVistor() {
		tokenQueue = new ArrayDeque<>();
		currentStatement = new ArrayDeque<>();

	}

	abstract public Token getCurrentToken();

	abstract public void nextToken();

	public void consumedToken(Statement statement, Token token, ParserTokenType type) {
		token.putProperty(PROPERTIES_KEY_OF_TYPE, type.name());

		token.putProperty(STATEMENT_KEY_OF_TYPE, getStatementType(statement));
	}

	public String getStatementType(Statement statement) {
		String statementType;
		if(statement == null) {
			statementType = "None";
		} else {
			statementType = statement.getClass()
				.getSimpleName();
		}
		return statementType;
	}

	private void token(Statement statement, ParserTokenType type, String surface, int posBefore) {
		while(getCurrentToken() != null && getCurrentToken().getEndPosition() <= posBefore) {
			Token prev = getCurrentToken();
			nextToken();
			if(prev.getSurface()
				.equals(surface)) {
				consumedToken(statement, prev, type);
				return;
			} else {
				consumedToken(statement, prev, ParserTokenType.UNKNOWN);
			}
		}
	}

	private void consumeQueue(ASTNode node, int point) {
		for(VisitElement e : tokenQueue) {
			if(e.run != null) e.run.run();
			else token(e.current, e.type, e.surface, point);
		}
		tokenQueue.clear();
	}

	private void token(ParserTokenType type, String surface) {
		tokenQueue.add(new VisitElement(currentStatement.peek(), type, surface));
	}

	protected void setOperation(Runnable run) {
		tokenQueue.add(new VisitElement(run));
	}

	@Override
	public void preVisit(ASTNode node) {
		if(node instanceof Statement) {
			currentStatement.push((Statement)node);
		}
		consumeQueue(node, node.getStartPosition());
	}

	@Override
	public void postVisit(ASTNode node) {
		if(node instanceof Statement) {
			currentStatement.pop();
		}
		consumeQueue(node, node.getStartPosition() + node.getLength());
	}

	/**
	 * Internal synonym for {@link ClassInstanceCreation#getName()}. Use to
	 * alleviate deprecation warnings.
	 *
	 * @deprecated
	 * @since 3.4
	 */
	@Deprecated
	private Name getName(ClassInstanceCreation node) {
		return node.getName();
	}

	/**
	 * Internal synonym for {@link MethodDeclaration#getReturnType()}. Use to
	 * alleviate deprecation warnings.
	 *
	 * @deprecated
	 * @since 3.4
	 */
	@Deprecated
	private static Type getReturnType(MethodDeclaration node) {
		return node.getReturnType();
	}

	/**
	 * Internal synonym for {@link TypeDeclaration#getSuperclass()}. Use to
	 * alleviate deprecation warnings.
	 *
	 * @deprecated
	 * @since 3.4
	 */
	@Deprecated
	private static Name getSuperclass(TypeDeclaration node) {
		return node.getSuperclass();
	}

	/**
	 * Internal synonym for
	 * {@link TypeDeclarationStatement#getTypeDeclaration()}. Use to alleviate
	 * deprecation warnings.
	 *
	 * @deprecated
	 * @since 3.4
	 */
	@Deprecated
	private static TypeDeclaration getTypeDeclaration(TypeDeclarationStatement node) {
		return node.getTypeDeclaration();
	}

	/**
	 * Internal synonym for {@link MethodDeclaration#thrownExceptions()}. Use to
	 * alleviate deprecation warnings.
	 *
	 * @deprecated
	 * @since 3.10
	 */
	@Deprecated
	private static List thrownExceptions(MethodDeclaration node) {
		return node.thrownExceptions();
	}

	/**
	 * Appends the text representation of the given modifier flags, followed by
	 * a single space. Used for JLS2 modifiers.
	 *
	 * @param modifiers
	 *            the modifier flags
	 */
	private void printModifiers(int modifiers) {
		if(Modifier.isPublic(modifiers)) {
			token(ParserTokenType.KEYWORD, "public");//$NON-NLS-1$
		}
		if(Modifier.isProtected(modifiers)) {
			token(ParserTokenType.KEYWORD, "protected");//$NON-NLS-1$
		}
		if(Modifier.isPrivate(modifiers)) {
			token(ParserTokenType.KEYWORD, "private");//$NON-NLS-1$
		}
		if(Modifier.isStatic(modifiers)) {
			token(ParserTokenType.KEYWORD, "static");//$NON-NLS-1$
		}
		if(Modifier.isAbstract(modifiers)) {
			token(ParserTokenType.KEYWORD, "abstract");//$NON-NLS-1$
		}
		if(Modifier.isFinal(modifiers)) {
			token(ParserTokenType.KEYWORD, "final");//$NON-NLS-1$
		}
		if(Modifier.isSynchronized(modifiers)) {
			token(ParserTokenType.KEYWORD, "synchronized");//$NON-NLS-1$
		}
		if(Modifier.isVolatile(modifiers)) {
			token(ParserTokenType.KEYWORD, "volatile");//$NON-NLS-1$
		}
		if(Modifier.isNative(modifiers)) {
			token(ParserTokenType.KEYWORD, "native");//$NON-NLS-1$
		}
		if(Modifier.isStrictfp(modifiers)) {
			token(ParserTokenType.KEYWORD, "strictfp");//$NON-NLS-1$
		}
		if(Modifier.isTransient(modifiers)) {
			token(ParserTokenType.KEYWORD, "transient");//$NON-NLS-1$
		}
	}

	/**
	 * Appends the text representation of the given modifier flags, followed by
	 * a single space. Used for 3.0 modifiers and annotations.
	 *
	 * @param ext
	 *            the list of modifier and annotation nodes (element type:
	 *            <code>IExtendedModifiers</code>)
	 */
	private void printModifiers(List ext) {
		for(Iterator it = ext.iterator(); it.hasNext();) {
			ASTNode p = (ASTNode)it.next();
			p.accept(this);
		}
	}

	/**
	 * reference node helper function that is common to all the difference
	 * reference nodes.
	 *
	 * @param typeArguments
	 *            list of type arguments
	 */
	private void visitReferenceTypeArguments(List typeArguments) {
		token(ParserTokenType.OPERATOR, "::");//$NON-NLS-1$
		if(!typeArguments.isEmpty()) {
			token(ParserTokenType.BRACKET, "<");
			for(Iterator it = typeArguments.iterator(); it.hasNext();) {
				Type t = (Type)it.next();
				t.accept(this);
				if(it.hasNext()) {
					token(ParserTokenType.OPERATOR, ",");
				}
			}
			token(ParserTokenType.BRACKET, ">");
		}
	}

	private void visitTypeAnnotations(AnnotatableType node) {
		if(node.getAST()
			.apiLevel() >= AST.JLS8) {
			visitAnnotationsList(node.annotations());
		}
	}

	private void visitAnnotationsList(List annotations) {
		for(Iterator it = annotations.iterator(); it.hasNext();) {
			Annotation annotation = (Annotation)it.next();
			annotation.accept(this);
		}
	}

	/**
	 * Internal synonym for {@link TypeDeclaration#superInterfaces()}. Use to
	 * alleviate deprecation warnings.
	 *
	 * @deprecated
	 * @since 3.4
	 */
	@Deprecated
	private List superInterfaces(TypeDeclaration node) {
		return node.superInterfaces();
	}

	/*
	 * @see ASTVisitor#visit(AnnotationTypeDeclaration)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		if(node.getJavadoc() != null) {
			node.getJavadoc()
				.accept(this);
		}

		printModifiers(node.modifiers());
		token(ParserTokenType.KEYWORD, "@interface");//$NON-NLS-1$
		node.getName()
			.accept(this);
		token(ParserTokenType.BRACKET, "{");//$NON-NLS-1$
		for(Iterator it = node.bodyDeclarations()
			.iterator(); it.hasNext();) {
			BodyDeclaration d = (BodyDeclaration)it.next();
			d.accept(this);
		}
		token(ParserTokenType.BRACKET, "}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AnnotationTypeMemberDeclaration)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		if(node.getJavadoc() != null) {
			node.getJavadoc()
				.accept(this);
		}
		printModifiers(node.modifiers());
		node.getType()
			.accept(this);

		node.getName()
			.accept(this);
		token(ParserTokenType.BRACKET, "(");
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		if(node.getDefault() != null) {
			token(ParserTokenType.KEYWORD, "default");//$NON-NLS-1$
			node.getDefault()
				.accept(this);
		}
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AnonymousClassDeclaration)
	 */
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		token(ParserTokenType.BRACKET, "{");//$NON-NLS-1$
		for(Iterator it = node.bodyDeclarations()
			.iterator(); it.hasNext();) {
			BodyDeclaration b = (BodyDeclaration)it.next();
			b.accept(this);
		}
		token(ParserTokenType.BRACKET, "}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayAccess)
	 */
	@Override
	public boolean visit(ArrayAccess node) {
		node.getArray()
			.accept(this);
		token(ParserTokenType.BRACKET, "[");//$NON-NLS-1$
		node.getIndex()
			.accept(this);
		token(ParserTokenType.BRACKET, "]");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayCreation)
	 */
	@Override
	public boolean visit(ArrayCreation node) {
		token(ParserTokenType.KEYWORD, "new");//$NON-NLS-1$
		ArrayType at = node.getType();
		int dims = at.getDimensions();
		Type elementType = at.getElementType();
		elementType.accept(this);
		for(Iterator it = node.dimensions()
			.iterator(); it.hasNext();) {
			token(ParserTokenType.BRACKET, "[");//$NON-NLS-1$
			Expression e = (Expression)it.next();
			e.accept(this);
			token(ParserTokenType.BRACKET, "]");//$NON-NLS-1$
			dims--;
		}
		// add empty "[]" for each extra array dimension
		for(int i = 0; i < dims; i++) {
			token(ParserTokenType.BRACKET, "[");
			token(ParserTokenType.BRACKET, "]");//$NON-NLS-1$
		}
		if(node.getInitializer() != null) {
			node.getInitializer()
				.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayInitializer)
	 */
	@Override
	public boolean visit(ArrayInitializer node) {
		token(ParserTokenType.BRACKET, "{");//$NON-NLS-1$
		for(Iterator it = node.expressions()
			.iterator(); it.hasNext();) {
			Expression e = (Expression)it.next();
			e.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.BRACKET, "}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayType)
	 */
	@Override
	public boolean visit(ArrayType node) {
		if(node.getAST()
			.apiLevel() < AST.JLS8) {
			visitComponentType(node);
			token(ParserTokenType.BRACKET, "[");
			token(ParserTokenType.BRACKET, "]");//$NON-NLS-1$
		} else {
			node.getElementType()
				.accept(this);
			List dimensions = node.dimensions();
			int size = dimensions.size();
			for(int i = 0; i < size; i++) {
				Dimension aDimension = (Dimension)dimensions.get(i);
				aDimension.accept(this);
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AssertStatement)
	 */
	@Override
	public boolean visit(AssertStatement node) {
		token(ParserTokenType.KEYWORD, "assert");//$NON-NLS-1$
		node.getExpression()
			.accept(this);
		if(node.getMessage() != null) {
			token(ParserTokenType.OPERATOR, ":");//$NON-NLS-1$
			node.getMessage()
				.accept(this);
		}
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Assignment)
	 */
	@Override
	public boolean visit(Assignment node) {
		node.getLeftHandSide()
			.accept(this);
		token(ParserTokenType.OPERATOR, node.getOperator()
			.toString());
		node.getRightHandSide()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Block)
	 */
	@Override
	public boolean visit(Block node) {
		token(ParserTokenType.BRACKET, "{");//$NON-NLS-1$
		for(Iterator it = node.statements()
			.iterator(); it.hasNext();) {
			Statement s = (Statement)it.next();
			s.accept(this);
		}
		token(ParserTokenType.BRACKET, "}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BlockComment)
	 *
	 * @since 3.0
	 */
	@Override
	public boolean visit(BlockComment node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BooleanLiteral)
	 */
	@Override
	public boolean visit(BooleanLiteral node) {
		if(node.booleanValue() == true) {
			token(ParserTokenType.LITERAL, "true");//$NON-NLS-1$
		} else {
			token(ParserTokenType.LITERAL, "false");//$NON-NLS-1$
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BreakStatement)
	 */
	@Override
	public boolean visit(BreakStatement node) {
		token(ParserTokenType.KEYWORD, "break");//$NON-NLS-1$
		if(node.getLabel() != null) {
			node.getLabel()
				.accept(this);
		}
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CastExpression)
	 */
	@Override
	public boolean visit(CastExpression node) {
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		node.getType()
			.accept(this);
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		node.getExpression()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CatchClause)
	 */
	@Override
	public boolean visit(CatchClause node) {
		token(ParserTokenType.KEYWORD, "catch");
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		node.getException()
			.accept(this);
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		node.getBody()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CharacterLiteral)
	 */
	@Override
	public boolean visit(CharacterLiteral node) {
		token(ParserTokenType.LITERAL, node.getEscapedValue());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ClassInstanceCreation)
	 */
	@Override
	public boolean visit(ClassInstanceCreation node) {
		if(node.getExpression() != null) {
			node.getExpression()
				.accept(this);
			token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		}
		token(ParserTokenType.KEYWORD, "new");//$NON-NLS-1$
		if(node.getAST()
			.apiLevel() == AST.JLS2) {
			this.isTypeName = true;
			getName(node).accept(this);
			this.isTypeName = false;
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(!node.typeArguments()
				.isEmpty()) {
				token(ParserTokenType.BRACKET, "<");//$NON-NLS-1$
				for(Iterator it = node.typeArguments()
					.iterator(); it.hasNext();) {
					Type t = (Type)it.next();
					t.accept(this);
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
				token(ParserTokenType.BRACKET, ">");//$NON-NLS-1$
			}
			node.getType()
				.accept(this);
		}
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		for(Iterator it = node.arguments()
			.iterator(); it.hasNext();) {
			Expression e = (Expression)it.next();
			e.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		if(node.getAnonymousClassDeclaration() != null) {
			node.getAnonymousClassDeclaration()
				.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	@Override
	public boolean visit(CompilationUnit node) {
		if(node.getPackage() != null) {
			node.getPackage()
				.accept(this);
		}
		for(Iterator it = node.imports()
			.iterator(); it.hasNext();) {
			ImportDeclaration d = (ImportDeclaration)it.next();
			d.accept(this);
		}
		for(Iterator it = node.types()
			.iterator(); it.hasNext();) {
			AbstractTypeDeclaration d = (AbstractTypeDeclaration)it.next();
			d.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConditionalExpression)
	 */
	@Override
	public boolean visit(ConditionalExpression node) {
		node.getExpression()
			.accept(this);
		token(ParserTokenType.OPERATOR, "?");//$NON-NLS-1$
		node.getThenExpression()
			.accept(this);
		token(ParserTokenType.OPERATOR, ":");//$NON-NLS-1$
		node.getElseExpression()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConstructorInvocation)
	 */
	@Override
	public boolean visit(ConstructorInvocation node) {
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(!node.typeArguments()
				.isEmpty()) {
				token(ParserTokenType.BRACKET, "<");//$NON-NLS-1$
				for(Iterator it = node.typeArguments()
					.iterator(); it.hasNext();) {
					Type t = (Type)it.next();
					t.accept(this);
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
				token(ParserTokenType.BRACKET, ">");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.KEYWORD, "this");
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		for(Iterator it = node.arguments()
			.iterator(); it.hasNext();) {
			Expression e = (Expression)it.next();
			e.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.BRACKET, ")");
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ContinueStatement)
	 */
	@Override
	public boolean visit(ContinueStatement node) {
		token(ParserTokenType.KEYWORD, "continue");//$NON-NLS-1$
		if(node.getLabel() != null) {
			node.getLabel()
				.accept(this);
		}
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CreationReference)
	 *
	 * @since 3.10
	 */
	@Override
	public boolean visit(CreationReference node) {
		node.getType()
			.accept(this);
		visitReferenceTypeArguments(node.typeArguments());
		token(ParserTokenType.KEYWORD, "new");//$NON-NLS-1$
		return false;
	}

	@Override
	public boolean visit(Dimension node) {
		List annotations = node.annotations();
		visitAnnotationsList(annotations);
		token(ParserTokenType.BRACKET, "[");
		token(ParserTokenType.BRACKET, "]"); //$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(DoStatement)
	 */
	@Override
	public boolean visit(DoStatement node) {
		token(ParserTokenType.KEYWORD, "do");//$NON-NLS-1$
		node.getBody()
			.accept(this);
		token(ParserTokenType.KEYWORD, "while");
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		node.getExpression()
			.accept(this);
		token(ParserTokenType.BRACKET, ")");
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EmptyStatement)
	 */
	@Override
	public boolean visit(EmptyStatement node) {
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnhancedForStatement)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(EnhancedForStatement node) {
		token(ParserTokenType.KEYWORD, "for");
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		node.getParameter()
			.accept(this);
		token(ParserTokenType.OPERATOR, ":");//$NON-NLS-1$
		node.getExpression()
			.accept(this);
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		node.getBody()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnumConstantDeclaration)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(EnumConstantDeclaration node) {
		if(node.getJavadoc() != null) {
			node.getJavadoc()
				.accept(this);
		}
		printModifiers(node.modifiers());
		node.getName()
			.accept(this);
		if(!node.arguments()
			.isEmpty()) {
			token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
			for(Iterator it = node.arguments()
				.iterator(); it.hasNext();) {
				Expression e = (Expression)it.next();
				e.accept(this);
				if(it.hasNext()) {
					token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
				}
			}
			token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		}
		if(node.getAnonymousClassDeclaration() != null) {
			node.getAnonymousClassDeclaration()
				.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnumDeclaration)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(EnumDeclaration node) {
		if(node.getJavadoc() != null) {
			node.getJavadoc()
				.accept(this);
		}
		printModifiers(node.modifiers());
		token(ParserTokenType.KEYWORD, "enum");//$NON-NLS-1$
		this.isTypeName = true;
		node.getName()
			.accept(this);
		this.isTypeName = true;
		if(!node.superInterfaceTypes()
			.isEmpty()) {
			token(ParserTokenType.KEYWORD, "implements");//$NON-NLS-1$
			for(Iterator it = node.superInterfaceTypes()
				.iterator(); it.hasNext();) {
				Type t = (Type)it.next();
				t.accept(this);
				if(it.hasNext()) {
					token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
				}
			}
		}
		token(ParserTokenType.BRACKET, "{");//$NON-NLS-1$
		for(Iterator it = node.enumConstants()
			.iterator(); it.hasNext();) {
			EnumConstantDeclaration d = (EnumConstantDeclaration)it.next();
			d.accept(this);
			// enum constant declarations do not include punctuation
			if(it.hasNext()) {
				// enum constant declarations are separated by commas
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		if(!node.bodyDeclarations()
			.isEmpty()) {
			token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
			for(Iterator it = node.bodyDeclarations()
				.iterator(); it.hasNext();) {
				BodyDeclaration d = (BodyDeclaration)it.next();
				d.accept(this);
				// other body declarations include trailing punctuation
			}
		}
		token(ParserTokenType.BRACKET, "}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ExpressionMethodReference)
	 *
	 * @since 3.10
	 */
	@Override
	public boolean visit(ExpressionMethodReference node) {
		node.getExpression()
			.accept(this);
		visitReferenceTypeArguments(node.typeArguments());
		node.getName()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ExpressionStatement)
	 */
	@Override
	public boolean visit(ExpressionStatement node) {
		node.getExpression()
			.accept(this);
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldAccess)
	 */
	@Override
	public boolean visit(FieldAccess node) {
		node.getExpression()
			.accept(this);
		token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		node.getName()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldDeclaration)
	 */
	@Override
	public boolean visit(FieldDeclaration node) {
		if(node.getJavadoc() != null) {
			node.getJavadoc()
				.accept(this);
		}
		if(node.getAST()
			.apiLevel() == AST.JLS2) {
			printModifiers(node.getModifiers());
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType()
			.accept(this);
		for(Iterator it = node.fragments()
			.iterator(); it.hasNext();) {
			VariableDeclarationFragment f = (VariableDeclarationFragment)it.next();
			f.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ForStatement)
	 */
	@Override
	public boolean visit(ForStatement node) {
		token(ParserTokenType.KEYWORD, "for");
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		for(Iterator it = node.initializers()
			.iterator(); it.hasNext();) {
			Expression e = (Expression)it.next();
			e.accept(this);
			if(it.hasNext())
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
		}
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		if(node.getExpression() != null) {
			node.getExpression()
				.accept(this);
		}
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		for(Iterator it = node.updaters()
			.iterator(); it.hasNext();) {
			Expression e = (Expression)it.next();
			e.accept(this);
			if(it.hasNext())
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
		}
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		node.getBody()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(IfStatement)
	 */
	@Override
	public boolean visit(IfStatement node) {
		token(ParserTokenType.KEYWORD, "if");
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		node.getExpression()
			.accept(this);
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		node.getThenStatement()
			.accept(this);
		if(node.getElseStatement() != null) {
			token(ParserTokenType.KEYWORD, "else");//$NON-NLS-1$
			node.getElseStatement()
				.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ImportDeclaration)
	 */
	@Override
	public boolean visit(ImportDeclaration node) {
		token(ParserTokenType.KEYWORD, "import");//$NON-NLS-1$
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(node.isStatic()) {
				token(ParserTokenType.KEYWORD, "static");//$NON-NLS-1$
			}
		}
		if(node.isOnDemand()) {
			this.isPackageName = true;
			node.getName()
				.accept(this);
			this.isPackageName = false;
			token(ParserTokenType.OPERATOR, ".");
			token(ParserTokenType.OPERATOR, "*");//$NON-NLS-1$
		} else {
			this.isTypeName = true;
			node.getName()
				.accept(this);
			this.isTypeName = false;
		}
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InfixExpression)
	 */
	@Override
	public boolean visit(InfixExpression node) {
		node.getLeftOperand()
			.accept(this);

		operatorToken(node.getOperator()
			.toString());

		node.getRightOperand()
			.accept(this);
		final List extendedOperands = node.extendedOperands();
		if(extendedOperands.size() != 0) {
			for(Iterator it = extendedOperands.iterator(); it.hasNext();) {
				operatorToken(node.getOperator()
					.toString());
				Expression e = (Expression)it.next();
				e.accept(this);
			}
		}
		return false;
	}

	private void operatorToken(String operator) {
		while(operator.length() >= 2 && operator.charAt(0) == '>') {
			token(ParserTokenType.OPERATOR, ">");
			operator = operator.substring(1);
		}
		token(ParserTokenType.OPERATOR, operator);
	}

	/*
	 * @see ASTVisitor#visit(Initializer)
	 */
	@Override
	public boolean visit(Initializer node) {
		if(node.getJavadoc() != null) {
			node.getJavadoc()
				.accept(this);
		}
		if(node.getAST()
			.apiLevel() == AST.JLS2) {
			printModifiers(node.getModifiers());
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getBody()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InstanceofExpression)
	 */
	@Override
	public boolean visit(InstanceofExpression node) {
		node.getLeftOperand()
			.accept(this);
		token(ParserTokenType.OPERATOR, "instanceof");//$NON-NLS-1$
		node.getRightOperand()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(IntersectionType)
	 *
	 * @since 3.7
	 */
	@Override
	public boolean visit(IntersectionType node) {
		for(Iterator it = node.types()
			.iterator(); it.hasNext();) {
			Type t = (Type)it.next();
			t.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, "&"); //$NON-NLS-1$
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Javadoc)
	 */
	@Override
	public boolean visit(Javadoc node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LabeledStatement)
	 */
	@Override
	public boolean visit(LabeledStatement node) {
		node.getLabel()
			.accept(this);
		token(ParserTokenType.OPERATOR, ":");//$NON-NLS-1$
		node.getBody()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LambdaExpression)
	 */
	@Override
	public boolean visit(LambdaExpression node) {
		boolean hasParentheses = node.hasParentheses();
		if(hasParentheses)
			token(ParserTokenType.BRACKET, "(");
		for(Iterator it = node.parameters()
			.iterator(); it.hasNext();) {
			VariableDeclaration v = (VariableDeclaration)it.next();
			v.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		if(hasParentheses)
			token(ParserTokenType.BRACKET, ")");
		token(ParserTokenType.OPERATOR, "->"); //$NON-NLS-1$
		node.getBody()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LineComment)
	 *
	 * @since 3.0
	 */
	@Override
	public boolean visit(LineComment node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MarkerAnnotation)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(MarkerAnnotation node) {
		token(ParserTokenType.OPERATOR, "@");//$NON-NLS-1$
		this.isTypeName = true;
		node.getTypeName()
			.accept(this);
		this.isTypeName = false;
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MemberRef)
	 *
	 * @since 3.0
	 */
	@Override
	public boolean visit(MemberRef node) {
		if(node.getQualifier() != null) {
			this.isTypeName = true;
			node.getQualifier()
				.accept(this);
			this.isTypeName = false;
		}
		token(ParserTokenType.OPERATOR, "#");//$NON-NLS-1$
		node.getName()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MemberValuePair)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(MemberValuePair node) {
		node.getName()
			.accept(this);
		token(ParserTokenType.OPERATOR, "=");//$NON-NLS-1$
		node.getValue()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		if(node.getJavadoc() != null) {
			node.getJavadoc()
				.accept(this);
		}
		if(node.getAST()
			.apiLevel() == AST.JLS2) {
			printModifiers(node.getModifiers());
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
			if(!node.typeParameters()
				.isEmpty()) {
				token(ParserTokenType.BRACKET, "<");//$NON-NLS-1$
				for(Iterator it = node.typeParameters()
					.iterator(); it.hasNext();) {
					TypeParameter t = (TypeParameter)it.next();
					t.accept(this);
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
				token(ParserTokenType.BRACKET, ">");//$NON-NLS-1$
			}
		}
		if(!node.isConstructor()) {
			if(node.getAST()
				.apiLevel() == AST.JLS2) {
				getReturnType(node).accept(this);
			} else {
				if(node.getReturnType2() != null) {
					node.getReturnType2()
						.accept(this);
				} else {
					// methods really ought to have a return type
					token(ParserTokenType.KEYWORD, "void");//$NON-NLS-1$
				}
			}
		}
		node.getName()
			.accept(this);
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		if(node.getAST()
			.apiLevel() >= AST.JLS8) {
			Type receiverType = node.getReceiverType();
			if(receiverType != null) {
				receiverType.accept(this);
				SimpleName qualifier = node.getReceiverQualifier();
				if(qualifier != null) {
					this.isTypeName = true;
					qualifier.accept(this);
					this.isTypeName = false;
					token(ParserTokenType.OPERATOR, ".");
				}
				token(ParserTokenType.KEYWORD, "this"); //$NON-NLS-1$
				if(node.parameters()
					.size() > 0) {
					token(ParserTokenType.OPERATOR, ",");
				}
			}
		}
		for(Iterator it = node.parameters()
			.iterator(); it.hasNext();) {
			SingleVariableDeclaration v = (SingleVariableDeclaration)it.next();
			v.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		int size = node.getExtraDimensions();
		if(node.getAST()
			.apiLevel() >= AST.JLS8) {
			List dimensions = node.extraDimensions();
			for(int i = 0; i < size; i++) {
				visit((Dimension)dimensions.get(i));
			}
		} else {
			for(int i = 0; i < size; i++) {
				token(ParserTokenType.BRACKET, "[");
				token(ParserTokenType.BRACKET, "]"); //$NON-NLS-1$
			}
		}
		if(node.getAST()
			.apiLevel() < AST.JLS8) {
			if(!thrownExceptions(node).isEmpty()) {
				token(ParserTokenType.KEYWORD, "throws");//$NON-NLS-1$
				for(Iterator it = thrownExceptions(node).iterator(); it.hasNext();) {
					Name n = (Name)it.next();
					this.isTypeName = true;
					n.accept(this);
					this.isTypeName = false;
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
			}
		} else {
			if(!node.thrownExceptionTypes()
				.isEmpty()) {
				token(ParserTokenType.KEYWORD, "throws");//$NON-NLS-1$
				for(Iterator it = node.thrownExceptionTypes()
					.iterator(); it.hasNext();) {
					Type n = (Type)it.next();
					n.accept(this);
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
			}
		}
		if(node.getBody() == null) {
			token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		} else {
			node.getBody()
				.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodInvocation)
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		if(node.getExpression() != null) {
			node.getExpression()
				.accept(this);
			token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(!node.typeArguments()
				.isEmpty()) {
				token(ParserTokenType.BRACKET, "<");//$NON-NLS-1$
				for(Iterator it = node.typeArguments()
					.iterator(); it.hasNext();) {
					Type t = (Type)it.next();
					t.accept(this);
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
				token(ParserTokenType.BRACKET, ">");//$NON-NLS-1$
			}
		}
		node.getName()
			.accept(this);
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		for(Iterator it = node.arguments()
			.iterator(); it.hasNext();) {
			Expression e = (Expression)it.next();
			e.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodRef)
	 *
	 * @since 3.0
	 */
	@Override
	public boolean visit(MethodRef node) {
		if(node.getQualifier() != null) {
			this.isTypeName = true;
			node.getQualifier()
				.accept(this);
			this.isTypeName = false;
		}
		token(ParserTokenType.OPERATOR, "#");//$NON-NLS-1$
		node.getName()
			.accept(this);
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		for(Iterator it = node.parameters()
			.iterator(); it.hasNext();) {
			MethodRefParameter e = (MethodRefParameter)it.next();
			e.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodRefParameter)
	 *
	 * @since 3.0
	 */
	@Override
	public boolean visit(MethodRefParameter node) {
		node.getType()
			.accept(this);
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(node.isVarargs()) {
				token(ParserTokenType.OPERATOR, "...");//$NON-NLS-1$
			}
		}
		if(node.getName() != null) {
			node.getName()
				.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Modifier)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(Modifier node) {
		token(ParserTokenType.KEYWORD, node.getKeyword()
			.toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NameQualifiedType)
	 *
	 * @since 3.10
	 */
	@Override
	public boolean visit(NameQualifiedType node) {
		this.qulifyNest++;
		this.isTypeName = true;
		node.getQualifier()
			.accept(this);
		token(ParserTokenType.OPERATOR, ".");
		visitTypeAnnotations(node);
		node.getName()
			.accept(this);
		this.isTypeName = false;
		this.qulifyNest--;
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NormalAnnotation)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(NormalAnnotation node) {
		token(ParserTokenType.OPERATOR, "@");//$NON-NLS-1$
		this.isTypeName = true;
		node.getTypeName()
			.accept(this);
		this.isTypeName = false;
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		for(Iterator it = node.values()
			.iterator(); it.hasNext();) {
			MemberValuePair p = (MemberValuePair)it.next();
			p.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NullLiteral)
	 */
	@Override
	public boolean visit(NullLiteral node) {
		token(ParserTokenType.LITERAL, "null");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NumberLiteral)
	 */
	@Override
	public boolean visit(NumberLiteral node) {
		token(ParserTokenType.LITERAL, node.getToken());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PackageDeclaration)
	 */
	@Override
	public boolean visit(PackageDeclaration node) {
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(node.getJavadoc() != null) {
				node.getJavadoc()
					.accept(this);
			}
			for(Iterator it = node.annotations()
				.iterator(); it.hasNext();) {
				Annotation p = (Annotation)it.next();
				p.accept(this);
			}
		}
		token(ParserTokenType.KEYWORD, "package");//$NON-NLS-1$
		this.isPackageName = true;
		node.getName()
			.accept(this);
		this.isPackageName = false;
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParameterizedType)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(ParameterizedType node) {
		node.getType()
			.accept(this);
		token(ParserTokenType.BRACKET, "<");//$NON-NLS-1$
		for(Iterator it = node.typeArguments()
			.iterator(); it.hasNext();) {
			Type t = (Type)it.next();
			t.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.BRACKET, ">");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParenthesizedExpression)
	 */
	@Override
	public boolean visit(ParenthesizedExpression node) {
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		node.getExpression()
			.accept(this);
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PostfixExpression)
	 */
	@Override
	public boolean visit(PostfixExpression node) {
		node.getOperand()
			.accept(this);
		token(ParserTokenType.OPERATOR, node.getOperator()
			.toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrefixExpression)
	 */
	@Override
	public boolean visit(PrefixExpression node) {
		token(ParserTokenType.OPERATOR, node.getOperator()
			.toString());
		node.getOperand()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrimitiveType)
	 */
	@Override
	public boolean visit(PrimitiveType node) {
		visitTypeAnnotations(node);
		token(ParserTokenType.TYPE_IDENTIFIER, node.getPrimitiveTypeCode()
			.toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedName)
	 */
	@Override
	public boolean visit(QualifiedName node) {
		this.qulifyNest++;
		node.getQualifier()
			.accept(this);
		token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		node.getName()
			.accept(this);
		this.qulifyNest--;
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedType)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(QualifiedType node) {
		this.qulifyNest++;
		node.getQualifier()
			.accept(this);
		token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		visitTypeAnnotations(node);
		node.getName()
			.accept(this);
		this.qulifyNest--;
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ReturnStatement)
	 */
	@Override
	public boolean visit(ReturnStatement node) {
		token(ParserTokenType.KEYWORD, "return");//$NON-NLS-1$
		if(node.getExpression() != null) {
			node.getExpression()
				.accept(this);
		}
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleName)
	 */
	@Override
	public boolean visit(SimpleName node) {
		if(this.isPackageName || this.qulifyNest > 1) {
			token(ParserTokenType.QUALIFY_IDENTIFIER, node.getIdentifier());
		} else if(this.isTypeName) {
			token(ParserTokenType.TYPE_IDENTIFIER, node.getIdentifier());
		} else {
			token(ParserTokenType.VARIABLE_IDENTIFIER, node.getIdentifier());
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleType)
	 */
	@Override
	public boolean visit(SimpleType node) {
		visitTypeAnnotations(node);
		this.isTypeName = true;
		node.getName()
			.accept(this);
		this.isTypeName = false;
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SingleMemberAnnotation)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(SingleMemberAnnotation node) {
		token(ParserTokenType.OPERATOR, "@");//$NON-NLS-1$
		this.isTypeName = true;
		node.getTypeName()
			.accept(this);
		this.isTypeName = false;
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		node.getValue()
			.accept(this);
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SingleVariableDeclaration)
	 */
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		if(node.getAST()
			.apiLevel() == AST.JLS2) {
			printModifiers(node.getModifiers());
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType()
			.accept(this);
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(node.isVarargs()) {
				if(node.getAST()
					.apiLevel() >= AST.JLS8) {
					List annotations = node.varargsAnnotations();

					visitAnnotationsList(annotations);
				}
				token(ParserTokenType.OPERATOR, "...");//$NON-NLS-1$
			}
		}
		node.getName()
			.accept(this);
		int size = node.getExtraDimensions();
		if(node.getAST()
			.apiLevel() >= AST.JLS8) {
			List dimensions = node.extraDimensions();
			for(int i = 0; i < size; i++) {
				visit((Dimension)dimensions.get(i));
			}
		} else {
			for(int i = 0; i < size; i++) {
				token(ParserTokenType.BRACKET, "[");
				token(ParserTokenType.BRACKET, "]"); //$NON-NLS-1$
			}
		}
		if(node.getInitializer() != null) {
			token(ParserTokenType.OPERATOR, "=");//$NON-NLS-1$
			node.getInitializer()
				.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(StringLiteral)
	 */
	@Override
	public boolean visit(StringLiteral node) {
		token(ParserTokenType.LITERAL, node.getEscapedValue());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperConstructorInvocation)
	 */
	@Override
	public boolean visit(SuperConstructorInvocation node) {

		if(node.getExpression() != null) {
			node.getExpression()
				.accept(this);
			token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(!node.typeArguments()
				.isEmpty()) {
				token(ParserTokenType.BRACKET, "<");//$NON-NLS-1$
				for(Iterator it = node.typeArguments()
					.iterator(); it.hasNext();) {
					Type t = (Type)it.next();
					t.accept(this);
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
				token(ParserTokenType.BRACKET, ">");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.KEYWORD, "super");
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		for(Iterator it = node.arguments()
			.iterator(); it.hasNext();) {
			Expression e = (Expression)it.next();
			e.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		token(ParserTokenType.OPERATOR, ";");
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperFieldAccess)
	 */
	@Override
	public boolean visit(SuperFieldAccess node) {
		if(node.getQualifier() != null) {
			this.isTypeName = true;
			node.getQualifier()
				.accept(this);
			this.isTypeName = false;
			token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		}
		token(ParserTokenType.KEYWORD, "super");
		token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		node.getName()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperMethodInvocation)
	 */
	@Override
	public boolean visit(SuperMethodInvocation node) {
		if(node.getQualifier() != null) {
			this.isTypeName = true;
			node.getQualifier()
				.accept(this);
			this.isTypeName = false;
			token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		}
		token(ParserTokenType.KEYWORD, "super");
		token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(!node.typeArguments()
				.isEmpty()) {
				token(ParserTokenType.BRACKET, "<");//$NON-NLS-1$
				for(Iterator it = node.typeArguments()
					.iterator(); it.hasNext();) {
					Type t = (Type)it.next();
					t.accept(this);
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
				token(ParserTokenType.BRACKET, ">");//$NON-NLS-1$
			}
		}
		node.getName()
			.accept(this);
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		for(Iterator it = node.arguments()
			.iterator(); it.hasNext();) {
			Expression e = (Expression)it.next();
			e.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperMethodReference)
	 *
	 * @since 3.10
	 */
	@Override
	public boolean visit(SuperMethodReference node) {
		if(node.getQualifier() != null) {
			this.isTypeName = true;
			node.getQualifier()
				.accept(this);
			this.isTypeName = false;
			token(ParserTokenType.OPERATOR, ".");
		}
		token(ParserTokenType.KEYWORD, "super");//$NON-NLS-1$
		visitReferenceTypeArguments(node.typeArguments());
		node.getName()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchCase)
	 */
	@Override
	public boolean visit(SwitchCase node) {
		if(node.isDefault()) {
			token(ParserTokenType.KEYWORD, "default");
			token(ParserTokenType.OPERATOR, ":");//$NON-NLS-1$
		} else {
			token(ParserTokenType.KEYWORD, "case");//$NON-NLS-1$
			node.getExpression()
				.accept(this);
			token(ParserTokenType.OPERATOR, ":");//$NON-NLS-1$
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchStatement)
	 */
	@Override
	public boolean visit(SwitchStatement node) {
		token(ParserTokenType.KEYWORD, "switch");
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		node.getExpression()
			.accept(this);
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		token(ParserTokenType.BRACKET, "{");//$NON-NLS-1$

		for(Iterator it = node.statements()
			.iterator(); it.hasNext();) {
			Statement s = (Statement)it.next();
			s.accept(this);

		}
		token(ParserTokenType.BRACKET, "}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SynchronizedStatement)
	 */
	@Override
	public boolean visit(SynchronizedStatement node) {
		token(ParserTokenType.KEYWORD, "synchronized");
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		node.getExpression()
			.accept(this);
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		node.getBody()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TagElement)
	 *
	 * @since 3.0
	 */
	@Override
	public boolean visit(TagElement node) {
		if(node.isNested()) {
			// nested tags are always enclosed in braces
			token(ParserTokenType.BRACKET, "{");//$NON-NLS-1$
		} else {
			// top-level tags always begin on a new line
			token(ParserTokenType.OPERATOR, "*");//$NON-NLS-1$
		}
		//boolean previousRequiresWhiteSpace = false;
		if(node.getTagName() != null) {
			token(ParserTokenType.VARIABLE_IDENTIFIER, node.getTagName());
			//previousRequiresWhiteSpace = true;
		}
		boolean previousRequiresNewLine = false;
		for(Iterator it = node.fragments()
			.iterator(); it.hasNext();) {
			ASTNode e = (ASTNode)it.next();
			// Name, MemberRef, MethodRef, and nested TagElement do not include
			// white space.
			// TextElements don't always include whitespace, see
			// <https://bugs.eclipse.org/206518>.
			boolean currentIncludesWhiteSpace = false;
			if(e instanceof TextElement) {
				String text = ((TextElement)e).getText();
				if(text.length() > 0 && ScannerHelper.isWhitespace(text.charAt(0))) {
					currentIncludesWhiteSpace = true; // workaround for
														// https://bugs.eclipse.org/403735
				}
			}
			if(previousRequiresNewLine && currentIncludesWhiteSpace) {
				token(ParserTokenType.OPERATOR, "*");//$NON-NLS-1$
			}
			previousRequiresNewLine = currentIncludesWhiteSpace;
			// add space if required to separate

			e.accept(this);
			//previousRequiresWhiteSpace = !currentIncludesWhiteSpace && !(e instanceof TagElement);
		}
		if(node.isNested()) {
			token(ParserTokenType.BRACKET, "}");//$NON-NLS-1$
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TextElement)
	 *
	 * @since 3.0
	 */
	@Override
	public boolean visit(TextElement node) {
		token(ParserTokenType.LITERAL, node.getText());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThisExpression)
	 */
	@Override
	public boolean visit(ThisExpression node) {
		if(node.getQualifier() != null) {
			this.isTypeName = true;
			node.getQualifier()
				.accept(this);
			this.isTypeName = false;
			token(ParserTokenType.OPERATOR, ".");//$NON-NLS-1$
		}
		token(ParserTokenType.KEYWORD, "this");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThrowStatement)
	 */
	@Override
	public boolean visit(ThrowStatement node) {

		token(ParserTokenType.KEYWORD, "throw");//$NON-NLS-1$
		node.getExpression()
			.accept(this);
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TryStatement)
	 */
	@Override
	public boolean visit(TryStatement node) {
		token(ParserTokenType.KEYWORD, "try");//$NON-NLS-1$
		if(node.getAST()
			.apiLevel() >= AST.JLS4) {
			List resources = node.resources();
			if(!resources.isEmpty()) {
				token(ParserTokenType.BRACKET, "(");
				for(Iterator it = resources.iterator(); it.hasNext();) {
					VariableDeclarationExpression variable = (VariableDeclarationExpression)it.next();
					variable.accept(this);
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ";");
					}
				}
				token(ParserTokenType.BRACKET, ")");
			}
		}
		node.getBody()
			.accept(this);
		for(Iterator it = node.catchClauses()
			.iterator(); it.hasNext();) {
			CatchClause cc = (CatchClause)it.next();
			cc.accept(this);
		}
		if(node.getFinally() != null) {
			token(ParserTokenType.KEYWORD, "finally");//$NON-NLS-1$
			node.getFinally()
				.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclaration)
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		if(node.getJavadoc() != null) {
			node.getJavadoc()
				.accept(this);
		}
		if(node.getAST()
			.apiLevel() == AST.JLS2) {
			printModifiers(node.getModifiers());
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		token(ParserTokenType.KEYWORD, node.isInterface() ? "interface" : "class");//$NON-NLS-2$//$NON-NLS-1$
		this.isTypeName = true;
		node.getName()
			.accept(this);
		this.isTypeName = false;
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(!node.typeParameters()
				.isEmpty()) {
				token(ParserTokenType.BRACKET, "<");//$NON-NLS-1$
				for(Iterator it = node.typeParameters()
					.iterator(); it.hasNext();) {
					TypeParameter t = (TypeParameter)it.next();
					t.accept(this);
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
				token(ParserTokenType.BRACKET, ">");//$NON-NLS-1$
			}
		}

		if(node.getAST()
			.apiLevel() == AST.JLS2) {
			if(getSuperclass(node) != null) {
				token(ParserTokenType.KEYWORD, "extends");//$NON-NLS-1$
				getSuperclass(node).accept(this);
			}
			if(!superInterfaces(node).isEmpty()) {
				token(ParserTokenType.KEYWORD, node.isInterface() ? "extends" : "implements");//$NON-NLS-2$//$NON-NLS-1$
				for(Iterator it = superInterfaces(node).iterator(); it.hasNext();) {
					this.isTypeName = true;
					Name n = (Name)it.next();
					n.accept(this);
					this.isTypeName = false;
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
			}
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			if(node.getSuperclassType() != null) {
				token(ParserTokenType.KEYWORD, "extends");//$NON-NLS-1$
				node.getSuperclassType()
					.accept(this);
			}
			if(!node.superInterfaceTypes()
				.isEmpty()) {
				token(ParserTokenType.KEYWORD, node.isInterface() ? "extends" : "implements");//$NON-NLS-2$//$NON-NLS-1$
				for(Iterator it = node.superInterfaceTypes()
					.iterator(); it.hasNext();) {
					Type t = (Type)it.next();
					t.accept(this);
					if(it.hasNext()) {
						token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
					}
				}
			}
		}
		token(ParserTokenType.BRACKET, "{");
		for(Iterator it = node.bodyDeclarations()
			.iterator(); it.hasNext();) {
			BodyDeclaration d = (BodyDeclaration)it.next();
			d.accept(this);
		}

		token(ParserTokenType.BRACKET, "}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclarationStatement)
	 */
	@Override
	public boolean visit(TypeDeclarationStatement node) {
		if(node.getAST()
			.apiLevel() == AST.JLS2) {
			getTypeDeclaration(node).accept(this);
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			node.getDeclaration()
				.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeLiteral)
	 */
	@Override
	public boolean visit(TypeLiteral node) {
		node.getType()
			.accept(this);
		token(ParserTokenType.OPERATOR, ".");
		token(ParserTokenType.KEYWORD, "class");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeMethodReference)
	 *
	 * @since 3.10
	 */
	@Override
	public boolean visit(TypeMethodReference node) {
		node.getType()
			.accept(this);
		visitReferenceTypeArguments(node.typeArguments());
		node.getName()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeParameter)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(TypeParameter node) {
		if(node.getAST()
			.apiLevel() >= AST.JLS8) {
			printModifiers(node.modifiers());
		}
		node.getName()
			.accept(this);
		if(!node.typeBounds()
			.isEmpty()) {
			token(ParserTokenType.KEYWORD, "extends");//$NON-NLS-1$
			for(Iterator it = node.typeBounds()
				.iterator(); it.hasNext();) {
				Type t = (Type)it.next();
				t.accept(this);
				if(it.hasNext()) {
					token(ParserTokenType.OPERATOR, "&");//$NON-NLS-1$
				}
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(UnionType)
	 *
	 * @since 3.7
	 */
	@Override
	public boolean visit(UnionType node) {
		for(Iterator it = node.types()
			.iterator(); it.hasNext();) {
			Type t = (Type)it.next();
			t.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, "|");
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationExpression)
	 */
	@Override
	public boolean visit(VariableDeclarationExpression node) {
		if(node.getAST()
			.apiLevel() == AST.JLS2) {
			printModifiers(node.getModifiers());
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType()
			.accept(this);
		for(Iterator it = node.fragments()
			.iterator(); it.hasNext();) {
			VariableDeclarationFragment f = (VariableDeclarationFragment)it.next();
			f.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationFragment)
	 */
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		node.getName()
			.accept(this);
		int size = node.getExtraDimensions();
		if(node.getAST()
			.apiLevel() >= AST.JLS8) {
			List dimensions = node.extraDimensions();
			for(int i = 0; i < size; i++) {
				visit((Dimension)dimensions.get(i));
			}
		} else {
			for(int i = 0; i < size; i++) {
				token(ParserTokenType.BRACKET, "[");
				token(ParserTokenType.BRACKET, "]");//$NON-NLS-1$
			}
		}
		if(node.getInitializer() != null) {
			token(ParserTokenType.OPERATOR, "=");//$NON-NLS-1$
			node.getInitializer()
				.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationStatement)
	 */
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		if(node.getAST()
			.apiLevel() == AST.JLS2) {
			printModifiers(node.getModifiers());
		}
		if(node.getAST()
			.apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType()
			.accept(this);
		for(Iterator it = node.fragments()
			.iterator(); it.hasNext();) {
			VariableDeclarationFragment f = (VariableDeclarationFragment)it.next();
			f.accept(this);
			if(it.hasNext()) {
				token(ParserTokenType.OPERATOR, ",");//$NON-NLS-1$
			}
		}
		token(ParserTokenType.OPERATOR, ";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WhileStatement)
	 */
	@Override
	public boolean visit(WhileStatement node) {
		token(ParserTokenType.KEYWORD, "while");
		token(ParserTokenType.BRACKET, "(");//$NON-NLS-1$
		node.getExpression()
			.accept(this);
		token(ParserTokenType.BRACKET, ")");//$NON-NLS-1$
		node.getBody()
			.accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WildcardType)
	 *
	 * @since 3.1
	 */
	@Override
	public boolean visit(WildcardType node) {
		visitTypeAnnotations(node);
		token(ParserTokenType.OPERATOR, "?");//$NON-NLS-1$
		Type bound = node.getBound();
		if(bound != null) {
			if(node.isUpperBound()) {
				token(ParserTokenType.KEYWORD, "extends");//$NON-NLS-1$
			} else {
				token(ParserTokenType.KEYWORD, "super");//$NON-NLS-1$
			}
			bound.accept(this);
		}
		return false;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	private void visitComponentType(ArrayType node) {
		node.getComponentType()
			.accept(this);
	}

}
