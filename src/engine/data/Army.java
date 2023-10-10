package ac.engine.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;

import ac.data.ArmyData;
import ac.data.ArmyData.SoldierType;
import ac.data.ArmyData.UnitDistribution;
import ac.data.base.Date;
import ac.data.base.Pair;
import ac.data.base.Position;
import ac.data.constant.Texts;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import ac.util.ContainerUtil;
import ac.util.TileUtil;

public class Army extends IdKeyedData {

	protected Army(DataAccessor accessor, State state, int id, ArmyData data) {
		super(accessor, id);
		this.data = data;
		this.state = state;
		
		combat = new ArmyCombat(accessor);
		
		if (id >= 0) {
			typed_soldiers = new long[ArmyData.kMaxSoldierTypes];
			soldier_type_dist = new double[ArmyData.kMaxSoldierTypes];
		}
		UpdateUnitQuantities();
		UpdateSoldierDistribution();
	}

	@Override
	public String GetName() {
		Person general = GetGeneral();
		if (general != null) {
			return general.GetName() + Texts.legion;
		}
		return GetIdName();
	}
	
	public String GetFullName() {
		return GetState().GetName() + "-" + GetName();
	}
	
	public String GetId() {
		return String.valueOf(id + 1);
	}
	
	public String GetIdName() {
		return Texts.legion + "(" + (id + 1) +")";
	}
	
	public boolean IsGarrison() {
		return false;
	}
	
	public State GetState() {
		return state;
	}
	
	public Person GetGeneral() {
		return general;
	}
	
	public long GetTotalSoldier() {
		return total_soldiers;
	}
	
	public long GetTypedSoldier(UnitType type) {
		return typed_unit_soldiers[type.ordinal()];
	}
	
	public long GetTypedSoldier(SoldierType type) {
		return typed_soldiers[type.ordinal()];
	}
	
	public double GetTypedSoldierRatio(UnitType type) {
		return (double)typed_unit_soldiers[type.ordinal()] / total_soldiers;
	}
	
	public long GetTypedSoldier(UnitType unit_type, SoldierType soldier_type) {
		return data.typed_soldier_quantities[unit_type.ordinal()][soldier_type.ordinal()];
	}
	
	public Collection<Pair<City, Double>> GetConscriptionCity() {
		ArrayList<Pair<City, Double>> cities = new ArrayList<Pair<City, Double>>();
		for (Entry<Integer, Double> city : data.city_conscriptions.entrySet()) {
			cities.add(new Pair<City, Double>(accessor.cities.get(city.getKey()), city.getValue()));
		}
		return cities;
	}
	
	public long GetCityConscription(City city) {
		return Math.round(typed_soldiers[SoldierType.CONSCRIPTION.ordinal()] * data.city_conscriptions.getOrDefault(city.id, 0.0));
	}
	
	public City GetLogistcalCity() {
		return data.logistical_city >= 0 ? accessor.cities.get(data.logistical_city) : null;
	}
	
	public int GetLogisticCost() {
		return data.logistic_cost;
	}
	
	public int GetTotalSupportingCities() {
		return data.city_supporting_labors.size();
	}
	
	public City GetSupportingCity(int index) {
		if (index < 0 || index >= data.city_supporting_labors.size()) return null;
		for (int i : data.city_supporting_labors.keySet()) {
			if (index-- <= 0) return accessor.cities.get(i);
		}
		return null;
	}
	
	public long GetCitySupportingLabor(City city) {
		return data.city_supporting_labors.getOrDefault(city.id, 0L);
	}
	
	public long GetTotalSupportingLabor() {
		long total = 0L;
		for (long labor : data.city_supporting_labors.values()) total += labor;
		return total;
	}
	
	protected long ComputeTypedSoldier(UnitType type) {
		UnitDistribution unit_dist = data.typed_unit_quantities[type.ordinal()];
		if (unit_dist == null) return 0L;
		long quantity = 0L;
		for (long q : unit_dist.unit_quantities.values()) {
			quantity += q;
		}
		return quantity;
	}
	
	
	public TreeMap<Unit, Long> GetUnitQuantities(UnitType type) {
		return unit_quantity_map.get(type);
	}
	
	public double GetTrainingLevel() {
		return data.training;
	}
	
	public double GetMorale() {
		return data.morale;
	}
	
