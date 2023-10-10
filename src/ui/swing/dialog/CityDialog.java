package ac.ui.swing.dialog;

import java.util.function.Supplier;

import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;
import ac.ui.swing.DialogValidator;

public abstract class CityDialog extends StateDialog {
	private static final long serialVersionUID = 1L;

	public CityDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<City> city_getter,
			String title, int width, int height, boolean has_confirm_button) {
		super(parent, cmp, data, () -> city_getter.get().GetOwner(), title, width, height, has_confirm_button);
		this.city = city_getter.get();
		super.SetAdditionalValidator(() -> DialogValidator.ValidateCity(city, data));
	}

	protected City city;
}
