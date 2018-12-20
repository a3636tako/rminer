package jp.ac.osaka_u.ist.sdl.naturalness.component.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.ac.osaka_u.ist.sdl.naturalness.component.LanguageModel;
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
import slp.core.util.Pair;

public class SLPLM implements LanguageModel {
	Path modelPath;
	Map<String, String> parameter;
	Lexer lexer;
	LexerRunner lexerRunner;
	int order;
	Counter counter;
	int vocaburarySize;
	private Vocabulary vocabulary;
	
	@Override
	public void initialize(Path modelPath, Map<String, String> parameter) {
		this.parameter = parameter;
		this.modelPath = modelPath;
		this.order = Integer.parseInt(parameter.getOrDefault("order", "3"));
		this.vocaburarySize = Integer.parseInt(parameter.get("vocaburarySize"));
		this.counter = CounterIO.readCounter(modelPath.toFile());
		lexer = new WhitespaceLexer();
		lexerRunner = new LexerRunner(lexer, true);
		vocabulary = VocabularyRunner.read(SLPLMEstimator.constructVocabFilePath(modelPath));
	}

	@Override
	public List<Double> estimate(List<Long> id) {
		Model model = MixModel.standard(new JMModel(order, counter), new CacheModel());
		ModelRunner modelRunner = new ModelRunner(model, lexerRunner, vocabulary);
		
		Stream<Stream<String>> stream = Stream.of(id.stream().map(v -> Long.toString(v)));
		List<List<Double>> results = modelRunner.modelTokens(stream);
		return results.get(0);
		
		/*
		model.notify(null);
		return model.model(id.stream().map(Long::intValue).collect(Collectors.toList()))
			.stream()
			.map(p -> this.toProb(p))
			.map(v -> ModelRunner.toEntropy(v))
			.collect(Collectors.toList());
			*/
	}
	
	public double toProb(Pair<Double, Double> probConf) {
		double prob = probConf.left;
		double conf = probConf.right;
		return prob*conf + (1 - conf)/this.vocaburarySize;
	}
}