	public double[] GetSoldierTypeDistribution() {
		return soldier_type_dist;
	}
	
	public City GetBaseCity() {
		if (data.base_city < 0) return GetState().GetCapital();
		return accessor.cities.get(data.base_city); 
	}
	
	public Position GetPosition() {
		return data.position;
	}
	
	public static enum Status {
		IDLE,
		ATTACK,
		PURSUE_ATTACK,
		RETREAT,
	}
	
	public Status GetStatus() {
		if (data.target != null) return data.target.pursue ? Status.PURSUE_ATTACK : Status.ATTACK;
		if (data.retreat) return Status.RETREAT;
		return Status.IDLE;
	}
	
	public Army GetTarget() {
		if (data.target != null) {
			if (data.target.city >= 0) {
				return accessor.cities.get(data.target.city).GetMilitary().GetGarrison();
			}
			return accessor.GetState(data.target.army_state).GetMilitary().GetArmy(data.target.army_index);
		}
		return null;
	}
	
	public Position GetNextTile() {
		if (data.march.direction < 0) return null;
		return TileUtil.GetNeighborPosition(GetPosition(), data.march.direction);
	}
	
	public Date GetNextTileArriveDate() {
		return data.march.arrive_date;
	}
	
	public Date GetArriveDate() {
		return data.march.final_arrive_date;
	}
	
	public ArmyCombat GetCombat() {
		return combat;
	}
	
	// Setter
	public void SetMovement(int next_tile_direction, Date arrive_data, Date final_arrive_date) {
		data.march.direction = next_tile_direction;
		data.march.arrive_date = arrive_data;
		data.march.final_arrive_date = final_arrive_date;
	}
	
	public void ResetMovement() {
		data.march.direction = -1;
		data.march.arrive_date = null;
		data.march.final_arrive_date = null;
	}
	
	public void SetTarget(Army army, boolean pursue) {
		data.target = new ArmyData.Target();
		data.target.army_state = army.GetState().id;
		data.target.army_index = army.id;
		data.target.pursue = pursue;
	}
	
	public void SetTarget(City city) {
		data.target = new ArmyData.Target();
		data.target.city = city.id;
		data.target.pursue = true;
	}
	
	public void ResetTarget() {
		data.target = null;
	}
	
	public void Arrive() {
		accessor.army_positions.Remove(data.position, this);
		data.position = TileUtil.GetNeighborPosition(data.position, data.march.direction);
		accessor.army_positions.Insert(data.position, this);
		ResetMovement();
	}
	
	public void SetRetreat(boolean retreat) {
		data.retreat = retreat;
		ResetTarget();
	}
	
	public void SetGeneral(Person person) {
		general = person;
		if (general != null) general.AssignToArmy(this);
	}
	
	public void AddSoldiers(Unit unit, SoldierType soldier_type, City city, long quantity) {
		data.training = data.training * total_soldiers / (total_soldiers + quantity);
		int type_index = unit.type.ordinal();
		data.typed_soldier_quantities[type_index][soldier_type.ordinal()] += quantity;
		//data.typed_units[type_index][soldier_type.ordinal()] += quantity;
		UnitDistribution unit_dist = data.typed_unit_quantities[type_index];
		
		unit_dist.unit_quantities.put(unit.index, unit_dist.unit_quantities.getOrDefault(unit.index, 0L) + quantity);
		long typed_soldier = typed_soldiers[SoldierType.CONSCRIPTION.ordinal()];
		if (soldier_type == SoldierType.CONSCRIPTION) {
			double ratio = (double)typed_soldier / (typed_soldier + quantity);
			for (Entry<Integer, Double> entry : data.city_conscriptions.entrySet()) {
				if (entry.getKey().intValue() != city.id) {
					entry.setValue(entry.getValue() * ratio);
				}
			}
			data.city_conscriptions.put(city.id,
					(data.city_conscriptions.getOrDefault(city.id, 0.0) * typed_soldier + quantity) / (typed_soldier + quantity));
		}
		UpdateUnitQuantities();
		UpdateSoldierDistribution();
	}
	
