package ac.engine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

import ac.data.ArmyData.SoldierType;
import ac.data.base.Pair;
import ac.data.base.Position;
import ac.data.base.Resource;
import ac.data.constant.Unit;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Improvement.SpecialImprovementType;
import ac.data.constant.Technology.Effect;
import ac.data.constant.Tile;
import ac.data.constant.Tile.Terrain;
import ac.data.constant.Unit.UnitType;
import ac.engine.data.Army;
import ac.engine.data.Army.Status;
import ac.engine.data.ArmyCombat;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.Garrison;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.util.ComparatorUtil;
import ac.util.RandomUtil;

public class ArmyUtil extends BaseUtil {

	protected ArmyUtil(DataAccessor data, Utils utils) {
		super(data, utils);
	}
	
	public double GetCityDefenceBuffer(City city) {
		return param.city_base_defence_buffer
			+ city.GetImprovements().GetCount(ImprovementType.WALL) * param.wall_defence_buffer
			+ (city.GetImprovements().GetFinishedSpecialImprovement() == SpecialImprovementType.FORT ? param.fort_defence_buffer : 0);
	}
	
	public long GetMoblizableLabor(City city) {
		long mobilizable_power = (long)(city.GetTotalPopulation() * param.mobilize_base *
				(param.foreign_mobilize_multiplier + city.GetPopulation().GetPopRatio(0) * (1.0 - param.foreign_mobilize_multiplier)));
		if (city.GetOwner().GetPolicy().HasIdeology(Ideology.MOHISM)) {
			mobilizable_power /= 2;
		} else if (city.GetOwner().GetPolicy().HasIdeology(Ideology.AGRICULURAL_WAR)) {
			mobilizable_power *= 2;
		}
		return Math.min(mobilizable_power, utils.pop_util.GetAvailableLabor(city));
	}
	
	public long GetEffectiveSoldierBySupply(Army army, int logistic_cost, long mobilized_labor) {
		double supply_ratio = param.supply_multiplier / logistic_cost;
		long max_supporting_soldier = (long) (supply_ratio * mobilized_labor);
		long total = army.GetTotalSoldier();
		if (max_supporting_soldier >= total) return total;
		long remaining = total - max_supporting_soldier;
		return (long) (max_supporting_soldier + (supply_ratio / (supply_ratio + 1.0) * remaining));
	}

	public long GetMaintenanceCost(Army army) {
		long cost = 0L;
		for (UnitType type : UnitType.values()) {
			TreeMap<Unit, Long> unit_quantities = army.GetUnitQuantities(type);
			if (unit_quantities == null) continue;
			for (Entry<Unit, Long> kv : unit_quantities.entrySet()) {
				cost += kv.getKey().cost.gold * kv.getValue();
			}
		}
		return (long) (cost * param.unit_maintenance_cost_ratio);
	}
	
	public Resource<Long> GetReinforceCost(City city, long reinforcement) {
		return ReinforceAndReturnCost(city, reinforcement, null);
	}
	
	public void Reinforce(City city, long reinforcement, Resource<Long> resource) {
		ReinforceAndReturnCost(city, reinforcement, resource);
	}
	
	public void Reinforce(City city, Unit unit, long reinforcement) {
		Resource<Long> resource = new Resource<Long>(999999L);
		ReinforceAndReturnCost(city, unit, unit, reinforcement, 0.0, resource);
	}
	
	public Resource<Long> GetRecruitmentCost(Unit unit) {
		Resource<Long> cost = new Resource<Long>(0L);
		cost.Assign((x, y) -> y * param.base_recruitment, unit.cost);
		return cost;
	}
	
	public ArrayList<Army> AttackedBy(Army army) {
		ArrayList<Army> armies = new ArrayList<Army>(data.GetArmiesByPosition(army.GetPosition()));
		armies.removeIf(other_army -> other_army == army || other_army.GetTarget() != army);
		return armies;
	}
	
	public boolean IsArmyReinforceable(Army army) {
		return army.GetPosition().equals(army.GetBaseCity().GetPosition()) &&
				army.GetTarget() == null && AttackedBy(army).isEmpty();
	}
	
	public boolean IsArmyActioning(Army army) {
		if (army.GetArriveDate() != null) return true;
		Army target = army.GetTarget();
		if (target != null && army.GetPosition().equals(target.GetPosition())) {
			return true;
		}
		if (!AttackedBy(army).isEmpty()) return true;
		return false;
	}
	
