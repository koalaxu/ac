package ac.data.base;

public class Pair<K, V> {
	public Pair(K k, V v) {
		this.first = k;
		this.second = v;
	}
	
	public K first;
	public V second;
	
    @Override
	public int hashCode() {
		final int prime = 10031;
		int result = 1;
		result = prime * result + first.hashCode();
		result = prime * result + second.hashCode();
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<K, V> other = (Pair<K, V>) obj;
		if (first != other.first || second != other.second)
			return false;
		return true;
	}
}
