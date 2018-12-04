package jp.ac.osaka_u.ist.sdl.rminer.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.Commit;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitRepository;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Document;
import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCodeFile;
import jp.ac.osaka_u.ist.sdl.rminer.parser.SourceCodeGitFile;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Token;
import jp.ac.osaka_u.ist.sdl.rminer.parser.TokenizedCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.java.JavaParser;

public class CUIController {

	@Argument(index = 0, required = true, usage = "Path to target directory.")
	public String directory;

	@Argument(index = 1, usage = "If set, tokenize files at the commit.")
	public String commitId;

	@Option(name = "--output", usage = "Path to output file. If unset, output result to standard output.")
	public String output;

	@Option(name = "--unit", usage = "document unit (method, class or file)")
	public String unit = "method";

	@Option(name = "--normalize", usage = "'none' or 'identifier'")
	public String normalize = "none";

	public void run() throws IOException {
		if(commitId == null) {
			generateDocument(directory, unit, output, normalize);
		} else {
			generateFromGitRepository(directory, commitId, unit, output, normalize);
		}
	}

	public static void generateFromGitRepository(String directory, String commitId, String unit, String output, String normalize) throws IOException {
		GitRepository repository = new GitRepository(Paths.get(directory));
		Commit commit = repository.getCommit(commitId);
		JavaParser parser = new JavaParser();
		Map<String, String> params = new HashMap<>();
		params.put("unit", unit);
		params.put("identifier", normalize);
		parser.initialize(params);

		try(PrintWriter out = getWriter(output)) {
			commit.retrieveFiles()
				.stream()
				.filter(v -> v.getPath()
					.endsWith(".java"))
				.map(SourceCodeGitFile::new)
				.map(parser::tokenize)
				.flatMap(TokenizedCode::stream)
				.map(CUIController::documentToString)
				.forEach(out::println);
		}
	}

	public static void generateDocument(String directory, String unit, String output, String normalize) throws IOException {

		JavaParser parser = new JavaParser();
		Map<String, String> params = new HashMap<>();
		params.put("unit", unit);
		params.put("identifier", normalize);
		parser.initialize(params);

		try(PrintWriter out = getWriter(output)) {
			Files.walk(Paths.get(directory))
				.filter(v -> v.toString()
					.endsWith(".java"))
				.map(SourceCodeFile::new)
				.map(parser::tokenize)
				.flatMap(TokenizedCode::stream)
				.map(CUIController::documentToString)
				.forEach(out::println);
		}
	}

	private static PrintWriter getWriter(String filename) throws IOException {
		if(filename == null) {
			return new PrintWriter(System.out);
		} else {
			return new PrintWriter(Files.newBufferedWriter(Paths.get(filename)));
		}
	}

	private static String documentToString(Document doc) {
		return doc.stream()
			.map(Token::getSurface)
			.collect(Collectors.joining(" "));
	}

	public static void main(String[] args) throws Exception {
		CUIController controller = new CUIController();
		CmdLineParser parser = new CmdLineParser(controller);
		try {
			parser.parseArgument(args);
		} catch(CmdLineException e) {
			parser.printUsage(System.err);
			System.exit(1);
		}
		controller.run();
	}
}
