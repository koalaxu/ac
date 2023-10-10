package ac.data.constant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import ac.data.ArmyData.SoldierType;
import ac.data.base.Position;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Improvement.SpecialImprovementType;
import ac.data.constant.Policies.Policy;
import ac.data.constant.Technology.TechnologyType;
import ac.data.constant.Unit.UnitType;
import ac.util.TileUtil;
import data.JsonUtil;

public class ConstGameData {
	public ConstGameData() {
		JsonUtil.ParseArrayFromJson("const/tiles.json", tiles, Tile.class, true);
		JsonUtil.ParseArrayFromJson("const/cities.json", cities, ConstCityData.class, true);
		JsonUtil.ParseArrayFromJson("const/states.json", states, ConstStateData.class, true);
		JsonUtil.ParseArrayFromJson("const/races.json", races, ConstRaceData.class, true);
		JsonUtil.ParseArrayFromJson("const/persons.json", persons, ConstPersonData.class, true);
		JsonUtil.ParseArrayFromJson("const/monarch.json", monarchs, ConstPersonData.class, true);
		JsonUtil.ParseArrayFromJson("const/units.json", units, Unit.class, true);
		JsonUtil.ParseArrayFromJson("const/tech.json", techs, Technology.class, true);
		BuildCityMap();
		BuildTypedUnitMap();
		PreprocessTechnologies();
	}
	
	public Tile GetTile(Position coordinate) {
		if (coordinate == null) return null;
		int index = coordinate.x * kMapHeight + coordinate.y;
		if (index < 0 || index > tiles.size()) return null;
		return tiles.get(index);
	}
	
	public int GetUnitIndex(Unit unit) {
		return unit_indices.get(unit);
	}
	
	public int GetTechIndex(Technology tech) {
		return tech_indices.get(tech);
	}
	
	private void BuildCityMap() {
		for (ConstCityData city : cities) {
			BFSCity(city);
		}
	}
	
	private void BFSCity(ConstCityData city) {
		HashSet<Position> visited_tiles = new HashSet<Position>();
		Queue<Position> queue = new LinkedList<Position>();
		queue.add(city.coordinate);
		int current_index = GetTile(city.coordinate).county;
		
		while(!queue.isEmpty()) {
			Position pos = queue.poll();
			ArrayList<Position> neighbors = TileUtil.GetNeighborPositions(pos);
			for (Position neighbor : neighbors) {
				if (visited_tiles.contains(neighbor)) continue;
				visited_tiles.add(neighbor);
				int index = GetTile(neighbor).county;
				if (index != current_index) {
					if (index > 0) {
						double distance = TileUtil.Distance(city.coordinate, cities.get(index - 1).coordinate);
						cities.get(current_index - 1).neighbor_cities.put(index - 1, distance);
						cities.get(index - 1).neighbor_cities.put(current_index - 1, distance);
					}
				} else {
					queue.add(neighbor);
				}
			}
		}
	}
	
	private void BuildTypedUnitMap() {
		for (@SuppressWarnings("unused") UnitType type : UnitType.values()) {
			typed_units.add(new ArrayList<Unit>());
		}
		for (int i = 0; i < units.size(); ++i) {
			Unit unit = units.get(i);
			unit.index = typed_units.get(unit.type.ordinal()).size();
			typed_units.get(unit.type.ordinal()).add(unit);
			unit_indices.put(unit, i);
		}
	}
	
