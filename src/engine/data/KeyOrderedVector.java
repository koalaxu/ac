package ac.engine.data;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class KeyOrderedVector<T extends Data, K> {
	public KeyOrderedVector(ElementComparator<T, K> cmp) {
		ordered_elements = new TreeSet<T>(new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return cmp.CompareElement(o1, o2);
			}
		});
		this.cmp = cmp;
	}
	
	public static interface ElementComparator<T, K> {
		public int CompareKey(T t, K k);
		public int CompareElement(T o1, T o2);
	}
	
	public class OutputVector implements Iterable<T> {
		public OutputVector(K k, boolean poll) {
			this.k = k;
			this.poll = poll;
		}

		@Override
		public Iterator<T> iterator() {
			return poll ? new PollOutputIterator() : new PeekOutputIterator();
		}
		
		private class PollOutputIterator implements Iterator<T> {
			@Override
			public boolean hasNext() {
				return !ordered_elements.isEmpty() && cmp.CompareKey(ordered_elements.first(), k) < 0;
			}

			@Override
			public T next() {
				return ordered_elements.pollFirst();
			}
		}
		
		private class PeekOutputIterator implements Iterator<T> {
			@Override
			public boolean hasNext() {
				return next != null && cmp.CompareKey(next, k) < 0;
			}

			@Override
			public T next() {
				T current = next;
				next = iter.hasNext() ? iter.next() : null;
				return current;
			}
			
			private Iterator<T> iter = ordered_elements.iterator();
			private T next = iter.hasNext() ? iter.next() : null;
		}
		
		private K k;
		private boolean poll = true;
	}
	
	public void Insert(T t) {
		ordered_elements.add(t);
	}
	
	public Iterable<T> GetElementsLessThan(K k) {
		return new OutputVector(k, false);
	}
	
	public Iterable<T> PollElementsLessThan(K k) {
		return new OutputVector(k, true);
	}
	
	public void Clear() {
		ordered_elements.clear();
	}
	
	public void Remove(T t) {
		ordered_elements.remove(t);
	}

	private TreeSet<T> ordered_elements; 
	private ElementComparator<T, K> cmp;
}
 