package ac.ui.swing;

import ac.engine.data.Data;
import ac.engine.data.DataAccessor;

public abstract class TypedDataPanel<T extends Data> extends GenericPanel {
	private static final long serialVersionUID = 1L;

	protected TypedDataPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		setLayout(null);
	}

	public abstract void Reset(T input);
	public T GetData() {
		return value;
	}
	
	protected T value;
}
