package ac.engine.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import ac.data.StateData;
import ac.data.base.Pair;
import ac.data.base.Resource;
import ac.data.constant.ConstStateData;
import ac.data.constant.Role;
import ac.data.constant.Role.RoleType;
import ac.util.StringUtil;

public class State extends IdKeyedData {
	protected State(DataAccessor accessor, int id, StateData data, ConstStateData const_data) {
		super(accessor, id);
		this.data = data;
		this.const_data = const_data;
		policy = new StatePolicy(accessor, this, data.policies);
		economic = new StateEconomic(accessor, data.economic);
		military = new StateMilitary(accessor, this, data.military);
		technology = new StateTechnology(accessor, data.technologies);
		diplomacy = new StateDiplomacy(accessor, this, data.diplomacy);
		description = new StateDescription(accessor, const_data);
	}
	
	@Override
	public String GetName() {
		return StringUtil.IfNull(data.alias, const_data.name);
	}
	
	// Getters
	public StateData Get() {
		return data;
	}
	
	public int ColorIndex() {
		return data.playable ? const_data.color_index : 0;
	}
	
	public Resource<Long> GetResource() {
		return data.resources;
	}
	
	public boolean Playable() {
		return data.playable;
	}
	
	public StateDescription GetDescription() {
		return description;
	}
	
	public StateEconomic GetEconomic() {
		return economic;
	}
	
	public StateMilitary GetMilitary() {
		return military;
	}
	
	public StateTechnology GetTechnology() {
		return technology;
	}
	
	public StateDiplomacy GetDiplomacy() {
		return diplomacy;
	}
	
	public StatePolicy GetPolicy() {
		return policy;
	}
	
	public City GetCapital() {
		if (!Playable()) return null;
		return accessor.cities.get(data.capital);
	}
	
	public Monarch GetKing() {
//		if (data.king > 0) return accessor.monarchs.get(data.king);
//		return null;
		return king;
	}
	
	public Person GetOfficer(RoleType type) {
		return ministers[type.ordinal() - Role.kMinisters[0].ordinal()];
	}
	
	public void SetOfficer(RoleType type, Person person) {
		if (type == null || type == RoleType.NONE || type == RoleType.KING) return;
		ministers[type.ordinal() - Role.kMinisters[0].ordinal()] = person;
		if (person != null) person.SetRole(type);
	}
	
	public void SetKing(Monarch king) {
		this.king = king;
	}
	
	public void AddPerson(Person person) {
		//RoleType type = person.GetRole().GetRoleType();
		persons.add(person);
		//ministers[type.ordinal() - Role.kMinisters[0].ordinal()] = person;
	}
	
	public void RemovePerson(Person person) {
		persons.remove(person);
	}
	
	public Collection<City> GetOwnedCities() {
		return cities;
	}
	
	public Collection<State> GetNeighborStates() {
		return neighbors;
	}
	
	public Collection<Monarch> GetHistoricalMonarchs() {
		return accessor.sorted_real_monarchs.Get(this);
	}
	
	public Collection<Person> GetPersons() {
		return persons;
	}
	
	// Setters
	public void UpdateNeighbors() {
		// Update neighbor state
		neighbors.clear();
		for (City city : GetOwnedCities()) {
			for (Pair<City, Double> neighbor : city.GetDescription().GetNeighbors()) {
				State neighbor_state = neighbor.first.GetOwner();
				if (neighbor_state != this) neighbors.add(neighbor_state);
			}
		}
		// Update path to capital
		city_paths.clear();
		for (City city : GetOwnedCities()) {
			city_paths.put(city, Integer.MAX_VALUE);
		}
		accessor.GetUtils().trans_util.FindShortestPaths(GetCapital(), city_paths);
	}
	
	public void RemoveCity(City city) {
		cities.remove(city);
	}
	
	public void AddCity(City city) {
		cities.add(city);
	}
	
	public void SetCapital(City city) {
		data.capital = city.id;
	}
	
	private StateData data;
	private ConstStateData const_data;
	private StatePolicy policy;
	private StateEconomic economic;
	private StateMilitary military;
	private StateTechnology technology;
	private StateDiplomacy diplomacy;
	private StateDescription description;
	private ArrayList<City> cities = new ArrayList<City>();
	
	private Monarch king;
	private Person[] ministers = new Person[Role.kMinisters.length];
	private ArrayList<Person> persons = new ArrayList<Person>();
	
	private HashSet<State> neighbors = new HashSet<State>();
	protected HashMap<City, Integer> city_paths = new HashMap<City, Integer>();
}
