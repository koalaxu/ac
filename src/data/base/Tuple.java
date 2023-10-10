package ac.data.base;

public class Tuple<T, U, V> {
	public Tuple(T t, U u, V v) {
		this.first = t;
		this.second = u;
		this.third = v;
	}
	
	public T first;
	public U second;
	public V third;
}
