package ac.ui.swing;

import java.util.function.Supplier;

import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.GenericDialog.Status;

public class DialogValidator {
	public static Status ValidateCity(City city, DataAccessor data) {
		if (city == null) return Status.IDLE;
		return city.GetOwner() == data.GetPlayer().GetState() ? Status.VALID : Status.INVALID;
	}
	
	public static Status ValidateState(State state) {
		if (state == null) return Status.IDLE;
		return state.GetOwnedCities().isEmpty()? Status.INVALID : Status.VALID;
	}
	
	public static Supplier<State> StateGetter(Supplier<City> city_getter) {
		City city = city_getter.get();
		return city == null ? () -> null : () -> city.GetOwner();
	}
}
