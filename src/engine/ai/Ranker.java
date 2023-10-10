package ac.engine.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import ac.data.ArmyData.SoldierType;
import ac.data.CityData.Profession;
import ac.data.base.Position;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Improvement.ImprovementType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.CityImprovements;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.engine.util.CityUtil;
import ac.engine.util.StateUtil;
import ac.engine.util.TransportationUtil;
import ac.engine.util.TransportationUtil.Path;

public class Ranker {
	// City comparators
	public static Comparator<City> city_unhappiness = new Comparator<City>() {
		@Override
		public int compare(City o1, City o2) {
			return o1.GetPopulation().GetHappiness() - o2.GetPopulation().GetHappiness();
		}
	};
	public static Comparator<City> city_populations = new Comparator<City>() {
		@Override
		public int compare(City o1, City o2) {
			return (int) (o2.Get().population - o1.Get().population);
		}
	};
	public static Comparator<City> city_foreigners = new Comparator<City>() {
		@Override
		public int compare(City o1, City o2) {
			if (o1.GetPopulation().GetPopRatio(0) < o2.GetPopulation().GetPopRatio(0)) return -1;
			if (o1.GetPopulation().GetPopRatio(0) > o2.GetPopulation().GetPopRatio(0)) return 1;
			return 0;
		}
	};
	public static Comparator<City> city_riot_points = new Comparator<City>() {
		@Override
		public int compare(City o1, City o2) {
			return o2.Get().riot - o1.Get().riot;
		}
	};
	public static class CityAvailablePeasantComparator implements Comparator<City> {
		public CityAvailablePeasantComparator(CityUtil city_util) {
			this.city_util = city_util;
		}

		@Override
		public int compare(City o1, City o2) {
			return AvailablePeasant(o1) - AvailablePeasant(o2);
		}
		
		private int AvailablePeasant(City city) {
			CityImprovements impr = city.GetImprovements();
			int farm = impr.GetCount(ImprovementType.FARM);
			int aqeducted_farm = impr.GetCount(ImprovementType.AQEDUCTED_FARM);
			int irrigated_farm = impr.GetCount(ImprovementType.IRRIGATED_FARM);
			int total_farm = farm + aqeducted_farm + irrigated_farm;
			long total_peasant = city_util.GetProfessionalPopulation(city, Profession.PEASANT);
			return total_farm > 0 ? (int)(total_peasant / total_farm) : (int)city.GetTotalPopulation();
		}
		
		private CityUtil city_util;
	};
	
	// State comparators
	public static Comparator<State> GetStateHostileComparator(State state) {
		state_hostile.SetState(state);
		return state_hostile;
	}
	
	public static class StateHostileComparator implements Comparator<State> {
		public void SetState(State state) {
			this.state = state;
		}
		@Override
		public int compare(State o1, State o2) {
			if (o1 == state) return 1;
			if (o2 == state) return -1;
 			return state.GetDiplomacy().GetAttitude(o1) - state.GetDiplomacy().GetAttitude(o2);
		}
		private State state;
	}
	private static StateHostileComparator state_hostile = new StateHostileComparator();
	
	// Advanced State comparators
	public static class EnemyComparator implements Comparator<State> {
		public EnemyComparator(State state) {
			HashSet<City> neighbors = new HashSet<City>();
			for (City city : state.GetOwnedCities()) {
				for (City neighbor : city.GetNeighbors()) {
					if (neighbor.GetOwner() != null && neighbor.GetOwner() != state) {
						neighbors.add(neighbor);
					}
				}
			}
			for (City neighbor : neighbors) {
				if (!neighbor.GetOwner().Playable()) continue;
				state_threat.put(neighbor.GetOwner(), GetThreat(neighbor.GetOwner()) + neighbor.GetTotalPopulation());
			}
		}
		@Override
		public int compare(State o1, State o2) {
			return (int) (GetThreat(o2) - GetThreat(o1));
		}
		public Collection<State> GetCandidates() {
			return state_threat.keySet();
		}
		public long GetThreat(State state) {
			return state_threat.getOrDefault(state, 0L);
		}
		private HashMap<State, Long> state_threat = new HashMap<State, Long>();
	}
	
