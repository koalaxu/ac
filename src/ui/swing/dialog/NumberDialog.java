package ac.ui.swing.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.DialogValidator;
import ac.ui.swing.GenericDialog;
import ac.ui.swing.GenericFrame;

public class NumberDialog extends StateDialog {
	private static final long serialVersionUID = 1L;
	public NumberDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<City> city_getter, String title, String field_name,
			int default_value, Supplier<Integer> max_value_getter, Consumer<Integer> func) {
		super(parent, cmp, data, DialogValidator.StateGetter(city_getter), title, 360, 120, true);
		this.SetAdditionalValidator(() -> DialogValidator.ValidateCity(city_getter.get(), data));
		Init(field_name, default_value, max_value_getter, func);
	}	
	
	public NumberDialog(GenericDialog parent, Components cmp, DataAccessor data, Supplier<State> state_getter, String title, String field_name,
			int default_value, Supplier<Integer> max_value_getter, Consumer<Integer> func) {
		super(parent, cmp, data, state_getter, title, 360, 120, true);
		Init(field_name, default_value, max_value_getter, func);
	}
	private void Init(String field_name, int default_value, Supplier<Integer> max_value_getter, Consumer<Integer> func) {
		this.value = default_value;
		this.func = func;
		
		name = new JLabel(field_name);
		name.setBounds(10, 55, 100, 18);
		field.setBounds(120, 55, 80, 18);
		field.setHorizontalAlignment(SwingConstants.RIGHT);
		up.setBounds(245, 50, 18, 12);
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (value + 1 <= max_value_getter.get()) {
					value++;
					UpdateValue();
				}
			}
		});
		down.setBounds(245, 66, 18, 12);
		down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (value > 0) {
					value--;
					UpdateValue();
				}
			}
		});
		left.setBounds(228, 55, 12, 18);
		left.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (value * 10 <= max_value_getter.get()) {
					value *= 10;
					UpdateValue();
				}
			}
		});
		right.setBounds(268, 55, 12, 18);
		right.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				value /= 10;
				UpdateValue();
			}
		});
		max.setBounds(228, 90, 52, 20);
		max.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				value = max_value_getter.get();
				UpdateValue();
			}
		});
		add(name);
		add(field);
		add(up);
		add(down);
		add(left);
		add(right);
		add(max);
		
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
		field.setText(String.format("%,d", value));
	}
	
	private int value;
	private Consumer<Integer> func;
	
	private JLabel name;
	private JLabel field = new JLabel();
	private JButton up = new JButton(Texts.upIcon);
	private JButton down = new JButton(Texts.downIcon);
	private JButton left = new JButton(Texts.leftIcon);
	private JButton right = new JButton(Texts.rightIcon);
	private JButton max = new JButton("Max");
}