	public boolean IsArmyAvailableForAttack(Army army) {
		if (army.GetStatus() == Status.RETREAT) return false;
		if (army.GetTotalSoldier() <= 0) return false;
		return true;
	}
	
	public void HandleMilitaryServiceForConscription(Army army) {
		for (City city : army.GetState().GetOwnedCities()) {
			city.Get().military_service += army.GetCityConscription(city);
		}
	}
	
	private Resource<Long> ReinforceAndReturnCost(City city, long reinforcement, Resource<Long> resource) {
		State state = city.GetOwner();
		Unit advanced_unit = state.GetTechnology().GetMostAdvancedUnit(UnitType.ARCHERY);
		Unit second_advanced_unit = state.GetTechnology().GetSecondAdvancedUnit(UnitType.ARCHERY);
		double advanced_unit_ratio = city.GetMilitary().GetAdvancedUnitRatio();
		return ReinforceAndReturnCost(city, advanced_unit, second_advanced_unit, reinforcement, advanced_unit_ratio, resource);
	}
	
	private Resource<Long> ReinforceAndReturnCost(City city, Unit advanced_unit, Unit second_advanced_unit, long reinforcement,
			double advanced_unit_ratio, Resource<Long> resource) {
		Resource<Long> cost = new Resource<Long>(0L);
		Garrison garrison = city.GetMilitary().GetGarrison();
		TreeMap<Unit, Long> unit_quantities = garrison.GetUnitQuantities(UnitType.ARCHERY);

		long max_soldiers = garrison.GetMaxSoldier();
		reinforcement = Math.min(reinforcement, max_soldiers);
		if (max_soldiers <= 0L) return cost;
		long num_soldiers = garrison.GetTotalSoldier();
		long reinforced = Math.min(max_soldiers - num_soldiers, reinforcement);
		if (resource != null) {
			reinforced = Math.min(Resource.Divide(resource, second_advanced_unit.cost), reinforced);
		}
		Resource.AddResource(cost, second_advanced_unit.cost, reinforced);
		reinforcement -= reinforced;
		if (resource != null) {
			unit_quantities.put(second_advanced_unit, unit_quantities.getOrDefault(second_advanced_unit, 0L) + reinforced);
			Resource.SubtractResource(resource, second_advanced_unit.cost, reinforced);
		}
		
		long advanced_soldiers = 0L;
		for (Entry<Unit, Long> unit_quantity : unit_quantities.entrySet()) {
			if (reinforcement <= 0) break;
			if (unit_quantity.getKey() == advanced_unit) {
				advanced_soldiers += unit_quantity.getValue();
			} else if (unit_quantity.getKey() != second_advanced_unit) {
				reinforced = Math.min(unit_quantity.getValue() , reinforcement);
				Resource<Integer> delta_cost = new Resource<Integer>(second_advanced_unit.cost);
				delta_cost.Assign((x, y) -> x - y, unit_quantity.getKey().cost);
				if (resource != null) {
					reinforced = Math.min(Resource.Divide(resource, delta_cost), reinforced);
				}
				Resource.AddResource(cost, delta_cost, reinforced);
				advanced_soldiers += unit_quantity.getValue();
				reinforcement -= reinforced;
				if (resource != null) {
					unit_quantity.setValue(unit_quantity.getValue() - reinforced);
					unit_quantities.put(second_advanced_unit, unit_quantities.getOrDefault(second_advanced_unit, 0L) + reinforced);
					Resource.SubtractResource(resource, delta_cost, reinforced);
				}
			}
		}
		long targeted_advanced_soldiers = (long) (max_soldiers * advanced_unit_ratio);
		reinforced = Math.min(targeted_advanced_soldiers - advanced_soldiers, reinforcement);
		Resource<Integer> delta_cost = new Resource<Integer>(advanced_unit.cost);
		delta_cost.Assign((x, y) -> x - y, second_advanced_unit.cost);
		if (resource != null) {
			reinforced = Math.min(Resource.Divide(resource, delta_cost), reinforced);
		}
		if (reinforced > 0) {
			Resource.AddResource(cost, advanced_unit.cost, reinforced);
			Resource.SubtractResource(cost, second_advanced_unit.cost, reinforced);
			if (resource != null) {
				unit_quantities.put(advanced_unit, unit_quantities.getOrDefault(advanced_unit, 0L) + reinforced);
				unit_quantities.put(second_advanced_unit, unit_quantities.getOrDefault(second_advanced_unit, 0L) - reinforced);
				Resource.SubtractResource(resource, delta_cost, reinforced);
			}
		}
		if (resource != null) {
			garrison.UpdateUnitQuantity(UnitType.ARCHERY, unit_quantities);
		}
		return cost;
	}
	
