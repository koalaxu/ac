package ac.engine.data;

public abstract class IdKeyedData extends Data implements Comparable<IdKeyedData> {

	protected IdKeyedData(DataAccessor accessor, int id) {
		super(accessor);
		this.id = id;
	}
	
	public abstract String GetName();
	
	@Override
	public int compareTo(IdKeyedData o) {
		return this.id - o.id;
	}

	protected int id;
}
