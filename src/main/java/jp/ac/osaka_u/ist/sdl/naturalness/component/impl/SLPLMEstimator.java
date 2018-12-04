package jp.ac.osaka_u.ist.sdl.naturalness.component.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import jp.ac.osaka_u.ist.sdl.naturalness.component.LanguageModelEstimator;
import slp.core.counting.Counter;
import slp.core.counting.giga.GigaCounter;
import slp.core.counting.io.CounterIO;
import slp.core.modeling.ModelRunner;
import slp.core.modeling.ngram.JMModel;
import slp.core.modeling.ngram.NGramModel;

public class SLPLMEstimator implements LanguageModelEstimator {

	@Override
	public void initialize(Map<String, String> parameter) {
		ModelRunner.perLine(true);
		ModelRunner.setNGramOrder(Integer.parseInt(parameter.getOrDefault("order", "3")));
	}

	@Override
	public void estimate(Path dataPath, Path outputPath) {
		File inDir = dataPath.toFile();
		File outFile = outputPath.toFile();

		NGramModel model = new JMModel(new GigaCounter());
		ModelRunner.learn(model, inDir);
		// Since this is for training n-grams only, override ModelRunner's model
		// for easy access to the counter
		Counter counter = model.getCounter();
		// Force GigaCounter.resolve() (if applicable), just for accurate
		// timings below
		counter.getCount();
		CounterIO.writeCounter(counter, outFile);
	}

}
