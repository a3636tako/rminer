package jp.ac.osaka_u.ist.sdl.naturalness.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jp.ac.osaka_u.ist.sdl.naturalness.component.Dictionary;
import jp.ac.osaka_u.ist.sdl.naturalness.component.IndexedDocumentCollection;
import jp.ac.osaka_u.ist.sdl.naturalness.component.LanguageModel;
import jp.ac.osaka_u.ist.sdl.naturalness.component.Project;
import jp.ac.osaka_u.ist.sdl.naturalness.component.ProjectFile;
import jp.ac.osaka_u.ist.sdl.naturalness.component.TokenizedProject;
import jp.ac.osaka_u.ist.sdl.naturalness.component.impl.HashMapDictionary;
import jp.ac.osaka_u.ist.sdl.naturalness.component.impl.KenLM;
import jp.ac.osaka_u.ist.sdl.naturalness.component.impl.KenLMEstimator;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.Commit;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitRepository;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Document;
import jp.ac.osaka_u.ist.sdl.rminer.parser.TokenizedCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.java.AbstractDocumentGenerator;
import jp.ac.osaka_u.ist.sdl.rminer.parser.java.JavaParser;

public class CUIController {

	@Argument(index = 0, required = true, usage = "'estimate' or 'query'.")
	public String command;

	@Argument(index = 1, required = true, usage = "Path to target directory.")
	public String directory;

	@Argument(index = 2, usage = "If set, tokenize files at the commit.")
	public String commitId;

	@Option(name = "--unit", usage = "document unit (method, class or file)")
	public String unit = "method";

	@Option(name = "--normalize", usage = "'none' or 'identifier' or 'split'")
	public String normalize = "none";

	@Option(name = "--kenlm", usage = "path to kenlm binary")
	public String binPath = "";

	@Option(name = "--model", usage = "path to language model file")
	public String lmpath = "arpa.txt";

	@Option(name = "--dict", required = true, usage = "path to dictionary file")
	public String dictionary;

	@Option(name = "-o", usage = "order of language model")
	public int order = 3;

	@Option(name = "--detail", usage = "If set, output details.")
	public String detail;

	public void run() throws Exception {
		switch(command){
		case "estimate":
			estimateModel(directory, dictionary, commitId, unit, binPath, order, lmpath, normalize);
			return;
		case "query":
			calcNaturalness(directory, directory, commitId, unit, binPath, lmpath, normalize, detail);
			return;
		default:
			throw new RuntimeException();
		}
	}

	public static void estimateModel(String directory, String dictionaryPath, String commitId, String unit, String kenlmDir, int order, String model, String normalize) throws IOException {
		Dictionary dictionary = new HashMapDictionary();
		Path dpath = Paths.get(dictionaryPath);
		if(Files.exists(dpath)) {
			dictionary.load(dpath);
		}

		JavaParser parser = new JavaParser();
		Map<String, String> params = new HashMap<>();
		params.put("unit", unit);
		params.put("identifier", normalize);
		parser.initialize(params);

		KenLMEstimator estimator = new KenLMEstimator();
		estimator.setOption("lmplzPath", kenlmDir + "lmplz");
		estimator.setOption("-o", String.valueOf(order));
		estimator.setOption("--arpa", model);

		Project project = retriveFiles(directory, commitId, unit);

		IndexedDocumentCollection index = project.index(parser, dictionary);
		index.estimateModel(estimator, Paths.get(model));

		dictionary.store(dpath);
	}

	public static void calcNaturalness(String directory, String dictionaryPath, String commitId, String unit, String kenlmDir, String modelPath, String normalize, String detail) throws IOException {
		Dictionary dictionary = new HashMapDictionary();
		Path dpath = Paths.get(dictionaryPath);
		if(Files.exists(dpath)) {
			dictionary.load(dpath);
		}

		JavaParser parser = new JavaParser();
		Map<String, String> params = new HashMap<>();
		params.put("unit", unit);
		params.put("identifier", normalize);
		parser.initialize(params);

		LanguageModel model = new KenLM();
		model.initialize(Paths.get(modelPath), Collections.singletonMap("pathQuery", kenlmDir + "query"));

		Project project = retriveFiles(directory, commitId, unit);

		TokenizedProject tokenizedProject = project.tokenize(parser);

		tokenizedProject.query(model, dictionary, "score");

		List<Document> docs = tokenizedProject.stream()
			.flatMap(TokenizedCode::stream)
			.collect(Collectors.toList());

		for(Document doc : docs) {
			System.out.print(doc.getProperty(AbstractDocumentGenerator.PROPERTY_PATH_KEY));
			System.out.print("\t");
			System.out.print(getNaturalness(doc, "score"));
			System.out.println();
		}

		if(detail != null) {
			ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
			;
			mapper.writeValue(new File(detail), docs);
		}

		dictionary.store(dpath);
	}

	private static Project retriveFiles(String directory, String commitId, String unit) throws IOException {
		if(commitId == null) {
			return retriveFilesFromDirectory(directory, unit);
		} else {
			return retriveFilesFromCommit(directory, commitId, unit);
		}
	}

	private static Project retriveFilesFromDirectory(String directory, String unit) throws IOException {
		return new Project(Paths.get(directory));
	}

	private static Project retriveFilesFromCommit(String directory, String commitId, String unit) throws IOException {
		GitRepository repository = new GitRepository(directory);
		Commit commit = repository.getCommit(commitId);
		List<ProjectFile> docs = commit.retrieveFiles()
			.stream()
			.filter(v -> v.getPath()
				.endsWith(".java"))
			.map(ProjectFile::new)
			.collect(Collectors.toList());
		return new Project(docs);
	}

	private static double getNaturalness(Document doc, String key) {
		return doc.stream()
			.map(token -> token.getProperty(key))
			.mapToDouble(Double::parseDouble)
			.average()
			.orElse(Double.NaN);
	}

	public static void main(String[] args) throws Exception {
		CUIController controller = new CUIController();
		CmdLineParser parser = new CmdLineParser(controller);
		try {
			parser.parseArgument(args);
		} catch(CmdLineException e) {
			e.printStackTrace();
			parser.printUsage(System.err);
			System.exit(1);
		}
		controller.run();
	}
}
