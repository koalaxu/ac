package ac.ui.swing.dialog;

import java.util.function.Supplier;

import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.GenericDialog;
import ac.ui.swing.GenericFrame;
import ac.ui.swing.DialogValidator;

public abstract class StateDialog extends GenericDialog {
	private static final long serialVersionUID = 1L;
	public StateDialog(GenericDialog parent, Components cmp, DataAccessor data, Supplier<State> state_getter, String title, int width, int height,
			boolean has_confirm_button) {
		super(parent, cmp, data, title, width, height, has_confirm_button);
		this.state = state_getter.get();
	}
	public StateDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<State> state_getter, String title, int width, int height,
			boolean has_confirm_button) {
		super(parent, cmp, data, title, width, height, has_confirm_button);
		this.state = state_getter.get();
	}

	@Override
	protected Status Valid() {
		if (additional_validator != null) {
			Status status = additional_validator.get();
			if (status != Status.VALID) return status;
		}
		return DialogValidator.ValidateState(state);
	}
	
	protected void SetAdditionalValidator(Supplier<Status> validator) {
		additional_validator = validator;
	}

	protected State state;
	private Supplier<Status> additional_validator;
}
