package jp.ac.osaka_u.ist.sdl.rminer.jgit.blame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.lib.AnyObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.Commit;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitRepository;

public class NativeBlameResult implements BlameResult {
	private static final Logger log = LoggerFactory.getLogger(NativeBlameResult.class);

	private GitRepository repository;
	private AnyObjectId startCommit;
	private String filePath;
	private List<Element> result;
	private static Pattern HEADER = Pattern.compile("([0-9a-fA-F]{40}) (\\d+) (\\d+)(?: (\\d+))?");

	public NativeBlameResult(GitRepository repository, AnyObjectId startCommit, String filePath) throws IOException {
		this.repository = repository;
		this.startCommit = startCommit;
		this.filePath = filePath;
		blame();
	}

	@Override
	public int getResultSize() {
		return result.size();
	}

	@Override
	public Commit getSourceCommit(int i) {
		return result.get(i).sourceCommit;
	}

	@Override
	public String getSourcePath(int i) {
		return result.get(i).sourcePath;
	}

	@Override
	public int getSourceLine(int i) {
		return result.get(i).sourceLine - 1;
	}

	private void blame() throws IOException {

		ProcessBuilder build = new ProcessBuilder("git", "blame", "--line-porcelain", startCommit
			.name(), "--", filePath);
		build.directory(repository.getPath()
			.toFile());
		build.redirectError(ProcessBuilder.Redirect.INHERIT);
		Process process = null;
		BufferedReader reader = null;
		List<Element> result = new ArrayList<>();
		try {
			process = build.start();
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			List<String> buffer = new ArrayList<>();
			String line;
			while((line = reader.readLine()) != null) {
				buffer.add(line);
				if(line.startsWith("\t")) {
					Element element = parse(buffer);
					if(element == null) {
						result = Collections.emptyList();
						break;
					}
					result.add(parse(buffer));
					buffer.clear();
				}
			}
		} finally {
			if(reader != null) reader.close();
			if(process.isAlive()) {
				process.destroy();
			}
			try {
				if(!process.waitFor(5, TimeUnit.SECONDS)) {
					process.destroyForcibly()
						.waitFor();
				}
			} catch(InterruptedException e) {}

		}
		this.result = result;
	}

	private Element parse(List<String> buffer) {
		Matcher matcher = HEADER.matcher(buffer.get(0));
		if(!matcher.matches()) {
			log.info("match error:" + filePath);
			return null;
		}

		Element element = new Element();
		String[] header = buffer.get(0)
			.split(" ");
		element.sourceCommit = repository.getCommit(header[0]);
		element.sourceLine = Integer.parseInt(header[1]);

		for(String str : buffer) {
			if(str.startsWith("filename ")) {
				element.sourcePath = str.substring("filename ".length());
			}
		}

		return element;
	}

	static class Element {
		Commit sourceCommit;
		String sourcePath;
		int sourceLine;

	}
}