	public HashMap<City, Long> ConvertSoldiers(SoldierType from, SoldierType to, long quantity) {
		quantity = Math.min(quantity, GetTypedSoldier(from));
		long[] typed_soldiers = new long[Unit.kMaxUnitType];
		for (int i = 0; i < Unit.kMaxUnitType; ++i) {
			typed_soldiers[i] = data.typed_soldier_quantities[i][from.ordinal()];
		}
		HashMap<City, Long> conscription_changes = null;
		if (from == SoldierType.CONSCRIPTION || to == SoldierType.CONSCRIPTION) {
			conscription_changes = new HashMap<City, Long>();
			long conscription = typed_soldiers[SoldierType.CONSCRIPTION.ordinal()];
			long sign = (from == SoldierType.CONSCRIPTION) ? -1 : 1;
			long[] city_soldiers = new long[data.city_conscriptions.size()];
			int index = 0;
			for (Entry<Integer, Double> entry : data.city_conscriptions.entrySet()) {
				city_soldiers[index++] = Math.round(entry.getValue() * conscription);
			}
			long[] distributed = ContainerUtil.Distribute(city_soldiers, quantity);
			long new_conscription = conscription + (sign * quantity);
			index = 0;
			for (Entry<Integer, Double> entry : data.city_conscriptions.entrySet()) {
				long change = sign * distributed[index];
				conscription_changes.put(accessor.cities.get(entry.getKey()), change);
				entry.setValue((double)(city_soldiers[index++] + change) / new_conscription);
			}
		}
		long[] distributed = ContainerUtil.Distribute(typed_soldiers, quantity);
		for (int i = 0; i < Unit.kMaxUnitType; ++i) {
			data.typed_soldier_quantities[i][from.ordinal()] -= distributed[i];
			data.typed_soldier_quantities[i][to.ordinal()] += distributed[i];
		}

		UpdateSoldierDistribution();
		return conscription_changes;
	}
	
	public void ChangeTrainingLevel(double delta) {
		data.training = Math.max(0.0, Math.min(1.0, data.training + delta));
	}
	
	public void ChangeMorale(double delta) {
		ChangeMorale(delta, 0.0);
	}
	
	public void ChangeMorale(double delta, double min) {
		data.morale = Math.max(min, Math.min(1.0, data.morale + delta));
	}
	
	public void DecreaseTypedSoldier(SoldierType type, double ratio) {
		for (int i = 0; i < Unit.kMaxUnitType; ++i) {
			long soldier_to_reduce = (long) (data.typed_soldier_quantities[i][type.ordinal()] * ratio);
			DecreaseTypedSoldierInternl(UnitType.values()[i], soldier_to_reduce);
			data.typed_soldier_quantities[i][type.ordinal()] -= soldier_to_reduce;
		}
		UpdateSoldierDistribution();
	}
	
	public long DecreaseTypedSoldier(UnitType type, double damage_ratio) {
		long soldier_to_reduce = 0L;
		if (data.typed_soldier_quantities != null) {
			for (int i = 0; i < ArmyData.kMaxSoldierTypes; ++i) {
				long damage = Math.round(data.typed_soldier_quantities[type.ordinal()][i]
						* Math.min(1.0, damage_ratio));
				data.typed_soldier_quantities[type.ordinal()][i] -= damage;
				soldier_to_reduce += damage;
			}
		} else {
			soldier_to_reduce = Math.round(GetTypedSoldier(type) * damage_ratio);
		}
		DecreaseTypedSoldierInternl(type, soldier_to_reduce);
		return soldier_to_reduce;
		
	}
	
	public void DecreaseTypedSoldierInternl(UnitType type, long soldier_to_reduce) {
		if (soldier_to_reduce <= 0) return;
		TreeMap<Integer, Long> unit_quantities = data.typed_unit_quantities[type.ordinal()].unit_quantities;
		Set<Integer> keys_to_remove = new HashSet<Integer>();
		for (Entry<Integer, Long> entry : unit_quantities.entrySet()) {
			long quantity = entry.getValue();
			if (soldier_to_reduce >= quantity) {
				keys_to_remove.add(entry.getKey());
				soldier_to_reduce -= quantity;
			} else {
				entry.setValue(quantity - soldier_to_reduce);
				break;
			}
		}
		for (Integer key : keys_to_remove) {
			unit_quantities.remove(key);
		}
		UpdateUnitQuantity(type);
	}
	
	protected void UpdateUnitQuantities() {
		for (UnitType type : UnitType.values()) {
			UpdateUnitQuantity(type);
		}
	}
	
