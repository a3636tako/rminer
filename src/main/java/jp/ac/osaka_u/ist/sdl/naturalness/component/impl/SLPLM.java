package jp.ac.osaka_u.ist.sdl.naturalness.component.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jp.ac.osaka_u.ist.sdl.naturalness.component.LanguageModel;
import slp.core.counting.Counter;
import slp.core.counting.io.CounterIO;
import slp.core.modeling.Model;
import slp.core.modeling.ModelRunner;
import slp.core.modeling.mix.InverseMixModel;
import slp.core.modeling.ngram.JMModel;
import slp.core.modeling.ngram.NGramCache;

public class SLPLM implements LanguageModel {
	Path modelPath;
	Map<String, String> parameter;
	Counter counter;

	@Override
	public void initialize(Path modelPath, Map<String, String> parameter) {
		this.parameter = parameter;
		this.modelPath = modelPath;
		this.counter = CounterIO.readCounter(modelPath.toFile());

	}

	@Override
	public List<Double> estimate(List<Long> id) {
		ModelRunner.perLine(true);
		ModelRunner.setNGramOrder(Integer.parseInt(parameter.getOrDefault("order", "3")));
		Model model = new InverseMixModel(new JMModel(counter), new NGramCache());

		Stream<Stream<String>> data = Stream.of(id.stream()
			.map(l -> l.toString()));
		return ModelRunner.modelTokens(model, data)
			.get(0)
			.stream()
			.map(val -> -val)
			.collect(Collectors.toList());
	}
}
