package ac.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ac.data.CityData;
import ac.data.GameData;
import ac.data.base.Date;
import ac.data.base.Position;
import ac.data.constant.Technology.TechnologyType;
import ac.data.constant.Texts;
import ac.tools.FieldParser.FieldLocator;
import ac.ui.swing.util.ColorUtil;
import ac.util.StringUtil;

abstract class FieldEditor<T> {
	protected FieldEditor(FieldLocator locator) {
		this.locator = locator;
	}
	public JComponent GetComponent() {
		return cmp;
	}
	
	@SuppressWarnings("unchecked")
	protected T GetDefaultValue() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Field field = locator.field;
		Object obj = locator.parant;
		if (locator.key != null) {
			if (locator.is_array) {
				return (T) locator.getter.invoke(null, obj, locator.key);
			}
			if (locator.container_type == ArrayList.class) {
				if ((Integer)locator.key >= ((ArrayList<T>)obj).size()) {
					return null;
				}
			} else if (locator.container_type == LinkedList.class) {
				if ((Integer)locator.key >= ((LinkedList<T>)obj).size()) {
					return null;
				}
			}
			return (T)locator.getter.invoke(obj, locator.key);
		}
		if (field != null) {
			return (T)field.get(obj);
		}
		return (T)locator.getter.invoke(obj);
	}
	
	@SuppressWarnings("unchecked")
	protected void SetValue(T t) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Field field = locator.field;
		Object obj = locator.parant;
		if (locator.key != null) {
			if (locator.is_array) {
				locator.setter.invoke(null, obj, locator.key, t);
				return;
			}
			if (locator.container_type == ArrayList.class) {
				int key = (Integer)locator.key;
				ArrayList<T> list = (ArrayList<T>)obj;
				if (t == null) {
					while (key < list.size()) {
						list.remove(key);
					}
					return;
				} else {
					while (key >= list.size()) {
						list.add(null);
					}
				}
			} else if (locator.container_type == LinkedList.class) {
				int key = (Integer)locator.key;
				LinkedList<T> list = (LinkedList<T>)obj;
				if (t == null) {
					while (key < list.size()) {
						list.removeLast();
					}
					return;
				} else {
					while (key >= list.size()) {
						list.add(null);
					}
				}
			}
			locator.setter.invoke(obj, locator.key, t);
			return;
		}
		if (field != null) {
			field.set(obj, t);
			return;
		}
		locator.setter.invoke(obj, t);
	}
	
	public void ConvertAndSet() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException{
		T value = Convert();
		SetValue(value);
	}
	public abstract T Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException;
	public abstract long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException;
	protected JComponent cmp;
	protected FieldLocator locator;
	
	static class StringField extends FieldEditor<String> {
		public StringField(FieldLocator locator) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			cmp = new JTextField(GetDefaultValue());
		}
	
		@Override
		public String Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			return ((JTextField)cmp).getText();
		}
	
		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			return 0;
		}
	}
	
	static class DateField extends FieldEditor<Date> {
		public DateField(FieldLocator locator) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			cmp = new JTextField(GetDefaultValue().FormatString());
		}
	
		@Override
		public Date Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			String text = ((JTextField)cmp).getText();
			String[] texts = text.split("/");
			if (texts.length != 3) {
				((JTextField)cmp).setText("");
				return null;
			}
			return new Date(Integer.valueOf(texts[0]), Integer.valueOf(texts[1]), Integer.valueOf(texts[2]));
		}
	
		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			return Convert().value;
		}
	}
	
	static class PositionField extends FieldEditor<Position> {
		public PositionField(FieldLocator locator) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			cmp = new JTextField(GetDefaultValue().FormatString());
		}
	
		@Override
		public Position Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			String text = ((JTextField)cmp).getText();
			String[] texts = text.split(",");
			if (texts.length != 2) {
				((JTextField)cmp).setText("");
				return null;
			}
			return new Position(Integer.valueOf(texts[0]), Integer.valueOf(texts[1]));
		}
	
		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			Position pos = Convert();
			return pos.x * 1001 + pos.y;
		}
	}

	static class NumericField<T extends Number> extends FieldEditor<T> {
		public NumericField(FieldLocator locator, Class<T> clazz) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			this.clazz = clazz;
			T value = GetDefaultValue();
			cmp = new JTextField(value != null ? value.toString() : "");
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public T Convert() throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
			String text = ((JTextField)cmp).getText();
			if (text.isEmpty()) return null;
			Method func = clazz.getDeclaredMethod("valueOf", String.class);
			return (T) func.invoke(null, text);
		}
		
		private Class<T> clazz;
	
		@Override
		public long ConvertToNumber()
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
			return Convert().longValue();
		}
	}
	
	static class EnumField<T> extends FieldEditor<T> {
		public EnumField(FieldLocator locator, Class<T> clazz) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			this.clazz = clazz;
			T value = GetDefaultValue();
			String[] values = new String[clazz.getEnumConstants().length];
			int selected = 0;
			for (int i = 0; i < values.length; ++i) {
				T t = clazz.getEnumConstants()[i];
				values[i] = t.toString();
				if (t == value) {
					selected = i;
				}
			}
			JComboBox<String> dropbox = new JComboBox<String>(values);
			dropbox.setSelectedIndex(selected);
			cmp = dropbox;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public T Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, SecurityException {
			return clazz.getEnumConstants()[((JComboBox)cmp).getSelectedIndex()];
		}

		@SuppressWarnings("rawtypes")
		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			return ((JComboBox)cmp).getSelectedIndex();
		}
		
		private Class<T> clazz;
	}
	
	static class BooleanField extends FieldEditor<Boolean> {
		protected BooleanField(FieldLocator locator) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			cmp = new JCheckBox();
			((JCheckBox)cmp).setSelected(GetDefaultValue());
		}
	
		@Override
		public Boolean Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, SecurityException {
			return ((JCheckBox)cmp).isSelected();
		}
	
		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			return Convert() ? 1 : 0;
		}
	}
	
	static class CityField extends FieldEditor<Integer> {
		protected CityField(FieldLocator locator, GameData data)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			Init(locator, data, null);
		
		}
		protected CityField(FieldLocator locator, GameData data, int state_index)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			Init(locator, data, i -> i == -1 || data.cities.get(i).owner == state_index);
		}
		private void Init(FieldLocator locator, GameData data, Predicate<Integer> condition)
					throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			
			index = GetDefaultValue();
			JButton button = new JButton(index == -1 ? "" : data.cities.get(index).name);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new CitySelectionDialog(DataEditorFrame.frame, i -> {
						index = i; button.setText(index == -1 ? "" : data.cities.get(index).name);
					}, data, condition);
				}
			});
			cmp = button;
		}

		@Override
		public Integer Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, SecurityException {
			return index;
		}

		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			return index;
		}
		
		private int index;
	}
	
	static class StateField extends FieldEditor<Integer> {
		protected StateField(FieldLocator locator, GameData data) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			Init(locator, data, null);
		}
		protected StateField(FieldLocator locator, GameData data, int state_index)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			Init(locator, data, i -> i != state_index && (i == -1 || data.states.get(i).playable));
		}
		
		private void Init(FieldLocator locator, GameData data, Predicate<Integer> condition)
					throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			index = GetDefaultValue();
			JButton button = new JButton(index == -1 ? "" : StringUtil.IfNull(GameData.const_data.states.get(index).alias, GameData.const_data.states.get(index).name));
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new StateSelectionDialog(DataEditorFrame.frame, i -> {
						index = i; button.setText(
									index == -1 ? "" : StringUtil.IfNull(GameData.const_data.states.get(index).alias, GameData.const_data.states.get(index).name));
					}, condition);
				}
			});
			cmp = button;
		}

		@Override
		public Integer Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, SecurityException {
			return index;
		}

		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			return index;
		}
		
		private int index;
	}
	
	static class RaceField extends FieldEditor<Integer> {
		protected RaceField(FieldLocator locator) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);

			index = GetDefaultValue();
			JButton button = new JButton(index == -1 ? "" : GameData.const_data.races.get(index).name);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new RaceSelectionDialog(DataEditorFrame.frame, i -> {
						index = i; button.setText(index == -1 ? "" : GameData.const_data.races.get(index).name);
					});
				}
			});
			cmp = button;
		}

		@Override
		public Integer Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, SecurityException {
			return index;
		}

		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			return index;
		}
		
		private int index;
	}
	
	static class PersonField extends FieldEditor<Integer> {
		protected PersonField(FieldLocator locator, int person_index) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);

			index = GetDefaultValue();
			JButton button = new JButton(index == -1 ? "" : GameData.const_data.persons.get(index).name);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new PersonSelectionDialog(DataEditorFrame.frame, i -> {
						index = i; button.setText(index == -1 ? "" : GameData.const_data.persons.get(index).name);
					}, i -> i != person_index);
				}
			});
			cmp = button;
		}

		@Override
		public Integer Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, SecurityException {
			return index;
		}

		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			return index;
		}
		
		private int index;
	}
	
	static class UnitField extends FieldEditor<Integer> {
		protected UnitField(FieldLocator locator) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			index = GetDefaultValue();
			JButton button = new JButton(index == -1 ? "" : GameData.const_data.units.get(index).name);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new UnitSelectionDialog(DataEditorFrame.frame, i -> {
						index = i; button.setText(index == -1 ? "" : GameData.const_data.units.get(index).name);
					});
				}
			});
			cmp = button;
		}

		@Override
		public Integer Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, SecurityException {
			return index;
		}

		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			return index;
		}
		
		private int index;
	}
	
	static class TechField extends FieldEditor<Integer> {
		protected TechField(FieldLocator locator, GameData data, int type)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			index = GetDefaultValue();
			JButton button = new JButton(index == -1 ? "" : GameData.const_data.typed_techs.get(TechnologyType.values()[type]).get(index).name);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new TechSelectionDialog(DataEditorFrame.frame, i -> {
						index = i; button.setText(index == -1 ? "" : GameData.const_data.typed_techs.get(TechnologyType.values()[type]).get(index).name);
					}, TechnologyType.values()[type], null);
				}
			});
			cmp = button;
		}

		@Override
		public Integer Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, SecurityException {
			return index;
		}

		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			return index;
		}
		
		private int index;
	}
	
	static class ColorField extends FieldEditor<Integer> {
		protected ColorField(FieldLocator locator) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			super(locator);
			index = GetDefaultValue();
			JButton button = new JButton(Texts.capitalSymbol);
			button.setForeground(ColorUtil.kStateForegroundColor[index]);
			button.setBackground(ColorUtil.kStateBackgroundColor[index]);
			button.setOpaque(true);
			button.setBorderPainted(false);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new ColorSelectionDialog(DataEditorFrame.frame, i -> {
						index = i;
						button.setForeground(ColorUtil.kStateForegroundColor[index]);
						button.setBackground(ColorUtil.kStateBackgroundColor[index]);
					});
				}
