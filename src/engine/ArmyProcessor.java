package ac.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import ac.data.base.Date;
import ac.data.base.MultiMap;
import ac.data.base.Pair;
import ac.data.base.Position;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Role.RoleType;
import ac.data.constant.Technology.Effect;
import ac.data.constant.Parameters;
import ac.data.constant.Texts;
import ac.data.constant.Tile;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import ac.engine.ai.GameInterface;
import ac.engine.data.Army;
import ac.engine.data.Army.Status;
import ac.engine.data.ArmyCombat;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.Garrison;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.engine.util.TransportationUtil.Path;
import ac.engine.util.Utils;
import ac.util.RandomUtil;

public class ArmyProcessor {
	protected ArmyProcessor(DataAccessor data, GameInterface ai) {
		this.data = data;
		this.param = data.GetParam();
		this.utils = data.GetUtils();
		this.ai = ai;
	}
	
	public void HandleReinforcement() {
		Unit most_advanced_unit = data.GetConstData().typed_units.get(UnitType.ARCHERY.ordinal()).get(0);
		for (State state : data.GetAllPlayableStates()) {
			ArrayList<City> cities = new ArrayList<City>(state.GetOwnedCities());
			cities.sort(new Comparator<City>() {
				@Override
				public int compare(City o1, City o2) {
					Garrison g1 = o1.GetMilitary().GetGarrison();
					Garrison g2 = o2.GetMilitary().GetGarrison();
					return (int) ((g1.GetTotalSoldier() - g1.GetMaxSoldier()) - (g2.GetTotalSoldier() - g2.GetMaxSoldier()));
				}
			});
			for (City city : cities) {
				utils.army_util.Reinforce(city, param.base_reinforcement, state.GetResource());
			}
			Unit unit = state.GetTechnology().GetSecondAdvancedUnit(UnitType.ARCHERY);
			if (most_advanced_unit.compareTo(unit) < 0) most_advanced_unit = unit;
		}
		for (City city : data.GetAllCities()) {
			if (city.GetOwner() != null && city.GetOwner().Playable()) continue;
			utils.army_util.Reinforce(city, most_advanced_unit, param.base_reinforcement);
		}
	}
	
	public void HandleBattles() {
		for (Army army : data.GetAllPlayableArmies()) {
			army.GetCombat().Reset();
			if (army.GetTotalSoldier() <= 0) army.SetRetreat(true);
			if (army.GetStatus() == Army.Status.IDLE) {
				State owner = utils.army_util.GetPositionOwner(army);
				if (owner != army.GetState() && !utils.diplomacy_util.BorderOpened(owner, army.GetState())) {
					army.SetRetreat(true);
				}
			}
		}
		for (City city : data.GetAllPlayableCities()) {
			city.GetMilitary().GetGarrison().GetCombat().Reset();
		}
		HashSet<Position> finished_battles = new HashSet<Position>();
		for (Army army : data.GetAllPlayableArmies()) {
			Position pos = army.GetPosition();
			if (!finished_battles.add(pos)) continue;
			HandelBattle(pos);
		}
	}
	
