package ac.util;

import java.util.Comparator;
import java.util.function.Predicate;

public class ComparatorUtil {
	@SafeVarargs
	public static <T> Comparator<T> CreatePriorityComparator(Comparator<T>... comparators) {
		return new PriorityComparator<T>(comparators);
	}
	
	private static class PriorityComparator<T> implements Comparator<T> {
		@SafeVarargs
		public PriorityComparator(Comparator<T>... comparators) {
			this.comparators = comparators;
		}

		@Override
		public int compare(T o1, T o2) {
			for (Comparator<T> comparator : comparators) {
				int ret = comparator.compare(o1, o2);
				if (ret != 0) return ret;
			}
			return 0;
		}
		Comparator<T>[] comparators;
	}
	
	public static <T> Comparator<T> CreateAttributeComparator(Predicate<T> predicate) {
		return new AttributeComparator<T>(predicate);
	}
	
	public static class AttributeComparator<T> implements Comparator<T> {
		public AttributeComparator(Predicate<T> predicate) {
			this.predicate = predicate;
		}
		
		@Override
		public int compare(T o1, T o2) {
			boolean b1 = predicate.test(o1);
			boolean b2 = predicate.test(o2);
			if (b1) {
				return b2 ? 0 : -1;
			}
			return b2 ? 1 : 0;
		}
		
		private Predicate<T> predicate;
	}
}