//					new TechSelectionDialog(DataEditorFrame.frame, i -> {
//						index = i; button.setText(index == -1 ? "" : GameData.const_data.typed_techs.get(TechnologyType.values()[type]).get(index).name);
//					}, TechnologyType.values()[type], null);
				
			});
			cmp = button;
		}

		@Override
		public Integer Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, SecurityException {
			return index;
		}

		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			return index;
		}
		
		private int index;
	}
	
	static class RacePopulationField extends FieldEditor<CityData.RacePopulation> {
		protected RacePopulationField(FieldLocator locator, GameData data)
				throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, ClassNotFoundException, NoSuchMethodException {
			super(locator);
			race_pop = GetDefaultValue();
			if (race_pop == null) {
				race_pop = new CityData.RacePopulation();
				race_pop.race = -1;
				race_pop.ratio = 0;
			}
			race = new StateField(FieldParser.LocateField(race_pop, FieldParser.Parse("race")), data);
			ratio = new NumericField<Double>(FieldParser.LocateField(race_pop, FieldParser.Parse("ratio")), Double.class);
			cmp = new JPanel();
			cmp.setLayout(null);
			race.GetComponent().setBounds(0, 0, 38, 18);
			ratio.GetComponent().setBounds(40, 0, 40, 18);
			cmp.add(race.GetComponent());
			cmp.add(ratio.GetComponent());
		}

		@Override
		public CityData.RacePopulation Convert() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, SecurityException {
			race_pop.race = race.Convert();
			race_pop.ratio = ratio.Convert();
			return race_pop.race >= 0 ? race_pop : null;
		}

		@Override
		public long ConvertToNumber() throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException, NoSuchMethodException, SecurityException {
			return race_pop.race;
		}
		
		private CityData.RacePopulation race_pop;
		private FieldEditor<Integer> race;
		private FieldEditor<Double> ratio;
	}
}