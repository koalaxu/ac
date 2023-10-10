package ac.engine.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import ac.data.ArmyData;
import ac.data.ConfigData;
import ac.data.GameData;
import ac.data.PersonData;
import ac.data.TreatyData;
import ac.data.TreatyData.Relationship;
import ac.data.base.Date;
import ac.data.base.MultiMap;
import ac.data.base.Pair;
import ac.data.base.Position;
import ac.data.constant.ConstGameData;
import ac.data.constant.Parameters;
import ac.data.constant.Role.RoleType;
import ac.data.constant.Tile;
import ac.engine.util.Utils;
import data.FileUtil;
import data.JsonUtil;

public class DataAccessor {
	public DataAccessor(GameData data) {
		this.data = data;
		utils = new Utils(this);
		
		Init();
	}
	
	// Getters
	public Utils GetUtils() {
		return utils;
	}
	
	public Date GetDate() {
		return data.date;
	}
	
	public ConfigData GetConfig() {
		return data.config;
	}
	
	public void SetConfig(ConfigData config) {
		data.config = config;
	}
	
	public Parameters GetParam() {
		return data.params;
	}
	
	public Player GetPlayer() {
		return player;
	}
	
	public ArrayList<City> GetAllCities() {
		return cities;
	}
	
	public ArrayList<State> GetAllStates() {
		return states;
	}
	
	public Collection<Person> GetAllPersons() {
		return persons.subList(1, persons.size());
	}
	
	public Collection<Monarch> GetAllMornach() {
		return monarchs.subList(1, monarchs.size());
	}
	
	public DataCollection<City> GetAllPlayableCities() {
		return playable_cities;
	}
	
	public DataCollection<State> GetAllPlayableStates() {
		return playable_states;
	}
	
	public ArrayList<Army> GetAllArmies() {
		return armies;
	}
	
	public DataCollection<Army> GetAllPlayableArmies() {
		return playable_armies;
	}
	
	public ArrayList<Treaty> GetAllTreaties() {
		return treaties;
	}
	
	public Market GetMarket() {
		return market;
	}
	
	public Messages GetMessages() {
		return messages;
	}
	
	public void AddMessage(String text) {
		messages.AddMessage(text);
	}
	
	public City GetCityTerritoryByTile(Tile tile) {
		return tile == null || tile.county <= 0 ? null : cities.get(tile.county - 1);
	}
	
	public ArrayList<Army> GetArmiesByPosition(Position pos) {
		return army_positions.Get(pos);
	}
	
	protected State GetState(int index) {
		return index <= 0 ? states.get(0) : states.get(index);
	}
	
	protected Army CreateArmy(State state, int index, ArmyData data) {
		Army army = new Army(this, state, index, data);
		armies.add(army);
		army_positions.Insert(army.GetPosition(), army);
		return army;
	}
	
	public Person CreateNewPerson(Date death_date) {
		PersonData person_data = new PersonData();
		Person person = new Person(this, persons.size(), person_data, null);
		person.SetDeathTime(death_date);
		data.persons.add(person_data);
		persons.add(person);
		death_chronicle.Insert(person);
		return person;
	}
	
	public Monarch CreateNewMonarch(Date death_date) {
		PersonData person_data = new PersonData();
		Monarch monarch = new Monarch(this, monarchs.size(), person_data, null);
		monarch.SetDeathTime(death_date);
		data.monarchs.add(person_data);
		monarchs.add(monarch);
		person_data.role_type = RoleType.KING;
		death_chronicle.Insert(monarch);
		return monarch;
	}
	
	public Treaty CreateNewTreaty(State state, State target_state, Relationship proposed_relation, Date expire_date, boolean rejection_leads_to_unstable) {
		TreatyData treaty = new TreatyData();
		treaty.expire_date = expire_date;
		treaty.proposer_state = state.id;
		treaty.target_state = target_state.id;
		treaty.proposed_relation = proposed_relation;
		treaty.rejection_leads_to_unstable = rejection_leads_to_unstable;
		treaty.uid = data.next_treat_id++;
		data.treaties.add(treaty);
		Treaty new_treaty = new Treaty(this, treaty);
		treaties.add(new_treaty);
		return new_treaty;
	}
	
	public void CloseTreaty(Treaty treaty) {
		treaties.remove(treaty);
		data.treaties.remove(treaty.data);
	}
	
	public Iterable<Person> PollDeadPeople() {
		return death_chronicle.PollElementsLessThan(data.date);
	}
	
	public Iterable<Person> GetAvailablePeople() {
		return sorted_real_persons.GetElementsLessThan(data.date);
	}
	
	public ConstGameData GetConstData() {
		return GameData.const_data;
	}
	
	public Random GetRandom() {
		return data.random.GetRandom();
	}
	
	public long GetRandomSeed() {
		return data.random_seed;
	}
	
	public Overrides GetOverrides() {
		return overrides;
	}
	
