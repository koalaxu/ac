package ac.util;

import java.util.ArrayList;
import java.util.Random;

public class RandomUtil {
	public static <T extends Object> T Sample(ArrayList<T> pool, Random random) {
		int index = SampleFromUniformDistribution(0, pool.size() - 1, random);
		return pool.get(index);
	}
	
	public static <T extends Object> T Sample(T[] pool, Random random) {
		int index = SampleFromUniformDistribution(0, pool.length - 1, random);
		return pool[index];
	}
	
	public static int GetRandomIndexFromProbabilities(double[] weights, Random random) {
		double r = random.nextDouble();
		for (int i = 0; i < weights.length; i++) {
			r -= weights[i];
			if (r < 0) return i;
		}
		return weights.length;
	}
	
	public static int GetRandomIndexFromWeights(ArrayList<Double> weights, Random random) {
		double[] normalized_weights = new double[weights.size()];
		double total_weights = 0;
		for (double weight : weights) total_weights += weight;
		if (total_weights <= 0) return -1;
		for (int i = 0; i < weights.size(); ++i) normalized_weights[i] = weights.get(i) / total_weights;
		return GetRandomIndexFromProbabilities(normalized_weights, random);
	}

	public static boolean WhetherToHappend(double probability, Random random) {
		return random.nextFloat() < probability;
	}
	
	public static double SampleFromNormalDistribution(double mean, double std, Random random) {
		return random.nextGaussian() * std + mean;
	}
	
	// upperbound inclusive
	public static int SampleFromUniformDistribution(int lowerbound, int upperbound, Random random) {
		return random.nextInt(upperbound - lowerbound + 1) + lowerbound;
	}

	public static double SampleFromUniformDistribution(double lowerbound, double upperbound, Random random) {
		return random.nextDouble() * (upperbound - lowerbound) + lowerbound;
	}
}
