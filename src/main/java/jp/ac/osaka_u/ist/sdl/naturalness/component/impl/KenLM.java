package jp.ac.osaka_u.ist.sdl.naturalness.component.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jp.ac.osaka_u.ist.sdl.naturalness.NaturalnessException;
import jp.ac.osaka_u.ist.sdl.naturalness.QueueThread;
import jp.ac.osaka_u.ist.sdl.naturalness.component.LanguageModel;

public class KenLM implements LanguageModel {

	private String pathQuery;
	private Path modelPath;
	private Process process;
	private QueueThread<List<Long>> outputQueue;
	private BufferedReader reader;
	private static final Pattern pattern = Pattern.compile("(\\S+)=(\\d+) (\\d+) (\\S+)\t");

	public KenLM() {
	}

	@Override
	public void initialize(Path modelPath, Map<String, String> parameter) {
		this.modelPath = modelPath;
		this.pathQuery = parameter.getOrDefault("queryPath", "query");
		runProcess();
	}

	@Override
	public List<Double> estimate(List<Long> id) {
		outputQueue.offer(id);
		String str = null;
		try {
			str = reader.readLine();

		} catch(IOException e) {
			throw new NaturalnessException(e);
		}
		if(str == null) {
			throw new NaturalnessException("End of stream");
		}

		List<Double> ret = new ArrayList<>();
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()) {
			double score = Double.parseDouble(matcher.group(4));
			ret.add(score);
		}
		return ret;
	}

	@Override
	public void finalizeModel() {
		try {
			reader.close();
			outputQueue.finishQueue();
			outputQueue.join();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void runProcess() throws NaturalnessException {
		ProcessBuilder build = new ProcessBuilder(pathQuery, "-b", "-n", modelPath.toString());
		build.redirectError(Redirect.INHERIT);

		try {
			process = build.start();
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		} catch(IOException e) {
			throw new NaturalnessException(e);
		}

		outputQueue = new QueueThread<List<Long>>() {
			BufferedWriter buf;

			@Override
			protected void init() {
				buf = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			}

			@Override
			protected void consume(List<Long> id) {
				try {
					buf.write(id.stream()
						.map(v -> v.toString())
						.collect(Collectors.joining(" ")));
					buf.newLine();
					buf.flush();
				} catch(IOException e) {
					throw new NaturalnessException(e);
				}
			}

			@Override
			protected void close() {
				try {
					buf.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		};
		outputQueue.start();
	}
}
