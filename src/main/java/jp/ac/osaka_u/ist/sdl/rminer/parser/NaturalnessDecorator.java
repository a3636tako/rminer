package jp.ac.osaka_u.ist.sdl.rminer.parser;

import java.util.Arrays;
import java.util.List;

public class NaturalnessDecorator implements TokenDecorator {

	double[] thresholds;

	public NaturalnessDecorator(double min, double max, int num) {
		double diff = max - min;
		thresholds = new double[num];
		for(int i = 0; i < num; i++) {
			thresholds[i] = min + i * diff / num;
		}
	}

	public NaturalnessDecorator(double[] thresholds) {
		this.thresholds = thresholds;
		Arrays.sort(this.thresholds);
	}

	public NaturalnessDecorator(List<Double> thresholds) {
		this(thresholds.stream()
			.mapToDouble(Double::doubleValue)
			.toArray());
	}

	@Override
	public boolean match(Token token) {
		return token.getProperty("score") != null;
	}

	@Override
	public String decorate(Token token, String origin) {
		return String.format("<span title=\"" + token
			.getProperty("score") + "\"style='background-color:%s;'>%s</span>", getColor(Double
				.parseDouble(token.getProperty("score"))), origin);
	}

	public String getColor(double score) {
		int rate = toRate(score);
		//if(rate > -90) rate = 0;
		if(rate > 0) {
			return String.format("rgb(%d%%, %d%%, 100%%)", 100 - rate, 100 - rate);
		} else {
			return String.format("rgb(100%%, %d%%, %d%%)", 100 + rate, 100 + rate);
		}
	}

	private int toRate(double score) {
		int idx = Arrays.binarySearch(thresholds, score);
		if(idx < 0) {
			idx = -idx - 1;
		}

		if(thresholds.length % 2 == 0) {
			idx -= thresholds.length / 2;
			return idx * 100 / (thresholds.length / 2);

		} else {
			idx -= thresholds.length / 2;
			if(idx <= 0) idx--;
			return idx * 100 / (thresholds.length / 2 + 1);
		}
	}
}