	public void HandleMovement() {
		for (Army army : data.GetAllPlayableArmies()) {
			if (army.GetCombat().GetTarget() != null) {  // Attacked
				army.ResetMovement();
				continue;
			}
			if (army.GetStatus() == Army.Status.ATTACK) {
				Army target = army.GetTarget();
				if (target == null || utils.army_util.GetPositionOwner(target) == target.GetState()) {
					army.ResetTarget();
				}
			}
			Date arrive_date = army.GetNextTileArriveDate();
			if (arrive_date != null) {
				int diff = data.GetDate().GetDifference(arrive_date);
				if (diff < 0) {
					continue;
				}
				Tile next_tile = data.GetConstData().GetTile(army.GetNextTile());
				if (utils.trans_util.IsTileAccessible(army, next_tile)) {
					boolean border_crossed = CheckCrossBorder(army);
					army.Arrive();
					if (border_crossed) {
						City city = data.GetCityTerritoryByTile(next_tile);
						if (city.GetOwner() != null && city.GetOwner() != army.GetState()) ai.OnInvaded(city.GetOwner(), city, army);
					}
				} else {
					army.ResetMovement();
				}
			}

			if (army.GetStatus() == Army.Status.IDLE || army.GetStatus() == Army.Status.RETREAT || army.GetTotalSoldier() <= 0) {
				if (army.GetPosition().equals(army.GetBaseCity().GetPosition())) {
					army.ResetMovement();
					army.SetRetreat(false);
					continue;
				}
				Path path = utils.trans_util.FindShortestPath(army, army.GetBaseCity().GetPosition());
				if (path != null && path.shortest_time < Integer.MAX_VALUE) {
					army.SetMovement(path.next_direction, data.GetDate().CreateDate(path.next_step_time),
							data.GetDate().CreateDate(path.shortest_time));
				}
			} else if (army.GetStatus() == Army.Status.ATTACK || army.GetStatus() == Army.Status.PURSUE_ATTACK) {
				if (army.GetPosition().equals(army.GetTarget().GetPosition())) {
					army.ResetMovement();
					continue;
				}
				Path path = utils.trans_util.FindShortestPath(army, army.GetTarget().GetPosition());
				if (path != null && path.shortest_time < Integer.MAX_VALUE) {
					army.SetMovement(path.next_direction, data.GetDate().CreateDate(path.next_step_time),
							data.GetDate().CreateDate(path.shortest_time));
				} else {
					ai.OnArmyRouteBlocked(army);
				}
			}
		}
	}
	
	public void HandleLogistics() {
		for (State state : data.GetAllPlayableStates()) {
			for (Army army : state.GetMilitary().GetArmies()) {
				army.ResetSupportingCities();
			}
			for (Army army : state.GetMilitary().GetArmies()) {
				// if (utils.army_util.IsArmyReinforceable(army)) continue;
				if (army.GetTotalSoldier() <= 0L) continue;
				if (utils.army_util.GetPositionOwner(army) == state && !army.GetCombat().HasBattled()) continue;
				
				Pair<City, Integer> supply = utils.trans_util.GetSupplyRoute(army);
				if (supply != null) {
					City city = supply.first;
					army.SetLogisticalCity(city, supply.second);
					long labor_needed = (long) Math.ceil(army.GetTotalSoldier() * supply.second / param.supply_multiplier);
					if (state.GetTechnology().HasEffect(Effect.REDUCE_LOGISTIC_LABOR)) {
						labor_needed /= 0.7;
					}
					long labor = Math.min(labor_needed, utils.army_util.GetMoblizableLabor(city));
					if (labor > 0) {
						army.AddSupportingCity(city, labor);
						labor_needed -= labor;
					}
					if (labor_needed > 0) {
						HashMap<City, Integer> neighbor_costs = city.GetNeighborAndTransportation();
						PriorityQueue<City> neighbors = new PriorityQueue<City>(new Comparator<City>() {
							@Override
							public int compare(City o1, City o2) {
								return neighbor_costs.get(o1) - neighbor_costs.get(o2);
							}
						});
						for (City neighbor : city.GetNeighbors()) {
							if (neighbor.GetOwner() == army.GetState()) neighbors.add(neighbor);
						}
						while (labor_needed > 0 && !neighbors.isEmpty()) {
							City neighbor = neighbors.poll();
							labor = Math.min(labor_needed, utils.army_util.GetMoblizableLabor(neighbor));
							if (labor > 0) {
								army.AddSupportingCity(neighbor, labor);
								labor_needed -= labor;
							}
						}
					}
				}
			}
		}
		for (State state : data.GetAllPlayableStates()) {
			for (Army army : state.GetMilitary().GetArmies()) {
				long food_consumption = utils.army_util.GetDailyFoodConsumption(army);
				if (state.GetResource().food >= food_consumption) {
					state.GetResource().food -= food_consumption;
				} else {
					state.GetResource().food = 0L;
					army.ChangeMorale(-param.morale_decrease_from_food_shortage);
				}
				if (army.GetMorale() <= 0.0) {
					army.SetRetreat(true);
				}
			}
		}
	}
	