	public int GetTerrainTravelTime(Army army, Terrain terrain) {
		if (army.GetTotalSoldier() <= 0L) {
			return param.march_time[UnitType.MOUNTED.ordinal()][terrain.ordinal()];
		}
		double speed = 0;
		for (UnitType type : UnitType.values()) {
			int time = param.march_time[type.ordinal()][terrain.ordinal()];
			if (time == Integer.MAX_VALUE) return Integer.MAX_VALUE;
			double weighted_soldiers = 0;
			for (Entry<Unit, Long> unit_quantity: army.GetUnitQuantities(type).entrySet()) {
				weighted_soldiers += unit_quantity.getValue() * unit_quantity.getKey().speed_multiplier;
			}
			speed += weighted_soldiers / time;
		}
		if (speed <= 0) return Integer.MAX_VALUE;
		return (int)Math.round(army.GetTotalSoldier() / speed);
	}
	
	public City GetPositionCity(Army army) {
		return data.GetCityTerritoryByTile(data.GetConstData().GetTile(army.GetPosition()));
	}
	
	public State GetPositionOwner(Army army) {
		City city = GetPositionCity(army);
		if (city == null) return null;
		return city.GetOwner();
	}
	
	public boolean TileHasRiver(Tile tile) {
		int river = 0;
		for (int i = 0; i < 6; ++i) {
			if (tile.border_has_river[i] && ++river >= 2) return true;
		}
		return false;
	}
	
	public void ResetCombatPower(Army army) {
		Tile tile = data.GetConstData().GetTile(army.GetPosition());
		ArmyCombat combat = army.GetCombat();
		double[] attack = new double[Unit.kMaxUnitType];
		for (UnitType type : UnitType.values()) {
			long soldier = army.GetTypedSoldier(type);
			if (soldier <= 0) continue;
			Pair<Double, Double> combat_power = GetCombatPowerForUnitType(army, type, tile);
			combat.SetDefend(type, combat_power.second);
			//System.err.println("Defend: " + combat.GetDefend(type));
			if (combat_power.first <= 0) continue;
			soldier *= combat_power.first;
			for (UnitType unit_type : UnitType.values()) {
				attack[unit_type.ordinal()] +=
					param.combat_unit_type_multipliers[type.ordinal()][unit_type.ordinal()] * soldier;
			}
			if (combat.GetTarget() != null && combat.GetTarget().IsGarrison() && type == UnitType.SIEGE) {
				combat.SetSiegeAttack(combat_power.first + param.combat_siege_attack_base *
						GetCombatUnitTypeMultiplier(army, UnitType.SIEGE, tile)); 
			}
		}
		long total = army.GetTotalSoldier();
		for (UnitType unit_type : UnitType.values()) {
			combat.SetAttack(unit_type, attack[unit_type.ordinal()] / total);
			//System.err.println("Attack: " + combat.GetAttack(unit_type));
		}
		combat.SetPowerMultiplier(GetCombatPowerMultiplier(army));
		//System.err.println("Multiplier: " + combat.GetPowerMultiplier());
	}
	
	public void ResetGarrisonCombatPower(Army army) {
		Tile tile = data.GetConstData().GetTile(army.GetPosition());
		ArmyCombat combat = army.GetCombat();
		Pair<Double, Double> combat_power = GetCombatPowerForGarrison(army, tile);
		combat.SetDefend(UnitType.ARCHERY, combat_power.second);
		//System.err.println("Defend: " + combat.GetDefend(UnitType.ARCHERY));
		//System.err.println("Attack: " + combat_power.first);
		for (UnitType unit_type : UnitType.values()) {
			combat.SetAttack(unit_type, combat_power.first
					* param.combat_unit_type_multipliers[UnitType.ARCHERY.ordinal()][unit_type.ordinal()]);
			//System.err.println("Attack: " + combat.GetAttack(unit_type));
		}
		combat.SetPowerMultiplier(GetCombatPowerMultiplier(army));
		//System.err.println("Multiplier: " + combat.GetPowerMultiplier());
	}
	
	public Pair<Double, Double> GetCombatPowerForUnitType(Army army, UnitType unit_type, Tile tile) {
		double attack = 0.0;
		double defend = 0.0;
		for (Entry<Unit, Long> unit_quantity: army.GetUnitQuantities(unit_type).entrySet()) {
			Unit unit = unit_quantity.getKey();
			long quantity = unit_quantity.getValue();
			attack += unit.attack * quantity;
			defend += unit.defend * quantity;
		}
		double multiplier = GetCombatUnitTypeMultiplier(army, unit_type, tile) / army.GetTypedSoldier(unit_type);
		return new Pair<Double, Double>(attack * multiplier, defend * multiplier);
	}
	
