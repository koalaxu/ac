package ac.engine.data;

public class Data {
	protected Data(DataAccessor accessor) {
		this.accessor = accessor;
	}
	
	protected DataAccessor accessor;
}