	private boolean CheckCrossBorder(Army army) {
		Army target = army.GetTarget();
		if (target == null) return false;
		Tile old_tile = data.GetConstData().GetTile(army.GetPosition());
		Tile new_tile = data.GetConstData().GetTile(army.GetNextTile());
		City city = data.GetCityTerritoryByTile(new_tile);
		if (city == null || city == data.GetCityTerritoryByTile(old_tile)) return false;
		State state = army.GetState();
		State owner = city.GetOwner();
		if (owner == null || owner == state || utils.diplomacy_util.BorderOpened(owner, state)) return false;
		if (target.GetState() == owner) return true;
		return false;  // maybe true
	}
	
	private void HandelBattle(Position pos) {
		Army garrison = null;
		ArrayList<Army> armies = new ArrayList<Army>(data.GetArmiesByPosition(pos));
		for (Army army : armies) {
			army.GetCombat().Reset();
		}
		armies.removeIf(army -> army.GetTotalSoldier() <= 0);
		HashSet<Army> city_attackers = new HashSet<Army>();
		HashSet<Army> attackers = new HashSet<Army>();
		MultiMap<Army, Army> army_targeted_by = new MultiMap<Army, Army>();
		for (Army army : armies) {
			Army target = army.GetTarget();
			if (target == null || army.GetStatus() == Status.RETREAT) continue;
			if (target.GetPosition().equals(army.GetPosition())) {
				if (army.GetState() == target.GetState()) {
					army.ResetTarget();
					continue;
				}
				attackers.add(army);
				army.GetCombat().SetTarget(target);
				army_targeted_by.Insert(target, army);
				if (target.IsGarrison()) {
					city_attackers.add(army);
					garrison = target;
				}
			}
		}
		if (attackers.isEmpty()) return;
		for (Army army : city_attackers) {
			ArrayList<Army> intecepters = army_targeted_by.Get(army);
			if (intecepters.isEmpty()) continue;
			army_targeted_by.Get(garrison).remove(army);
			int index = RandomUtil.SampleFromUniformDistribution(0, intecepters.size() - 1, data.GetRandom());
			army.GetCombat().SetTarget(intecepters.get(index));
			army_targeted_by.Insert(intecepters.get(index), army);
		}
		
		for (Army army : armies) {
			if (attackers.contains(army) || army_targeted_by.Contains(army)) {
				utils.army_util.ResetCombatPower(army);
			}
		}
		if (garrison != null && army_targeted_by.Contains(garrison)) {
			utils.army_util.ResetGarrisonCombatPower(garrison);
			// City damage
			if (RandomUtil.WhetherToHappend(param.siege_improvement_destroy_prob, data.GetRandom())) {
				utils.city_util.HandleCityImprovementDamage(garrison.GetBaseCity());
			}
		}
		
		Tile tile = data.GetConstData().GetTile(pos);
		ArrayList<Army> ranked_attackers = new ArrayList<Army>(attackers);
		ranked_attackers.sort(utils.army_util.action_priority);
		HashSet<Pair<Army, Army>> battle_messages = new HashSet<Pair<Army, Army>>();
		for (Army attacker : attackers) {
			if (attacker.GetTotalSoldier() <= 0 || attacker.GetStatus() == Army.Status.RETREAT) continue;
			Army defender = attacker.GetCombat().GetTarget();
			if (defender.GetTotalSoldier() <= 0) continue;
			
			long[] attacker_killed = new long[Unit.kMaxUnitType];
			long[] defender_killed = new long[Unit.kMaxUnitType];
			CalculateDamage(attacker, defender, tile, defender_killed, 1.0);
			CalculateDamage(defender, attacker, tile, attacker_killed, param.combat_counter_attack_multiplier);
			SetDamage(attacker, attacker_killed);
			SetDamage(defender, defender_killed);
			utils.army_util.HandleCombatMorale(attacker, defender);
			utils.army_util.HandleCombatMorale(defender, attacker);
			utils.army_util.HandleCombatDamage(attacker);
			utils.army_util.HandleCombatDamage(defender);
			boolean is_siege = defender.IsGarrison();
			if (attacker.GetTotalSoldier() <= 0 || attacker.GetMorale() <= 0.0) {
				attacker.SetRetreat(true);
				HandleCombatPrestige(attacker, defender, army_targeted_by.Get(attacker), tile, is_siege);
				AddBattelResultMessage(defender, attacker, battle_messages);
				utils.army_util.ResetTargets(attacker);
				HandelGeneralDeath(attacker);
			} else if (defender.GetTotalSoldier() <= 0 || defender.GetMorale() <= 0.0) {
				State conquerer = HandleCombatPrestige(defender, attacker, army_targeted_by.Get(defender), tile, is_siege);
				AddBattelResultMessage(attacker, defender, battle_messages);
				utils.army_util.ResetTargets(defender);
				HandelGeneralDeath(defender);
				if (is_siege) {
					HandleCityConquered(garrison.GetBaseCity(), conquerer);
					break;
				}
			}
		}
	}
	
