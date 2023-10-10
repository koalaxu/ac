package ac.util;

public class MathUtil {
	public static double SumOfHarmonicSeries(int n) {
		if (n == 0) return 0.0;
		if (n > maxCachedSumOfHarmonicSeries) return cachedSumOfHarmonicSeries[maxCachedSumOfHarmonicSeries - 1];
		return cachedSumOfHarmonicSeries[n - 1];
	}
	
	public static long RoundByThousand(long value) {
		return (value / 1000 * 1000) + ((value % 1000) >= 500 ? 1000 : 0);
	}
	
	public static long FlooredByHundred(long value) {
		return (value / 100 * 100);
	}
	
	private static int maxCachedSumOfHarmonicSeries = 50;
	private static double[] cachedSumOfHarmonicSeries;
	static {
		cachedSumOfHarmonicSeries = new double[maxCachedSumOfHarmonicSeries];
		cachedSumOfHarmonicSeries[0] = 1.0;
		for (int i = 1; i < maxCachedSumOfHarmonicSeries; ++i) cachedSumOfHarmonicSeries[i] = cachedSumOfHarmonicSeries[i - 1] + 1.0 / (i + 1);
	};
}
