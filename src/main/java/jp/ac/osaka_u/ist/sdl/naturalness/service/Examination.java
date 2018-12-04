package jp.ac.osaka_u.ist.sdl.naturalness.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.ac.osaka_u.ist.sdl.naturalness.NaturalnessException;
import jp.ac.osaka_u.ist.sdl.naturalness.component.Dictionary;
import jp.ac.osaka_u.ist.sdl.naturalness.component.LanguageModel;
import jp.ac.osaka_u.ist.sdl.naturalness.component.Project;
import jp.ac.osaka_u.ist.sdl.naturalness.component.ProjectFile;
import jp.ac.osaka_u.ist.sdl.naturalness.component.TokenizedProject;
import jp.ac.osaka_u.ist.sdl.naturalness.component.impl.HashMapDictionary;
import jp.ac.osaka_u.ist.sdl.naturalness.component.impl.KenLM;
import jp.ac.osaka_u.ist.sdl.naturalness.component.impl.KenLMEstimator;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.ChangedFile;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.Commit;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitFile;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitRepository;
import jp.ac.osaka_u.ist.sdl.rminer.kenja.ExtractMethod;
import jp.ac.osaka_u.ist.sdl.rminer.kenja.Kenja;
import jp.ac.osaka_u.ist.sdl.rminer.parser.Document;
import jp.ac.osaka_u.ist.sdl.rminer.parser.TokenizedCode;
import jp.ac.osaka_u.ist.sdl.rminer.parser.java.AbstractDocumentGenerator;
import jp.ac.osaka_u.ist.sdl.rminer.parser.java.JavaParser;

public class Examination {

	@Argument(index = 0, required = true, usage = "Path to target directory.")
	public String directory;

	@Argument(index = 1, required = true, usage = "Path to kenja json file.")
	public String kenja;

	@Argument(index = 2, usage = "Path to dictionary Path.")
	public String dictionary = "dict.txt";

	ObjectMapper mapper = new ObjectMapper();

	public static class Result {
		public double naturalA;
		public double naturalB;
		public String origCommit;
		public List<Pair> commitA;
		public List<Pair> commitB;

		public Result(double naturalA, double naturalB, String origCommit, List<Pair> commitA, List<Pair> commitB) {
			this.naturalA = naturalA;
			this.naturalB = naturalB;
			this.origCommit = origCommit;
			this.commitA = commitA;
			this.commitB = commitB;
		}
	}

	public static class Pair {
		public String path;
		public double score;

		public Pair(String path, double score) {
			this.path = path;
			this.score = score;
		}
	}

	public static class CommitPair {
		public Commit a;
		public Commit b;

		public CommitPair(Commit a, Commit b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((b == null) ? 0 : b.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(!(obj instanceof CommitPair))
				return false;
			CommitPair other = (CommitPair)obj;
			if(a == null) {
				if(other.a != null)
					return false;
			} else if(!a.equals(other.a))
				return false;
			if(b == null) {
				if(other.b != null)
					return false;
			} else if(!b.equals(other.b))
				return false;
			return true;
		}
	}

	public void run() throws Exception {
		exam(directory, kenja);
	}

	public void exam(String repoPath, String kenjaJson) throws Exception {
		GitRepository repository = new GitRepository(repoPath);

		Dictionary dictionary = new HashMapDictionary();
		Path dpath = Paths.get(this.dictionary);
		if(Files.exists(dpath)) {
			dictionary.load(dpath);
		}

		JavaParser parser = new JavaParser();
		Map<String, String> params = new HashMap<>();
		params.put("unit", "file");
		params.put("identifier", "none");
		parser.initialize(params);

		List<ExtractMethod> kenja = Kenja.loadExtractMethod(kenjaJson);
		KenLMEstimator estimator = new KenLMEstimator();

		List<Result> answer = new ArrayList<>();
		HashSet<CommitPair> cache = new HashSet<>();

		for(ExtractMethod extract : kenja) {
			Commit commitb = repository.getCommit(extract.b_org_commit);
			Commit commita = commitb.getParent();
			CommitPair cp = new CommitPair(commita, commitb);
			if(cache.contains(cp)) {
				continue;
			}
			cache.add(cp);
			List<ChangedFile> diffs = commitb.diff(commita);

			List<GitFile> filesB = diffs.stream()
				.map(v -> v.getBaseFile())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

			Project projectB = new Project(filesB.stream()
				.map(ProjectFile::new)
				.collect(Collectors.toList()));

			List<GitFile> filesA = diffs.stream()
				.map(v -> v.getCompareFile())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

			Project projectA = new Project(filesA.stream()
				.map(ProjectFile::new)
				.collect(Collectors.toList()));

			Set<GitFile> modFiles = new HashSet<>(filesA);

			List<ProjectFile> files = commita.retrieveFiles()
				.stream()
				.filter(v -> v.getPath()
					.endsWith(".java"))
				.filter(v -> !modFiles.contains(v))
				.map(ProjectFile::new)
				.collect(Collectors.toList());

			Project modelProject = new Project(files);

			Path outputModel = Files.createTempFile(null, null);
			try {
				modelProject.index(parser, dictionary)
					.estimateModel(estimator, outputModel);
				;
			} catch(NaturalnessException e) {
				e.printStackTrace();
				continue;
			}

			LanguageModel model = new KenLM();
			model.initialize(outputModel, Collections.emptyMap());

			TokenizedProject tprojectB = projectB.tokenize(parser);
			tprojectB.query(model, dictionary, "score");
			List<Pair> listB = new ArrayList<>();
			double sumb = 0;
			int countb = 0;
			for(Document doc : iterable(tprojectB.stream()
				.flatMap(TokenizedCode::stream))) {
				double score = getNaturalnessSum(doc, "score");
				int size = doc.getSize();
				listB.add(new Pair(doc.getProperty(AbstractDocumentGenerator.PROPERTY_PATH_KEY), score / size));
				sumb += score;
				countb += size;
			}

			TokenizedProject tprojectA = projectA.tokenize(parser);
			tprojectA.query(model, dictionary, "score");
			List<Pair> listA = new ArrayList<>();
			double suma = 0;
			int counta = 0;
			for(Document doc : iterable(tprojectA.stream()
				.flatMap(TokenizedCode::stream))) {
				double score = getNaturalnessSum(doc, "score");
				int size = doc.getSize();
				listA.add(new Pair(doc.getProperty(AbstractDocumentGenerator.PROPERTY_PATH_KEY), score / size));
				suma += score;
				counta += size;
			}

			answer.add(new Result(suma, sumb, extract.b_org_commit, listA, listB));
			System.out
				.printf("%s, %f, %f, %f\n", extract.b_org_commit, suma / counta, sumb / countb, suma / counta - sumb / countb);

			model.finalizeModel();
		}
		mapper.writeValue(new File("natural-result.json"), answer);

		dictionary.store(dpath);
	}

	private static <T> Iterable<T> iterable(Stream<T> stream) {
		return () -> stream.iterator();
	}

	private static double getNaturalnessSum(Document doc, String key) {
		return doc.stream()
			.map(token -> token.getProperty(key))
			.mapToDouble(Double::parseDouble)
			.sum();
	}

	public static void main(String[] args) throws Exception {
		Examination examination = new Examination();
		CmdLineParser parser = new CmdLineParser(examination);
		try {
			parser.parseArgument(args);
		} catch(CmdLineException e) {
			parser.printUsage(System.err);
			System.exit(1);
		}
		examination.run();
	}
}