	public Pair<Double, Double> GetCombatPowerForGarrison(Army army, Tile tile) {
		double attack = 0.0;
		double defend = 0.0;
		for (Entry<Unit, Long> unit_quantity: army.GetUnitQuantities(UnitType.ARCHERY).entrySet()) {
			Unit unit = unit_quantity.getKey();
			long quantity = unit_quantity.getValue();
			attack += unit.attack * quantity;
			defend += unit.defend * quantity;
		}
		double multiplier = GetCombatGarissonMultiplier(army, tile) / army.GetTotalSoldier();
		return new Pair<Double, Double>(attack * multiplier,
				defend * multiplier * (1.0 + GetCityDefenceBuffer(army.GetBaseCity())));
	}
	
	public double GetCombatPowerMultiplier(Army army) {
		if (army.GetTotalSoldier() <= 0) return 0.0;
		return GetCombatGeneralMultiplier(army) * GetCombatTrainingMultiplier(army) * GetCombatMoraleMultiplier(army);
	}
	
	public double GetCombatGeneralMultiplier(Army army) {
		if (army.GetTotalSoldier() <= 0) return 0.0;
		Person general = army.GetGeneral();
		int ability = utils.person_util.GetAbility(general, AbilityType.MILITARY);
		long total_soldier = army.GetTotalSoldier();
		double power = param.combat_general_base + param.combat_general_multiplier *
				(ability + utils.person_util.GetAbility(general, AbilityType.MILITARY) / 3);
		return power * Math.min(total_soldier, param.max_soldier_on_command[ability]) / total_soldier;
	}
	
	public double GetEffectiveSoldier(Army army, Tile tile, boolean is_siege) {
		double soldier = army.IsGarrison() ? army.GetTotalSoldier() :
				GetEffectiveSoldierBySupply(army, army.GetLogisticCost(), army.GetTotalSupportingLabor());
		if (is_siege) {
			return Math.min(soldier, param.siege_max_soldier);
		}
		return Math.min(soldier, param.terrain_max_soldier[tile.terrain.ordinal()]);
	}
	
	public double GetCombatTrainingMultiplier(Army army) {
		return (1.0 - param.combat_power_by_training) + param.combat_power_by_training * army.GetTrainingLevel();
	}
	
	public double GetCombatMoraleMultiplier(Army army) {
		return (1.0 - param.combat_power_by_morale) + param.combat_power_by_morale * army.GetMorale();
	}
	
	public double GetCombatUnitTypeMultiplier(Army army, UnitType unit_type, Tile tile) {
		double power = 0, total = 0;
		for (SoldierType soldier_type : SoldierType.values()) {
			long soldier = army.GetTypedSoldier(unit_type, soldier_type);	
			power += soldier * GetSoldierTypeMultiplier(army, soldier_type);
			total += soldier;
		}
		power *= param.combat_unit_terrain_multipliers[unit_type.ordinal()][tile.terrain.ordinal()];
		power *= TileHasRiver(tile) ? param.combat_unit_river_multipliers[unit_type.ordinal()] : 1.0;
		return total > 0 ? power / total : 0;
	}
	
	public double GetCombatGarissonMultiplier(Army army, Tile tile) {
		double power = 1.0 - (utils.city_util.GetMilitiaRatio(army.GetBaseCity()) *
				(1.0 - param.combat_militia_multiplier));
		power *= param.combat_unit_terrain_multipliers[UnitType.ARCHERY.ordinal()][tile.terrain.ordinal()];
		power *= TileHasRiver(tile) ? param.combat_unit_river_multipliers[UnitType.ARCHERY.ordinal()] : 1.0;
		return power;
	}
	
	public Comparator<Army> action_priority = ComparatorUtil.CreatePriorityComparator(new Comparator<Army>() {
		@Override
		public int compare(Army o1, Army o2) {
			return utils.person_util.GetAbility(o2.GetGeneral(), AbilityType.MILITARY) -
					utils.person_util.GetAbility(o1.GetGeneral(), AbilityType.MILITARY);
		}
	}, new Comparator<Army>() {
		@Override
		public int compare(Army o1, Army o2) {
			if (o2.GetMorale() > o1.GetMorale()) return -1;
			if (o1.GetMorale() > o2.GetMorale()) return 1;
			return 0;
		}
	}, new Comparator<Army>() {
		@Override
		public int compare(Army o1, Army o2) {
			return (int) (o2.GetTotalSoldier() - o1.GetTotalSoldier());
		}
	}, new Comparator<Army>() {
		@Override
		public int compare(Army o1, Army o2) {
			return RandomUtil.WhetherToHappend(0.5, data.GetRandom()) ? 1 : -1;
		}
	});
	
