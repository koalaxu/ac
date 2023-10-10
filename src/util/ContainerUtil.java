package ac.util;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Predicate;

public class ContainerUtil {
	public static <T> T FindTop(Iterable<T> container, Comparator<T> cmp) {
		return FindTopWithFilter(container, cmp, null);
	}
	
	public static <T> T FindTopWithFilter(Iterable<T> container, Comparator<T> cmp, Predicate<T> filter) {
		T max = null;
		for (T t : container) {
			if (filter != null && filter.test(t)) continue;
			if (max == null || cmp.compare(t, max) < 0) {
				max = t;
			}
		}
		return max;
	}
	
	public static <T> T FindTopWithAttributes(Iterable<T> container, Map<T, Double> attributes, Comparator<Double> cmp, Predicate<T> filter) {
		return FindTopWithFilter(container, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				Double v1 = attributes.get(o1);
				Double v2 = attributes.get(o2);
				if (v1 == null) {
					if (v2 == null) return 0;
					return 1;
				}
				if (v2 == null) return -1;
				return cmp.compare(v1, v2);
			}
			
		}, filter);
	}
	
	public static long[] Distribute(long[] values, long amount) {
		long[] outputs = new long[values.length];
		long total = 0L;
		for (long value : values) {
			total += value;
		}
		amount = Math.min(amount, total);
		double ratio = Math.min(1.0, (double)amount / total);
		for (int i = 0; i < values.length; ++i) {
			long assigned = Math.min(amount, Math.round(ratio * values[i]));
			outputs[i] = assigned;
			amount -= assigned;
		}
		while (amount > 0) {
			for (int i = 0; i < values.length; ++i) {
				if (outputs[i] < values[i]) {
					outputs[i] += 1;
					if (--amount <= 0) break;
				}
			}
		}
		return outputs;
	}
}