	private void AddBattelResultMessage(Army winner, Army loser, HashSet<Pair<Army, Army>> printed_messages) {
		if (!printed_messages.add(new Pair<Army, Army>(winner, loser))) return;
		String winner_state_name = winner.GetState().GetName();
		data.AddMessage((winner_state_name.isEmpty() ? "" : winner_state_name + Texts.of) + winner.GetName() + Texts.defeat +
				loser.GetState().GetName() + Texts.of + loser.GetName());
	}
	
	private void CalculateDamage(Army army1, Army army2, Tile tile, long[] killed, double multiplier) {
		boolean is_siege = army2.IsGarrison();
		double effective_soldier = utils.army_util.GetEffectiveSoldier(army1, tile, is_siege);
		ArmyCombat army1_power = army1.GetCombat();
		ArmyCombat army2_power = army2.GetCombat();
		for (UnitType type : UnitType.values()) {
			double beared_attack_ratio = army2.GetTypedSoldierRatio(type);
			if (beared_attack_ratio <= 0.0) continue;
			double attack = army1_power.GetAttack(type) * army1_power.GetPowerMultiplier();
			double defend = army2_power.GetDefend(type) * army2_power.GetPowerMultiplier();
			double damage = effective_soldier * beared_attack_ratio * param.combat_damage_base
					* (attack / (attack + defend)) * RandomUtil.SampleFromNormalDistribution(1.0, 0.1, data.GetRandom());
			killed[type.ordinal()] = (long) Math.min(army2.GetTypedSoldier(type), damage * multiplier);
		}
		if (is_siege) {
			City city = army2.GetBaseCity();
			double attack = effective_soldier * army1_power.GetSiegeAttack();
			double prob = Math.min(param.siege_max_wall_damage_prob, attack / (city.GetTotalPopulation() + 1));
			if (RandomUtil.WhetherToHappend(prob, data.GetRandom())) {
				city.GetImprovements().DecreaseCount(ImprovementType.WALL);
				army2_power.SetWallDamage();
			}
		}
	}
	
	private void SetDamage(Army army, long[] killed) {
		ArmyCombat combat = army.GetCombat();
		combat.SetBattled();
		for (UnitType type : UnitType.values()) {
			combat.SetKilled(type, killed[type.ordinal()]);
		}
	}
	
