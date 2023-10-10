package ac.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import ac.data.StateData.DiplomacyData.StateRelationship;
import ac.data.base.Date;
import ac.data.base.DeterministicRandom;
import ac.data.constant.ConstCityData;
import ac.data.constant.Parameters;
import ac.data.constant.Technology;
import ac.data.constant.Technology.TechnologyType;
import ac.util.MathUtil;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import data.JsonUtil;

public class GameData {
	private GameData() {
		const_data = new ac.data.constant.ConstGameData();
	}

	public static GameData Init(String dir) {
		GameData data = new GameData();
		data = JsonUtil.ParseOneObjectFromJson("scenarios/" + dir + ".data", GameData.class);
		data.Init();
		return data;
	}
	
	private void Init() {
		//player.player = 2;

		persons.clear();
		monarchs.clear();
		for (int i = 0; i < const_data.persons.size(); ++i) {
			PersonData person = new PersonData(const_data.persons.get(i));
			persons.add(person);
			if (person.original_state == 0 && const_data.persons.get(i).city >= 0) {
				person.original_state = cities.get(const_data.persons.get(i).city).owner;
			}
		}
		for (int i = 0; i < const_data.monarchs.size(); ++i) {
			monarchs.add(new PersonData(const_data.monarchs.get(i)));
		}
		config = new ConfigData();
		//random_seed = System.currentTimeMillis();
		random_seed = 0;
		random.SetSeed(random_seed);
		for (int i = 0; i < states.size(); ++i) {
			StateData state = states.get(i);
			if (!state.playable) {
				for (int j = 0; j < state.military.armies.length; ++j) {
					state.military.armies[j] = null;
				}
				continue;
			}
				
			if (state.diplomacy.states == null) continue;
			//for (int j = 0; j < state.diplomacy.states.length; ++j) {
			for (int j = 0; j < state.diplomacy.states.size(); ++j) {
				//StateRelationship rel = state.diplomacy.states[j];
				StateRelationship rel = state.diplomacy.states.get(j);
				if (rel.alliance) {
					rel.ally = true;
					rel.open_border = true;
					if (rel.attitude < params.min_attitude_for_alliance) {
						rel.attitude = params.min_attitude_for_alliance;
					}
				} else if (state.diplomacy.suzerainty_state == j) {
					rel.ally = true;
					rel.open_border = true;
					if (rel.attitude < params.min_attitude_for_open_border) {
						rel.attitude = params.min_attitude_for_open_border;
					}
					//rel = states.get(j).diplomacy.states[i];
					rel = states.get(j).diplomacy.states.get(j);
					rel.ally = true;
					rel.open_border = true;
					if (rel.attitude < params.min_attitude_for_open_border) {
						rel.attitude = params.min_attitude_for_open_border;
					}
				} else if (rel.open_border) {
					rel.ally = true;
					if (rel.attitude < params.min_attitude_for_open_border) {
						rel.attitude = params.min_attitude_for_open_border;
					}
				} else if (rel.ally) {
					if (rel.attitude < params.min_attitude_for_ally) {
						rel.attitude = params.min_attitude_for_ally;
					}
				}
			}
			HashMap<Integer, Double> city_weights = new HashMap<Integer, Double>();
			long total_pop = 0L;
			for (int j = 0; j < cities.size(); ++j) {
				CityData city = cities.get(j);
				if (city.owner == i) {
					city_weights.put(j, (double) city.population);
					total_pop += city.population;
				}
			}
			for (ArmyData army : state.military.armies) {
				if (army == null) continue;
				army.city_conscriptions = new TreeMap<Integer, Double>();
				for (Entry<Integer, Double> entry : city_weights.entrySet()) {
					army.city_conscriptions.put(entry.getKey(), entry.getValue() / total_pop);
				}
				for (int j = 0; j < Unit.kMaxUnitType; ++j) {
					long soldiers = 0L;
					for (int k = 0; k < ArmyData.kMaxSoldierTypes; ++k) {
						soldiers += army.typed_soldier_quantities[j][k];
					}
					if (soldiers <= 0) continue;
					army.typed_unit_quantities[j].unit_quantities.put(FindMostAdvancedUnit(i, j).index, soldiers);
				}
			}
		}
		for (int i = 0; i < cities.size(); ++i) {
			CityData city = cities.get(i);
			for (int j = 0; j < city.profression_target_pct.length; ++j) {
				city.profression_ratio[j] = (double)city.profression_target_pct[j] / 100;
			}
			city.remaining_food = city.population * 10 / 12;
			city.military.garrison_max =  MathUtil.FlooredByHundred((long) (city.population * ConstCityData.kGarrisonRatio));
			int archery_id = UnitType.ARCHERY.ordinal();
			city.military.garrison.typed_unit_quantities[archery_id].unit_quantities.clear();
			city.military.garrison.typed_unit_quantities[archery_id].unit_quantities.put(FindMostAdvancedUnit(city.owner, archery_id).index, city.military.garrison_max);
		}
	}
	
	private Unit FindMostAdvancedUnit(int state_id, int type_id) {
		int max_military_tech = FindStateMilitaryTech(state_id);
		ArrayList<Unit> units = const_data.typed_units.get(UnitType.ARCHERY.ordinal());
		for (int k = units.size() - 1; k >= 0; --k) {
			Technology tech = const_data.unit_techs.get(units.get(k));
			if (tech == null || tech.index <= max_military_tech) {
				return units.get(k);
			}
		}
		return units.get(0);
	}
	
	private int FindStateMilitaryTech(int state_id) {
		if (state_id <= 0) {
			ArrayList<Technology> techs = const_data.typed_techs.get(TechnologyType.MILITARY);
			for (int i = techs.size() - 1; i >= 0; --i) {
				if (techs.get(i).year < date.GetYear() - 100) return techs.get(i).index;
			}
		}
		return states.get(state_id).technologies.obtained[TechnologyType.MILITARY.ordinal()];
	}
		
	
	public static ac.data.constant.ConstGameData const_data;
	public Date date;
	public ArrayList<CityData> cities = new ArrayList<CityData>();
	public ArrayList<StateData> states = new ArrayList<StateData>();
	public ArrayList<PersonData> persons = new ArrayList<PersonData>();
	public ArrayList<PersonData> monarchs = new ArrayList<PersonData>();
	public ArrayList<TreatyData> treaties = new ArrayList<TreatyData>();
	public int next_treat_id = 1;
	public MarketData market = new MarketData();
	public transient OverrideData override = new OverrideData();
	
	public ConfigData config;
	public transient Parameters params = new Parameters();
	public DeterministicRandom random = new DeterministicRandom();
	public long random_seed;
	public PlayerData player = new PlayerData();
}