	public static class FriendComparator implements Comparator<State> {
		public FriendComparator(State state, ArrayList<State> enemies, StateUtil state_util) {
			this.state_util = state_util;
			neighbors.addAll(state.GetNeighborStates());
			for (State enemy : enemies) {
				enemy_neighbors.addAll(enemy.GetNeighborStates());
			}
		}
		@Override
		public int compare(State o1, State o2) {
			if (neighbors.contains(o1) == neighbors.contains(o2)) {
				if (enemy_neighbors.contains(o1) == enemy_neighbors.contains(o2)) {
					return (int) (state_util.GetTotalPopulation(o2) - state_util.GetTotalPopulation(o1));
				}
				return enemy_neighbors.contains(o1) ? -1 : 1;
			}
			return neighbors.contains(o1) ? -1 : 1;
		}
		private StateUtil state_util;
		private HashSet<State> neighbors = new HashSet<State>();
		private HashSet<State> enemy_neighbors = new HashSet<State>();
	}
	
	// Army Comparators
	public static Comparator<Army> army_soldier = new Comparator<Army>() {
		@Override
		public int compare(Army o1, Army o2) {
			return (int) (o1.GetTotalSoldier() - o2.GetTotalSoldier());
		}
	};
	
	public static class ArmyTypedSoldierComparator implements Comparator<Army> {
		public ArmyTypedSoldierComparator(SoldierType type) {
			this.type = type;
		}
		@Override
		public int compare(Army o1, Army o2) {
			return (int) (o2.GetTypedSoldier(type) - o1.GetTypedSoldier(type));
		}
		private SoldierType type;
	}
	public static Comparator<Army> army_recruitment = new ArmyTypedSoldierComparator(SoldierType.RECRUITMENT);
	public static Comparator<Army> army_fubing = new ArmyTypedSoldierComparator(SoldierType.FUBING);
	public static Comparator<Army> army_conscription = new ArmyTypedSoldierComparator(SoldierType.CONSCRIPTION);
	public static HashMap<SoldierType, Comparator<Army>> army_soldier_types = new HashMap<SoldierType, Comparator<Army>>(){
		private static final long serialVersionUID = 1L; {
		put(SoldierType.CONSCRIPTION, army_conscription); put(SoldierType.FUBING, army_fubing); put(SoldierType.RECRUITMENT, army_recruitment);
	}};
	
	public static class ArmyDistanceComparator implements Comparator<Army> {
		public ArmyDistanceComparator(Position dest, TransportationUtil util) {
			this.dest = dest;
			this.util = util;
		}
		@Override
		public int compare(Army o1, Army o2) {
			Path p1 = util.FindShortestPath(o1, dest);
			Path p2 = util.FindShortestPath(o2, dest);
			if (p1 == null) {
				return p2 == null ? 0 : 1;
			}
			if (p2 == null) return -1;
			return p1.shortest_time - p2.shortest_time;
		}
		private Position dest;
		private TransportationUtil util;
	}
	
	// Person Comparators
	public static class PersonAbilityComparator implements Comparator<Person> {
		public PersonAbilityComparator(AbilityType type) {
			this.type = type;
		}
		@Override
		public int compare(Person o1, Person o2) {
			return o2.GetAbility(type) - o1.GetAbility(type);
		}
		private AbilityType type;
	}
	public static Comparator<Person> person_military_ability = new PersonAbilityComparator(AbilityType.MILITARY);
	public static Comparator<Person> person_admin_ability = new PersonAbilityComparator(AbilityType.ADMIN);
	public static Comparator<Person> person_diplomacy_ability = new PersonAbilityComparator(AbilityType.DIPLOMACY);
	public static HashMap<AbilityType, Comparator<Person>> person_abilities = new HashMap<AbilityType, Comparator<Person>>() {
		private static final long serialVersionUID = 1L; {
			put(AbilityType.MILITARY, person_military_ability); put(AbilityType.ADMIN, person_admin_ability);
			put(AbilityType.DIPLOMACY, person_diplomacy_ability);
	}};
	
	public static Comparator<Person> person_ability = new Comparator<Person>() {

		@Override
		public int compare(Person o1, Person o2) {
			return GetTotalAbility(o2) - GetTotalAbility(o1);
		}
		private int GetTotalAbility(Person p) {
			int total = 0;
			for (AbilityType type : AbilityType.values()) {
				total += p.GetAbility(type);
			}
			return total;
		}
	};
}