	public void UpdateUnitQuantity(UnitType type, TreeMap<Unit, Long> unit_quantity) {
		int type_index = type.ordinal();
		UnitDistribution unit_dist = data.typed_unit_quantities[type_index];
		if (unit_dist == null) return;
		unit_quantity_map.put(type, unit_quantity);
		ArrayList<Unit> keys_to_remove = new ArrayList<Unit>();
		unit_dist.unit_quantities.clear();
		for (Entry<Unit, Long> entry : unit_quantity.entrySet()) {
			if (entry.getValue() <= 0L) {
				keys_to_remove.add(entry.getKey());
				continue;
			}
			unit_dist.unit_quantities.put(entry.getKey().index, entry.getValue());
		}
		for (Unit key : keys_to_remove) {
			unit_quantity.remove(key);
		}
	}
	
	protected void UpdateUnitQuantity(UnitType type) {
		int type_index = type.ordinal();
		UnitDistribution unit_dist = data.typed_unit_quantities[type_index];
		if (unit_dist == null) return;
		TreeMap<Unit, Long> unit_quantity = unit_quantity_map.get(type);
		if (unit_quantity == null) {
			unit_quantity = new TreeMap<Unit, Long>();
			unit_quantity_map.put(type, unit_quantity);
		}
		unit_quantity.clear();
		ArrayList<Integer> keys_to_remove = new ArrayList<Integer>();
		for (Entry<Integer, Long> entry : unit_dist.unit_quantities.entrySet()) {
			if (entry.getValue() <= 0L) {
				keys_to_remove.add(entry.getKey());
				continue;
			}
			Unit unit = accessor.GetConstData().typed_units.get(type_index).get(entry.getKey());
			unit_quantity.put(unit, entry.getValue());
		}
		for (Integer key : keys_to_remove) {
		 unit_dist.unit_quantities.remove(key);
		}
	}
	
	public void UpdateSoldierDistribution() {
		total_soldiers = 0L;
		if (typed_soldiers != null) {
			for (int j = 0; j < ArmyData.kMaxSoldierTypes; ++j) {
				typed_soldiers[j] = 0;
			}
		}
		for (int i = 0; i < Unit.kMaxUnitType; ++i) {
			typed_unit_soldiers[i] = ComputeTypedSoldier(UnitType.values()[i]);
			total_soldiers += typed_unit_soldiers[i];
			if (typed_soldiers != null) {
				for (int j = 0; j < ArmyData.kMaxSoldierTypes; ++j) {
					typed_soldiers[j] += data.typed_soldier_quantities[i][j];
				}
			}
		}
		if (soldier_type_dist != null && total_soldiers > 0) {
			for (int i = 0; i < ArmyData.kMaxSoldierTypes; ++i) {
				soldier_type_dist[i] = (double)typed_soldiers[i] / total_soldiers;
			}
		}
	}
	
	public void SetBaseCity(City city) {
		data.base_city = city.id;
	}
	
	public void ResetSupportingCities() {
		data.logistical_city = -1;
		data.logistic_cost = 0;
		data.city_supporting_labors.clear();
	}
	
	public void SetLogisticalCity(City city, int logistic_cost) {
		data.logistical_city = city.id;
		data.logistic_cost = logistic_cost;
	}
	
	public void AddSupportingCity(City city, long labor) {
		data.city_supporting_labors.put(city.id, labor);
	}
	
	public void RemoveCityConscription(City city) {
		data.city_conscriptions.remove(city.id);
		double total = 0.0;
		for (double value : data.city_conscriptions.values()) total += value;
		for (Entry<Integer, Double> pair : data.city_conscriptions.entrySet()) {
			pair.setValue(pair.getValue() / total);
		}
	}
	
	public void Abandon() {
		ResetMovement();
		ResetSupportingCities();
		ResetTarget();
		GetCombat().Reset();
		accessor.army_positions.Remove(data.position, this);
		data.position.x = -100;
		data.position.y = -100;
	}

	private State state;
	protected ArmyData data;
	private HashMap<UnitType, TreeMap<Unit, Long>> unit_quantity_map = new HashMap<UnitType, TreeMap<Unit, Long>>();
	private long total_soldiers = 0L;
	private long[] typed_unit_soldiers = new long[Unit.kMaxUnitType];
	private long[] typed_soldiers;
	private double[] soldier_type_dist;
	private Person general;
	
	private ArmyCombat combat;
}
