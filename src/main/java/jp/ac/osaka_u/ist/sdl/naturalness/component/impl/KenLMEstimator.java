package jp.ac.osaka_u.ist.sdl.naturalness.component.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.osaka_u.ist.sdl.naturalness.NaturalnessException;
import jp.ac.osaka_u.ist.sdl.naturalness.component.LanguageModelEstimator;

public class KenLMEstimator implements LanguageModelEstimator {
	private Map<String, String> options;

	public KenLMEstimator() {
		options = new HashMap<>();
		options.put("order", "5");
	}

	public void setOption(String key, String value) {
		options.put(key, value);
	}

	public String getOption(String key) {
		return options.get(key);
	}

	@Override
	public void estimate(Path dataPath, Path outputPath) {
		setOption("text", dataPath.toString());
		setOption("arpa", outputPath.toString());

		List<String> command = constructCommand();

		ProcessBuilder build = new ProcessBuilder(command);
		build.inheritIO();
		Process process;
		try {
			process = build.start();
		} catch(IOException e) {
			throw new NaturalnessException(e);
		}

		int exitCode = 0;
		try {
			exitCode = process.waitFor();
		} catch(InterruptedException e) {}

		if(exitCode != 0) {
			throw new NaturalnessException("Faild to estimate model. Exit code is not zero.");
		}
	}

	private List<String> constructCommand() {
		String path = options.getOrDefault("lmplzPath", "lmplz");
		options.remove("lmplzPath");
		ArrayList<String> command = new ArrayList<>();
		command.add(path);
		options.forEach((key, value) -> {
			if(key.length() == 1) {
				command.add("-" + key);
			} else {
				command.add("--" + key);
			}

			if(value != null && !value.equals("")) command.add(value);
		});
		return command;
	}

	@Override
	public void initialize(Map<String, String> parameter) {
		parameter.forEach((k, v) -> {
			switch(k){
			case "lmplzPath":
			case "text":
			case "arpa":
				options.put(k, v);
				break;
			case "order":
			case "o":
				options.put("order", v);
				break;
			}
		});
	}
}