	private void PreprocessTechnologies() {
		for (TechnologyType type : TechnologyType.values()) {
			typed_techs.put(type, new ArrayList<Technology>());
		}
		for (int i = 0; i < techs.size(); ++i) {
			Technology tech = techs.get(i);
			// tech.index = typed_techs.get(tech.type).size();
			typed_techs.get(tech.type).add(tech);
			if (tech.agriculture_boost > 0) {
				agriculture_boost_techs.add(tech);
			}
			if (tech.commerce_boost > 0) {
				commerce_boost_techs.add(tech);
			}
			if (tech.tax_boost > 0) {
				tax_boost_techs.add(tech);
			}
			if (tech.county_bonus > 0) {
				county_boost_techs.add(tech);
			}
			if (tech.improvement != null) {
				improvement_techs.put(tech.improvement, tech);
			}
			if (tech.effect == Technology.Effect.UNBLOCK_AQEDUCT_SPECIAL_IMPROVEMENT) {
				for (SpecialImprovementType type : Improvement.kAqeductSpecialImprovements) {
					special_improvement_techs.put(type, tech);
				}
			} else if (tech.effect == Technology.Effect.UNBLOCK_GREATWALL) {
				special_improvement_techs.put(SpecialImprovementType.GREATWALL, tech);
			} else if (tech.effect != Technology.Effect.NONE) {
				tech_effects.put(tech.effect, tech);
			}
			if (tech.policy != null) {
				policy_techs.put(tech.policy, tech);
			}
			if (tech.ideology != null) {
				ideology_techs.put(tech.ideology, tech);
			}
			if (tech.unit >= 0) {
				unit_techs.put(units.get(tech.unit), tech);
			}
			if (tech.fubing > 0) {
				soldier_type_techs.get(SoldierType.FUBING).add(tech);
			}
			if (tech.recruitment > 0) {
				soldier_type_techs.get(SoldierType.RECRUITMENT).add(tech);
			}
			tech_indices.put(tech, i);
		}
		for (TechnologyType type : TechnologyType.values()) {
			typed_techs.get(type).sort(new Comparator<Technology>() {
				@Override
				public int compare(Technology o1, Technology o2) {
					return o1.index - o2.index;
				}
			});
		}
	}

	public ArrayList<Tile> tiles = new ArrayList<Tile>();
	public ArrayList<ConstCityData> cities = new ArrayList<ConstCityData>();
	public ArrayList<ConstStateData> states = new ArrayList<ConstStateData>();
	public ArrayList<ConstRaceData> races = new ArrayList<ConstRaceData>();
	public ArrayList<ConstPersonData> persons = new ArrayList<ConstPersonData>();
	public ArrayList<ConstPersonData> monarchs = new ArrayList<ConstPersonData>();
	public ArrayList<Unit> units = new ArrayList<Unit>();
	public ArrayList<ArrayList<Unit>> typed_units = new ArrayList<ArrayList<Unit>>();
	public ArrayList<Technology> techs = new ArrayList<Technology>();
	public HashMap<TechnologyType, ArrayList<Technology>> typed_techs = new HashMap<TechnologyType, ArrayList<Technology>>();
	public HashSet<Technology> agriculture_boost_techs = new HashSet<Technology>();
	public HashSet<Technology> commerce_boost_techs = new HashSet<Technology>();
	public HashSet<Technology> tax_boost_techs = new HashSet<Technology>();
	public HashSet<Technology> county_boost_techs = new HashSet<Technology>();
	public HashMap<ImprovementType, Technology> improvement_techs = new HashMap<ImprovementType, Technology>();
	public HashMap<SpecialImprovementType, Technology> special_improvement_techs = new HashMap<SpecialImprovementType, Technology>();
	public HashMap<Policy, Technology> policy_techs = new HashMap<Policy, Technology>();
	public HashMap<Ideology, Technology> ideology_techs = new HashMap<Ideology, Technology>();
	public HashMap<Unit, Technology> unit_techs = new HashMap<Unit, Technology>();
	public HashMap<SoldierType, HashSet<Technology>> soldier_type_techs = new HashMap<SoldierType, HashSet<Technology>>() {
		private static final long serialVersionUID = 1L; {
		put(SoldierType.FUBING, new HashSet<Technology>()); put(SoldierType.RECRUITMENT, new HashSet<Technology>());
	}};
	public HashMap<Technology.Effect, Technology> tech_effects = new HashMap<Technology.Effect, Technology>();
	
	
	public static int kMapWidth = 64;
	public static int kMapHeight = 56;
	
	private HashMap<Unit, Integer> unit_indices = new HashMap<Unit, Integer>();
	private HashMap<Technology, Integer> tech_indices = new HashMap<Technology, Integer>();
}
