package ac.engine.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class DataCollection<T extends Data> implements Iterable<T> {
	public DataCollection(Collection<T> container, Predicate<T> condition) {
		this.container = container;
		this.condition = condition;
	}
	
	public int Size() {
		int size = 0;
		for (T t : container) {
			if (condition.test(t)) size++;
		}
		return size;
	}
	
	private class DataIterator implements Iterator<T> {
		private DataIterator(Iterator<T> iter) {
			this.iter = iter;
			next = AdvanceToNext();
		}
		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public T next() {
			T ret = next;
			next = AdvanceToNext();
			return ret;
		}
		
		private T AdvanceToNext() {
			while(iter.hasNext()) {
				T cur = iter.next();
				if (condition.test(cur)) return cur;
			}
			return null;
		}
		
		private Iterator<T> iter;
		private T next;
	}

	@Override
	public Iterator<T> iterator() {
		return new DataIterator(container.iterator());
	}

	private Collection<T> container;
	private Predicate<T> condition;
}
