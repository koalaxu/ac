package ac.data.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MultiMap<K, V> {
	public void Clear() {
		map.clear();
	}
	
	public void Insert(K k, V v) {
		ArrayList<V> list = map.get(k);
		if (list == null) {
			list = new ArrayList<V>();
			map.put(k, list);
		}
		list.add(v);
	}
	
	public boolean Remove(K k, V v) {
		return Get(k).remove(v);
	}
	
	public ArrayList<V> Get(K k) {
		final ArrayList<V> empty = new ArrayList<V>();
		return map.getOrDefault(k, empty);
	}
	
	public Collection<ArrayList<V>> GetValues() {
		return map.values();
	}
	
	public boolean IsEmpty() {
		return map.isEmpty();
	}
	
	public boolean Contains(K k) {
		return map.containsKey(k);
	}
	
	private HashMap<K, ArrayList<V>> map = new HashMap<K, ArrayList<V>>();
}
