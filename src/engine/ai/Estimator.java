package ac.engine.ai;

import java.util.Map.Entry;

import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.Garrison;
import ac.engine.data.State;
import ac.engine.util.Utils;

public class Estimator {
	public Estimator(DataAccessor data) {
		utils = new Utils(data);
	}
	
	public static class AttackerEstimate {
		public Army army;
		public double attack_score = 0;
	}
	
	public static AttackerEstimate GetAttacker(State state) {
		AttackerEstimate ret = new AttackerEstimate();
		for (Army army : state.GetMilitary().GetArmies()) {
			if (army.GetStatus() == Army.Status.IDLE && army.GetMorale() >= 0.95) {
				double score = GetArmyScore(army);
				if (score > ret.attack_score) {
					ret.attack_score = score;
					ret.army = army;
				}
			}
		}
		return ret;
	}
	
	public static double GetDesireScore(State state, City target_city) {
		double score = target_city.GetTotalPopulation();
		score *= (1.0 + target_city.GetPopulation().GetPopRatio(state));
		return score;
	}
	
	public static class ChanceEstimate {
		public double chance_score = 0;
		public City nearest_city;
	}
	
	public ChanceEstimate GetChance(State state, City target_city, AttackerEstimate attacker) {
		ChanceEstimate ret = new ChanceEstimate();
		// Defend
		double defend_score = GetGarrisonScore(target_city);
		for (Army army : target_city.GetOwner().GetMilitary().GetArmies()) {
			if (army.GetPosition().equals(target_city.GetPosition())) {
				defend_score += GetArmyScore(army);
			}
		}
		defend_score *= (1.0 + utils.army_util.GetCityDefenceBuffer(target_city));
		
		// Attack
		ret.nearest_city = utils.trans_util.GetNearestCity(state, target_city);
		if (ret.nearest_city == null) return ret;
		double attack_score = attacker.attack_score;
		long mobilized_labor = GetMobilizedLabor(state, ret.nearest_city);
		attack_score *= (double)utils.army_util.GetEffectiveSoldierBySupply(attacker.army,
				ret.nearest_city.GetTransportation(target_city), mobilized_labor) / attacker.army.GetTotalSoldier();
		attack_score *= utils.army_util.GetCombatPowerMultiplier(attacker.army);
		attack_score *= GetDurationScore(attacker.army, target_city, ret.nearest_city, mobilized_labor);
		
		ret.chance_score = Math.min(1.0, attack_score / (defend_score + 0.00001) * 0.1);
		return ret;
	}
	
	public double GetTargetStateDiscount(State state, State target_state) {
		double ret = 1.0;
		if (target_state == null) return ret;
		if (utils.diplomacy_util.AreAlly(state, target_state)) ret *= 0.8;
		if (utils.diplomacy_util.AreAlliance(state, target_state) ||
				state.GetDiplomacy().GetSuzerainty() == target_state ||
				target_state.GetDiplomacy().GetSuzerainty() == state) ret *= 0.3;
		return ret;
	}
	
	private double GetGarrisonScore(City city) {
		double score = 0;
		Garrison garrison = city.GetMilitary().GetGarrison();
		for (Entry<Unit, Long> unit_quantity : garrison.GetUnitQuantities().entrySet()) {
			score += GetUnitScore(unit_quantity.getKey()) * unit_quantity.getValue();
		}
		return score;
	}
	
	private static double GetArmyScore(Army army) {
		double score = 0;
		for (UnitType type : UnitType.values()) {
			for (Entry<Unit, Long> unit_quantity : army.GetUnitQuantities(type).entrySet()) {
				score += GetUnitScore(unit_quantity.getKey()) * unit_quantity.getValue();
			}
		}
		return score;
	}
	
	private long GetMobilizedLabor(State state, City nearest_city) {
		long labor = 0L;
		for (City city : nearest_city.GetNeighbors()) {
			if (city.GetOwner() != state) continue;
			labor += utils.army_util.GetMoblizableLabor(city);
		}
		return labor;
	}
	
	public int GetEstimatedDurationDays(Army army, City target_city, City nearest_city, long labor) {
		long daily_consumption = utils.army_util.GetDailyFoodConsumption(army, labor, false);
		daily_consumption += utils.state_util.GetFoodExpense(army.GetState()) / 30 * 1.5 + 1;
		int estimated_days = (int) (army.GetState().GetResource().food / daily_consumption);
		estimated_days -= nearest_city.GetTransportation(target_city) * 2;
		return Math.max(0, estimated_days);
	}
	
	private double GetDurationScore(Army army, City target_city, City nearest_city, long labor) {
		int estimated_days = GetEstimatedDurationDays(army, target_city, nearest_city, labor);
		final int max_days = 60;
		if (estimated_days > max_days) return 1.0;
		if (estimated_days < 10) return 0.0;
		return (double)(estimated_days) / max_days;
	}
	
	private static double GetUnitScore(Unit unit) {
		return unit.attack + unit.defend;
	}
	
	private Utils utils;
}