	private State HandleCombatPrestige(Army loser, Army winner, ArrayList<Army> winners, Tile tile, boolean is_siege) {
		State loser_state = loser.GetState();
		State winner_state = winner.GetState();
		
		if (loser_state.Playable() && winner_state.Playable()) {
			winner_state.GetDiplomacy().DecreaseAttitude(loser_state, param.defeat_attitude_decrease);
			// utils.diplomacy_util.CheckAndResetRealationship(winner_state, loser_state);
		}
		HashSet<State> winner_states = new HashSet<State>();
		winner_states.add(winner_state);
		for (Army army : winners) {
			winner_states.add(army.GetState());
		}

		City city = data.GetCityTerritoryByTile(tile);
		State owner = city.GetOwner();
		boolean defender_is_winner = winner_state == owner || winner_states.contains(owner);
		if (owner.Playable() && defender_is_winner) {
			for (State state : winner_states) {
				if (state == owner) continue;
				state.GetDiplomacy().IncreaseAttitude(owner,
						is_siege ? param.siege_defend_helper_attitude_increase : param.defend_helper_attitude_increase);
			}
		}
		
		for (State state : winner_states) {
			if (winner_state.GetDiplomacy().GetSuzerainty() == state) {
				winner_state = state;
			}
		}
		
		if (loser_state.Playable()) {
			loser_state.Get().prestige -= param.defeat_prestige_decrease;
		}
		for (State state : winner_states) {
			if (state.Playable()) {
				utils.diplomacy_util.IncreasePrestige(state, param.winner_prestige_increase / winner_states.size());
				if (defender_is_winner) {
					if (state != owner && (utils.diplomacy_util.AreAlliance(owner, state) ||
							state.GetDiplomacy().GetSuzerainty() == owner)) {
						utils.diplomacy_util.IncreasePrestige(state,
								is_siege ? param.siege_defend_helper_prestige_increase : param.defend_helper_prestige_increase);
					}
				} else if (is_siege && state.GetDiplomacy().GetSuzerainty() == winner_state) {
					utils.diplomacy_util.IncreasePrestige(state, param.assist_prestige_increase);
				}
			}
		}
		if (!defender_is_winner && is_siege) {
			if (owner.Playable()) {
				owner.Get().prestige -= param.defeat_prestige_decrease;
				State suzerainty = owner.GetDiplomacy().GetSuzerainty();
				if (suzerainty != null) {
					suzerainty.Get().prestige -= param.assist_fail_prestige_decrease;
				}
				for (State state : utils.diplomacy_util.GetAllAlliances(owner)) {
					state.Get().prestige -= param.assist_fail_prestige_decrease;
				}
			}
			utils.diplomacy_util.IncreasePrestige(winner_state, param.winner_prestige_increase);
			return winner_state;
		}
		return null;
	}
	
	private void HandleCityConquered(City city, State conquerer) {
		utils.city_util.HandleCityLost(city, true, ai);
		State old_owner = utils.city_util.ChangeOwner(city, conquerer);
		if (old_owner != null && old_owner.Playable()) {
			if (old_owner.GetOwnedCities().isEmpty()) {
				utils.state_util.StateFall(old_owner, ai);
			} else {
				ai.OnCityLost(old_owner, city);
			}
		}
		ai.OnCityConquered(conquerer, city);
		data.AddMessage(conquerer.GetName() + Texts.conquer + city.GetName());
	}
	
	private void HandelGeneralDeath(Army army) {
		if (army.GetTotalSoldier() > 0) return;
		Person person = army.GetGeneral();
		if (person == null) return;
		if (!RandomUtil.WhetherToHappend(army.IsGarrison() ? param.combat_govener_death_prob :
			param.combat_general_death_prob, data.GetRandom())) {
			return;
		}
		person.OnDead();
		State state = person.GetOwner();
		if (state == null) return;
		if (person.GetRoleType() == RoleType.KING) {
			utils.person_util.CoronateNewKing(state, ai);
		} else {
//			if (!person.IsFake()) {
				data.AddMessage(state.GetName() + Texts.minister + person.GetName() + Texts.dead);
//			}
			person.ResetRole();
			ai.OnPersonDeath(person);
		}
		state.RemovePerson(person);
	}
	
	private DataAccessor data;
	private Parameters param;
	private Utils utils;
	private GameInterface ai;
}
