package ac.ui.swing.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;

import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.DialogValidator;
import ac.ui.swing.GenericFrame;

public class PercentageDialog extends StateDialog {
	private static final long serialVersionUID = 1L;
	
	public static PercentageDialog CreatePercentageDialogForState(GenericFrame parent, Components cmp, DataAccessor data, Supplier<State> state_getter, String title, String field_name,
			Supplier<Integer> value_getter, Consumer<Integer> func) {
		return new PercentageDialog(parent, cmp, data, state_getter, title, field_name, value_getter.get(), func);
	}
	
	public static PercentageDialog CreatePercentageDialogForCity(GenericFrame parent, Components cmp, DataAccessor data, Supplier<City> city_getter, String title, String field_name,
			Supplier<Integer> value_getter, Consumer<Integer> func) {
		PercentageDialog dialog = new PercentageDialog(parent, cmp, data, DialogValidator.StateGetter(city_getter), title, field_name, value_getter.get(), func);
		dialog.SetAdditionalValidator(() -> DialogValidator.ValidateCity(city_getter.get(), data));
		return dialog;
	}
	
	protected PercentageDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<State> state_getter, String title, String field_name,
			int default_value, Consumer<Integer> func) {
		super(parent, cmp, data, state_getter, title, 300, 100, true);
		this.value = default_value;
		this.func = func;
		
		name = new JLabel(field_name);
		name.setBounds(10, 55, 100, 18);
		field.setBounds(120, 55, 30, 18);
		up.setBounds(160, 52, 18, 10);
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (value < 100) {
					value++;
					UpdateValue();
				}
			}
		});
		down.setBounds(160, 66, 18, 10);
		down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (value > 0) {
					value--;
					UpdateValue();
				}
			}
		});
		add(name);
		add(field);
		add(up);
		add(down);
		
		InitDone();
		UpdateValue();
	}

	@Override
	protected void Refresh() {
		UpdateValue();
	}

	@Override
	protected void Confirm() {
		func.accept(value);
		
	}
	
	private void UpdateValue() {
		field.setText(String.format("%d%%", value));
	}
	
	private int value;
	private Consumer<Integer> func;
	
	private JLabel name;
	private JLabel field = new JLabel();
	private JButton up = new JButton(Texts.upIcon);
	private JButton down = new JButton(Texts.downIcon);
}
