package jp.ac.osaka_u.ist.sdl.naturalness.component.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.ac.osaka_u.ist.sdl.naturalness.component.LanguageModelEstimator;
import slp.core.counting.Counter;
import slp.core.counting.io.CounterIO;
import slp.core.lexing.Lexer;
import slp.core.lexing.runners.LexerRunner;
import slp.core.lexing.simple.WhitespaceLexer;
import slp.core.modeling.Model;
import slp.core.modeling.dynamic.CacheModel;
import slp.core.modeling.mix.MixModel;
import slp.core.modeling.ngram.JMModel;
import slp.core.modeling.runners.ModelRunner;
import slp.core.translating.Vocabulary;
import slp.core.translating.VocabularyRunner;

public class SLPLMEstimator implements LanguageModelEstimator {
	int order;
	Lexer lexer;
	LexerRunner runner;
	
	@Override
	public void initialize(Map<String, String> parameter) {
		order = Integer.parseInt(parameter.getOrDefault("order", "3"));
		lexer = new WhitespaceLexer();
		runner = new LexerRunner(lexer, true);
	}

	@Override
	public void estimate(Path dataPath, Path outputPath) {
		File outFile = outputPath.toFile();
		Vocabulary vocaburary = new Vocabulary();
		Counter counter = new JMModel().getCounter();
		Model model = MixModel.standard(new JMModel(order, counter), new CacheModel());
		ModelRunner modelRunner = new ModelRunner(model, runner, vocaburary);
		
		modelRunner.learnFile(dataPath.toFile());
		
		/*
		try(Stream<String> stream = Files.lines(dataPath)) {
			AtomicInteger cnt = new AtomicInteger();
			stream.map(str -> str.split("\\s+"))
				.map(str -> Arrays.stream(str)
					.map(Integer::parseInt)
					.collect(Collectors.toList()))
				.forEach(list -> {
					model.notify(new File(Integer.toString(cnt.incrementAndGet())));
					model.learn(list);
				});

		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		*/

		
		// Force GigaCounter.resolve() (if applicable), just for accurate
		// timings below
		counter.getCount();
		CounterIO.writeCounter(counter, outFile);
		VocabularyRunner.write(vocaburary, constructVocabFilePath(outputPath));

	}

	public static File constructVocabFilePath(Path modelPath) {
		return modelPath.getParent()
			.resolve(modelPath.getFileName() + ".vocab")
			.toFile();
	}
}