	public void HandleCombatMorale(Army army1, Army army2) {
		double damage_ratio = (double)army1.GetCombat().GetKilled() / army2.GetCombat().GetKilled();
		double morale_decrease = RandomUtil.SampleFromUniformDistribution(0, Math.sqrt(damage_ratio), data.GetRandom());
		army1.ChangeMorale(-morale_decrease / 100);
	}
	
	public void HandleCombatDamage(Army army) {
		ArmyCombat combat = army.GetCombat();
		for (UnitType type : UnitType.values()) {
			long damage = combat.GetKilled(type);
			double damage_ratio = (double)damage  / army.GetTypedSoldier(type);
			if (!army.IsGarrison()) {
				long conscription = army.GetTypedSoldier(type, SoldierType.CONSCRIPTION);
				if (conscription > 0) {
					Collection<Pair<City, Double>> city_ratio = army.GetConscriptionCity();
					for (Pair<City, Double> city : city_ratio) {
						utils.pop_util.CityReducePopulation(city.first, Math.round(city.second * conscription * damage_ratio));
					}
				}
			}
			damage = army.DecreaseTypedSoldier(type, damage_ratio);
			combat.SetKilled(type, damage);
		}
		army.UpdateSoldierDistribution();
	}
	
	public void AbandonArmy(Army army) {
		army.Abandon();
		ResetTargets(army);
	}
	
	public void ResetTargets(Army army) {
		for (Army other : data.GetAllArmies()) {
			if (other.GetTarget() == army && other.GetStatus() == Status.ATTACK) {
				other.ResetTarget();
			}
		}
	}
	
	public boolean AtForeignTerritory(Army army) {
		State state = army.GetState();
		State owner = GetPositionOwner(army);
		if (state == owner || utils.diplomacy_util.BorderOpened(owner, state)) return false;
		return true;
	}
	
	public boolean AtSiege(Army army) {
		Army target = army.GetTarget();
		if (target != null && target.IsGarrison() && target.GetPosition().equals(army.GetPosition())) return true;
		return false;
	}
	
	public boolean AtWar(Army army) {
		Position pos = army.GetPosition();
		Army target = army.GetTarget();
		for (Army other : data.GetArmiesByPosition(pos)) {
			if (other.GetTotalSoldier() <= 0) continue;
			if (target == other) return true;
			if (other.GetTarget() == army) return true;
		}
		if (target != null && target.IsGarrison() && target.GetPosition().equals(pos)) return true;
		return false;
	}
	
	public long GetDailyFoodConsumption(Army army, long labor, boolean is_siege) {
		long food = (long) (labor * param.combat_labor_food_consumption);
		food += (long) (army.GetTotalSoldier() * (is_siege ?
				param.combat_siege_army_food_consumption : param.combat_army_food_consumption));
		return food / 30;
	}
	
	public long GetDailyFoodConsumption(Army army) {
		if (AtSiege(army)) {
			return GetDailyFoodConsumption(army, army.GetTotalSupportingLabor(), true);
		} else if (AtForeignTerritory(army)) {
			return GetDailyFoodConsumption(army, army.GetTotalSupportingLabor(), false);
		} else if (AtWar(army)) {
			return (long) (army.GetTotalSoldier() * param.combat_demostic_army_food_consumption);
		}
		return 0L;
	}
	
	private double GetSoldierTypeMultiplier(Army army, SoldierType soldier_type) {
		double multiplier = param.combat_soldier_type_multiplier[soldier_type.ordinal()];
		if (soldier_type == SoldierType.CONSCRIPTION) {
			if (army.GetState().GetPolicy().HasIdeology(Ideology.AGRICULURAL_WAR)) {
				multiplier = param.combat_soldier_type_multiplier[SoldierType.FUBING.ordinal()];
			}
			if (army.GetState().GetTechnology().HasEffect(Effect.BOOST_CONSCRIPTION_COMBAT_POWER)) {
				multiplier *= 1.5;
			}
		}
		if (army.GetState().GetPolicy().HasIdeology(Ideology.MILITARY_BUREAU)) {
			multiplier *= 0.9;
		}
		return multiplier;
	}
}
