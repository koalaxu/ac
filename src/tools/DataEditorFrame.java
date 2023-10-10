package ac.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import ac.data.CityData;
import ac.data.GameData;
import ac.data.StateData;
import ac.data.base.Date;
import ac.data.base.Position;
import ac.data.constant.ConstCityData;
import ac.data.constant.ConstPersonData;
import ac.data.constant.ConstRaceData;
import ac.data.constant.ConstStateData;
import ac.data.constant.Improvement;
import ac.data.constant.Texts;
import ac.data.constant.Technology;
import ac.data.constant.Unit;
import ac.tools.FieldParser.FieldInfo;
import ac.tools.FieldParser.FieldLocator;
import ac.util.StringUtil;
import data.FileUtil;
import data.JsonUtil;

public class DataEditorFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	public DataEditorFrame() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		FileUtil.Init();
		//data = JsonUtil.ParseOneObjectFromJson("scenarios/default.data", GameData.class);
		data = JsonUtil.ParseOneObjectFromJson("scenarios/06_sg.data", GameData.class);
		
        setSize(kWidth, kHeight);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        
        tabbed_panel.setBounds(0, 30, kWidth, kHeight - 30);
        add(tabbed_panel);
        
        JButton save = new JButton("Save");
        save.setBounds(10, 10, 60, 20);
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	            JFileChooser fc = new JFileChooser();
	            String base_path = FileUtil.GetBasePath();
	            fc.setCurrentDirectory(new File(base_path  + "/scenarios"));
	            int val = fc.showSaveDialog(null);
	            if (val == JFileChooser.APPROVE_OPTION) {
	            	String filename = fc.getSelectedFile().toString();
	            	if (!filename.startsWith(base_path)) return;
	            	filename = filename.substring(base_path.length());
	            	if (!FileUtil.CheckExistence(filename) || (JOptionPane.showConfirmDialog(null,
	            			filename + " exits. Do you want to override?", "Connfirm",
	            			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)) {
	            		JsonUtil.WriteOneObjectToJson(filename, data);
	            	}
	            }
			}
		});
		add(save);
        JButton load = new JButton("Load");
        load.setBounds(80, 10, 60, 20);
        load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
	            JFileChooser fc = new JFileChooser();
	            String base_path = FileUtil.GetBasePath();
	            fc.setCurrentDirectory(new File(base_path));
	            int val = fc.showOpenDialog(null);
	            if (val == JFileChooser.APPROVE_OPTION) {
	            	String filename = fc.getSelectedFile().toString();
	            	if (!filename.startsWith(base_path)) return;
	            	filename = filename.substring(base_path.length());
	            	data = JsonUtil.ParseOneObjectFromJson(filename, GameData.class);
	            	// Override here
//	            	for (int i = 0; i < data.cities.size(); ++i) {
//	            		if (data.cities.get(i).improvements.auxiliary_improvements[1] != 10) 
//	            		data.cities.get(i).improvements.auxiliary_improvements[1] = data.cities.get(i).is_county ? 10 : 8;
//	            		data.cities.get(i).race_dist.clear();
//	            		RacePopulation race_pop = new RacePopulation();
//	            		race_pop.race = 78;
//	            		race_pop.ratio = 0.3;
//	            		data.cities.get(i).race_dist.add(race_pop);
//	            	}
	            	
	            	Init();
	            }
			}
		});
		add(load);
        
        //Toolkit.getDefaultToolkit().addAWTEventListener(panel, AWTEvent.KEY_EVENT_MASK);  
        setVisible(true);
        
        Init();
	}
	
	private void Init() {
		tabbed_panel.removeAll();
		try {
			ListEditPanel<StateData> states = new ListEditPanel<StateData>(state_list_fields, data.states, null,
					i -> StringUtil.IfNull(GameData.const_data.states.get(i).alias, GameData.const_data.states.get(i).name), field_to_names, data, StateData.class, null);
			ListEditPanel<CityData> cities = new ListEditPanel<CityData>(city_list_fields, data.cities, city_id -> GetCityDescription(city_id),
					i -> GameData.const_data.cities.get(i).name, field_to_names, data, null, null);
			ListEditPanel<ConstCityData> const_cities = new ListEditPanel<ConstCityData>(const_city_list_fields, GameData.const_data.cities, null,
					i -> String.valueOf(i), field_to_names, data, null,
					() -> JsonUtil.WriteArrayToJson("const/cities.json", GameData.const_data.cities, ConstCityData.class, true));
			ListEditPanel<ConstStateData> const_states = new ListEditPanel<ConstStateData>(const_state_list_fields, GameData.const_data.states, null,
					i -> String.valueOf(i), field_to_names, data, ConstStateData.class,
					() -> JsonUtil.WriteArrayToJson("const/states.json", GameData.const_data.states, ConstStateData.class, true));
			ListEditPanel<ConstRaceData> const_races = new ListEditPanel<ConstRaceData>(const_race_list_fields, GameData.const_data.races, null,
					i -> String.valueOf(i), field_to_names, data, ConstRaceData.class,
					() -> JsonUtil.WriteArrayToJson("const/races.json", GameData.const_data.races, ConstRaceData.class, true));
			ListEditPanel<ConstPersonData> const_persons = new ListEditPanel<ConstPersonData>(const_person_list_fields, GameData.const_data.persons, null,
					i -> String.valueOf(i), field_to_names, data, ConstPersonData.class,
					() -> JsonUtil.WriteArrayToJson("const/persons.json", GameData.const_data.persons, ConstPersonData.class, true));
			ListEditPanel<ConstPersonData> const_monarchs = new ListEditPanel<ConstPersonData>(const_person_list_fields, GameData.const_data.monarchs, null,
					i -> String.valueOf(i), field_to_names, data, ConstPersonData.class,
					() -> JsonUtil.WriteArrayToJson("const/monarch.json", GameData.const_data.monarchs, ConstPersonData.class, true));
			ListEditPanel<Unit> units = new ListEditPanel<Unit>(unit_list_fields, GameData.const_data.units, null, i -> GameData.const_data.units.get(i).name,
					field_to_names, data, Unit.class,
					() -> JsonUtil.WriteArrayToJson("const/units.json", GameData.const_data.units, Unit.class, true));
			ListEditPanel<Technology> techs = new ListEditPanel<Technology>(tech_list_fields, GameData.const_data.techs, null, i -> String.valueOf(i),
					field_to_names, data, Technology.class,
					() -> JsonUtil.WriteArrayToJson("const/tech.json", GameData.const_data.techs, Technology.class, true));
			tabbed_panel.add("Overall", new EditPanel(overall_fields, field_to_names));
			tabbed_panel.add("State", states);
			tabbed_panel.add("Diplomacy", new DiplomacyEditPanel(data));
			tabbed_panel.add("Army", new ArmyEditPanel(data));
			tabbed_panel.add("City", cities);
			tabbed_panel.add("Const City", const_cities);
			tabbed_panel.add("Const State", const_states);
			tabbed_panel.add("Const Races", const_races);
			tabbed_panel.add("Const Persons", const_persons);
			tabbed_panel.add("Const Monarchs", const_monarchs);
			tabbed_panel.add("Units", units);
			tabbed_panel.add("Tech", techs);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException
					| InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	protected class EditPanel extends GenericEditPanel {
		private static final long serialVersionUID = 1L;
		protected EditPanel(String[] fields, HashMap<String, String> field_to_names) throws
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
			super(null);
			for (int i = 0; i < fields.length; ++i) {
				String field = fields[i];
				JLabel label = new JLabel(field_to_names.getOrDefault(fields[i], fields[i]));
				label.setBounds(5, 30 * i + 5, 100, 20);
				add(label);
				FieldInfo info = FieldParser.Parse(field);
				FieldLocator locator = FieldParser.LocateField(data, info);
				AddFieldEditor(CreateFieldEditor(locator, data, -1), 125, 30 * i + 5, 100, 20, this);
			}
		}
	}
	
	private String GetCityDescription(int i) {
		String desc = "<html>";
		// CityData city = data.cities.get(i);
		String[] fields = { "max_irrigated_farm", "max_farm", "max_pasture", "max_ironmine", "max_salt",
		"max_fish", "max_china", "max_mine", "max_silk" };
		for (String field : fields) {
			desc += field + ": ";
			ConstCityData const_data = GameData.const_data.cities.get(i);
			try {
				desc += ConstCityData.class.getField(field).getInt(const_data);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
			desc += "<br>";
		}
		return desc + "</html>";
	}
	
	static FieldEditor<?> CreateFieldEditor(FieldLocator locator, GameData data, int parent_index)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, ClassNotFoundException, NoSuchMethodException {
		if (locator.output_type.isEnum()) {
			return new FieldEditor.EnumField<>(locator, locator.output_type);
		} else if (locator.output_type == String.class) {
			return new FieldEditor.StringField(locator);
		} else if (locator.output_type == Date.class) {
			return new FieldEditor.DateField(locator);
		} else if (locator.output_type == Position.class) {
			return new FieldEditor.PositionField(locator);
		} else if (locator.output_type == Double.class || locator.output_type == double.class) {
			return new FieldEditor.NumericField<Double>(locator, Double.class);
		} else if (locator.output_type == boolean.class) {
			return new FieldEditor.BooleanField(locator);
		} else if (locator.output_type == long.class) {
			return new FieldEditor.NumericField<Long>(locator, Long.class);
		} else if (locator.output_type == Integer.class || locator.output_type == int.class) {
			if (city_fields.contains(locator.field.getName())) {
				return new FieldEditor.CityField(locator, data);
			}
			if (owner_city_fields.contains(locator.field.getName())) {
				return new FieldEditor.CityField(locator, data, parent_index);
			}
			if (state_fields.contains(locator.field.getName())) {
				return new FieldEditor.StateField(locator, data);
			}
			if (playable_state_fields.contains(locator.field.getName())) {
				return new FieldEditor.StateField(locator, data, parent_index);
			}
			if (race_fields.contains(locator.field.getName())) {
				return new FieldEditor.RaceField(locator);
			}
			if (person_fields.contains(locator.field.getName())) {
				return new FieldEditor.PersonField(locator, parent_index);
			}
			if (unit_fields.contains(locator.field.getName())) {
				return new FieldEditor.UnitField(locator);
			}
			if (locator.field.getName().equals(tech_field)) {
				return new FieldEditor.TechField(locator, data, (Integer)locator.key);
			}
			if (locator.field.getName().equals(color_field)) {
				return new FieldEditor.ColorField(locator);
			}
			return new FieldEditor.NumericField<Integer>(locator, Integer.class);
		} else if (locator.output_type == Number.class) {
			locator.output_type = Long.class;  // This must be resource, override
			return new FieldEditor.NumericField<Long>(locator, Long.class);
		} else if (locator.output_type == CityData.RacePopulation.class) {
			return new FieldEditor.RacePopulationField(locator, data);
		} else {
			System.err.println("Unsupported type: " + locator.output_type);
			System.exit(0);
		}
		return null;
	}
	
	public static void main(String[] args) throws
		NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		FileUtil.Init();
//		GameData data = JsonUtil.ParseOneObjectFromJson("scenarios/default.data", GameData.class);
//		FieldInfo info0 = FieldParser.Parse("date");
//		FieldInfo info1 = FieldParser.Parse("market.price_history[0][83]");
//		FieldInfo info2 = FieldParser.Parse("market.price_history");
//		FieldInfo info3 = FieldParser.Parse("market[0][83]");
		frame = new DataEditorFrame();
		frame.revalidate();
	}
	
	public static DataEditorFrame frame;
	
	private GameData data;
	
	private static int kWidth = 1730;
	static int kHeight = 1080;
	private JTabbedPane tabbed_panel = new JTabbedPane();
	
	private static String[] overall_fields = { "date", "market.price_history[0][83]", "market.price_history[3][83]", "market.price_history[4][83]" };
	private static String[] state_list_fields = { "playable", "capital", "prestige", "stability",
			"economic.food_tax_pct", "economic.food_budget_pct", "economic.horse_budget_pct",
			"economic.iron_budget_pct", "economic.research_budget_pct", "technologies.obtained[0]", "technologies.obtained[1]", "technologies.obtained[2]",
			"ideologies[0]", "ideologies[1]", "ideologies[2]",
			"resources.food", "resources.hammer", "resources.gold", "resources.horse", "resources.iron", "diplomacy.suzerainty_state" , "alias" };
	private static String[] city_list_fields = { "name", "population", "owner", "happiness", "riot", "is_county",
			"profression_target_pct[0]", "profression_target_pct[1]", "profression_target_pct[2]",
			"improvements.agriculture_improvements[0]", "improvements.agriculture_improvements[1]", "improvements.agriculture_improvements[2]",
			"improvements.agriculture_improvements[3]", "improvements.agriculture_improvements[4]",
			"improvements.industry_improvements[0]", "improvements.industry_improvements[1]", "improvements.industry_improvements[2]", "improvements.industry_improvements[3]",
			"improvements.industry_improvements[4]", "improvements.industry_improvements[5]", "improvements.industry_improvements[6]",
			"improvements.auxiliary_improvements[0]", "improvements.auxiliary_improvements[1]", "improvements.auxiliary_improvements[2]",
			"race_dist[0]",
			};
	
	private static String[] const_city_list_fields = { "coordinate", "name", "x", "y", "han_county", "han_name", "tang_county", "tang_name", "current_name",
			"rain", "temperature", "flood", "locust", "barbarian", "max_irrigated_farm", "max_farm", "max_pasture", "max_ironmine", "max_salt",
			"max_fish", "max_china", "max_mine", "max_silk",
			"terrain", "special_improvement",
	};
	
	private static String[] const_state_list_fields = { "name", "official_name", "alias", "nobility", "family_name", "race", "color_index" };
	private static String[] const_race_list_fields = { "name" };
	private static String[] const_person_list_fields = { "name", "city", "state", "military", "administration", "diplomacy",
			"family_name", "surname", "courtesy_name", "given_name", "posthumous_name", "available", "death",
			"birth_city", "father" };	
	
	private static String[] unit_list_fields = { "index", "type", "attack", "defend", "speed_multiplier", "cost.food", "cost.hammer", "cost.gold", "cost.horse", "cost.iron" };
	private static String[] tech_list_fields = { "name", "type", "index", "cost", "year", "effect", "improvement", "policy", "ideology", "unit",
			"agriculture_boost", "commerce_boost", "tax_boost", "county_bonus", "fubing", "recruitment", };
			
	private static HashMap<String, String> field_to_names = new HashMap<String, String>() {private static final long serialVersionUID = 1L; {
		put("market.price_history[0][83]", "Food Price"); put("market.price_history[3][83]", "Horse Price"); put("market.price_history[4][83]", "Iron Price");
		put("economic.food_tax_pct", "Food Tax"); put("economic.food_budget_pct", "Food Budget"); put("economic.horse_budget_pct", "Horse Budget");
		put("economic.iron_budget_pct", "Iron Budget"); put("economic.research_budget_pct", "Tech Budget");
		put("technologies.obtained[0]", "Economic Tech"); put("technologies.obtained[1]", "Military Tech"); put("technologies.obtained[2]", "Civic Tech");
		put("ideologies[0]", "Religion Ideology"); put("ideologies[1]", "Civil Ideology"); put("ideologies[2]", "Military Ideology");
		put("resources.food", "Food"); put("resources.hammer", "Hammer"); put("resources.gold", "Gold"); put("resources.horse", "Horse"); put("resources.iron", "Iron");
		put("diplomacy.suzerainty_state", "suzerainty");
		put("family_name", "姓"); put("surname", "氏"); put("courtesy_name", "字"); put("given_name", "名"); put("posthumous_name", "谥");
		
		put("profression_target_pct[0]", "Worker %"); put("profression_target_pct[1]", "Merchant %"); put("profression_target_pct[2]", "Soldier %");
		for (int i = 0; i < Improvement.kAgricultureImprovements.length; ++i) {
			put(String.format("improvements.agriculture_improvements[%d]", i), Texts.improvements[Improvement.kAgricultureImprovements[i].ordinal()]);
		}
		for (int i = 0; i < Improvement.kIndustryImprovements.length; ++i) {
			put(String.format("improvements.industry_improvements[%d]", i), Texts.industryImprovements[i]);
		}
		for (int i = 0; i < Improvement.kAuxiliaryImprovements.length; ++i) {
			put(String.format("improvements.auxiliary_improvements[%d]", i), Texts.improvements[Improvement.kAuxiliaryImprovements[i].ordinal()]);
		}
	}};
	private static HashSet<String> city_fields = new HashSet<String>(){private static final long serialVersionUID = 1L; {
		add("city"); add("birth_city");
	}};
	private static HashSet<String> owner_city_fields = new HashSet<String>(){private static final long serialVersionUID = 1L; {
		add("capital"); add("base_city");
	}};
	private static HashSet<String> playable_state_fields = new HashSet<String>(){private static final long serialVersionUID = 1L; {
		add("suzerainty_state");
	}};
	private static HashSet<String> state_fields = new HashSet<String>(){private static final long serialVersionUID = 1L; {
		add("owner"); add("state");
	}};
	private static HashSet<String> race_fields = new HashSet<String>(){private static final long serialVersionUID = 1L; {
		add("race");
	}};
	private static HashSet<String> person_fields = new HashSet<String>(){private static final long serialVersionUID = 1L; {
		add("father");
	}};
	private static HashSet<String> unit_fields = new HashSet<String>(){private static final long serialVersionUID = 1L; {
		add("unit");
	}};
	private static final String color_field = "color_index";
	
	
	private static String tech_field = "obtained";
//	private static HashMap<String, AbilityType> tech_fields = new HashMap<String, AbilityType>(){private static final long serialVersionUID = 1L; {
//		put("technologies.obtained[0]", AbilityType.MILITARY); put("technologies.obtained[1]", AbilityType.ADMIN); put("technologies.obtained[2]", AbilityType.DIPLOMACY);
//	}};
}
