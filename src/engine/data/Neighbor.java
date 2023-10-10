package ac.engine.data;

public class Neighbor extends Data {

	protected Neighbor(DataAccessor accessor, City city) {
		super(accessor);
		this.city = city;
	}
	
	public City GetCity() {
		return city;
	}

	private City city;
}