	// Setters
	private void Init() {
		cities.clear();
		states.clear();
		armies.clear();
		persons.clear();
		monarchs.clear();
		treaties.clear();
		message_queue.clear();
		sorted_real_persons.Clear();
		sorted_real_monarchs.Clear();
		death_chronicle.Clear();
		army_positions.Clear();
		vassals.Clear();
		market = new Market(this, data.market);
		overrides = new Overrides(this, data.override);
		player = new Player(this, data.player);
		
		for (int i = 0; i < data.states.size(); ++i) {
			State state = new State(this, i, data.states.get(i), GameData.const_data.states.get(i));
			states.add(state);
			State suzerainty = state.GetDiplomacy().GetSuzerainty();
			if (suzerainty != null) {
				vassals.Insert(suzerainty, state);
			}
		}
		for (int i = data.states.size(); i < GameData.const_data.states.size(); ++i) {
			State state = new State(this, i, data.states.get(0), GameData.const_data.states.get(i));
			states.add(state);
		}
		for (int i = 0; i < data.cities.size(); ++i) {
			City city = new City(this, i, data.cities.get(i), GameData.const_data.cities.get(i));
			cities.add(city);
			city.GetOwner().AddCity(city);
		}
		for (int i = 0; i < data.cities.size(); ++i) {
			City city = new City(this, i, data.cities.get(i), GameData.const_data.cities.get(i));
			overrides.ApplySpecialImprovement(city.GetImprovements().GetFinishedSpecialImprovement());
		}
		for (int i = 0; i < data.persons.size(); ++i) {
			Person person = new Person(this, i, data.persons.get(i), i < GameData.const_data.persons.size() ? GameData.const_data.persons.get(i) : null);
			persons.add(person);
			if (person.IsDead()) continue;
			death_chronicle.Insert(person);
			if (!person.IsFake()) sorted_real_persons.Insert(person);
			State state = person.GetOwner();
			if (state != null) {
				state.AddPerson(person);
				state.SetOfficer(person.GetRoleType(), person);
				City city = person.GetAssignedCity();
				if (city != null) city.SetGovernor(person);
				Army army = person.GetAssignedArmy();
				if (army != null) army.SetGeneral(person);
			}
		}
		for (int i = 0; i < data.monarchs.size(); ++i) {
			Monarch monarch = null;
			if (i < GameData.const_data.monarchs.size()) {
				monarch = new Monarch(this, i, data.monarchs.get(i), GameData.const_data.monarchs.get(i));
				sorted_real_monarchs.Insert(monarch.GetState(), monarch);				
			} else {
				monarch = new Monarch(this, i, data.monarchs.get(i), null);
			}
			monarchs.add(monarch); 
			if (monarch.IsDead()) continue;
			death_chronicle.Insert(monarch);
			State state = monarch.GetOwner();
			if (state != null) state.SetKing(monarch);
		}
		for (State state : states) {
			state.UpdateNeighbors();
		}
		for (City city : cities) {
			city.UpdateTransportation();
		}
		utils.trans_util.UpdateTradeRoutes();
		
		for (ArrayList<Monarch> monarch_list : sorted_real_monarchs.GetValues()) {
			monarch_list.sort(new Person.AgeComparator());
		}
		
		for (TreatyData treaty : data.treaties) {
			treaties.add(new Treaty(this, treaty));
		}
	}
	
	// Others
	public void Save(int index) {
		JsonUtil.WriteOneObjectToJson(String.format("saves/slot_%d.sav", index), data);
	}
	
	public static DataAccessor Load(int index) {
		GameData data = JsonUtil.ParseOneObjectFromJson(String.format("saves/slot_%d.sav", index), GameData.class);
		DataAccessor accessor = new DataAccessor(data);
		return accessor;
	}
	
	public static String CheckSlot(int index) {
		String path = String.format("saves/slot_%d.sav", index);
		if (FileUtil.CheckExistence(path)) {
			GameData tmp = JsonUtil.ParseOneObjectFromJson(path, GameData.class);
			return tmp.date.toString();
		}
		return null;
	}
	
	public void DebugPrint() {
//		for (Person person : death_chronicle.GetElementsLessThan(new Date(2000, 1, 1))) {
//			if (person.GetOwner() == null) continue;
//			System.err.print(person.GetDeathTime());
//			System.err.println(" : " + person.GetName() + " - " + person.GetOwner().GetName());
//			//System.err.println(" : " + person.GetName());
//		}
//		for (Person person : sorted_real_persons.GetElementsLessThan(new Date(2000, 1, 1))) {
//			//if (person.GetOwner() == null) continue;
//			System.err.print(person.data.available_time);
//			System.err.println(" : " + person.GetName());
//			//System.err.println(" : " + person.GetName());
//		}
	}
	
	private GameData data;
	protected ArrayList<City> cities = new ArrayList<City>();
	protected ArrayList<State> states = new ArrayList<State>();
	protected ArrayList<Army> armies = new ArrayList<Army>();
	protected ArrayList<Person> persons = new ArrayList<Person>();
	protected ArrayList<Monarch> monarchs = new ArrayList<Monarch>();
	protected ArrayList<Treaty> treaties = new ArrayList<Treaty>();
	protected Market market;
	protected Overrides overrides;
	protected Player player;
	
	// Views
	protected DataCollection<City> playable_cities = new DataCollection<City>(cities, city -> city.GetOwner().Playable());
	protected DataCollection<State> playable_states = new DataCollection<State>(states, state -> state.Playable());
	protected DataCollection<Army> playable_armies = new DataCollection<Army>(armies, army -> army.GetState().Playable());
	//protected ArrayList<Person> sorted_real_persons = new ArrayList<Person>();
	protected MultiMap<State, Monarch> sorted_real_monarchs = new MultiMap<State, Monarch>();
	// protected TreeSet<Person> death_chronicle = new TreeSet<Person>(new Person.DeathTimeComparator());
	protected KeyOrderedVector<Person, Date> sorted_real_persons = new KeyOrderedVector<Person, Date>(new Person.AgeComparator());
	protected KeyOrderedVector<Person, Date> death_chronicle = new KeyOrderedVector<Person, Date>(new Person.DeathTimeComparator());
	protected MultiMap<Position, Army> army_positions = new MultiMap<Position, Army>();
	protected MultiMap<State, State> vassals = new MultiMap<State, State>();
	
	private Queue<Pair<Date, String>> message_queue = new ConcurrentLinkedQueue<Pair<Date, String>>();
	private Messages messages = new Messages(this, message_queue);
	private Utils utils;
}

