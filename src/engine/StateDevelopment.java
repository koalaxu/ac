package ac.engine;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import ac.data.ArmyData.SoldierType;
import ac.data.TreatyData.Relationship;
import ac.data.base.Date;
import ac.data.base.Resource;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.ConstStateData;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Parameters;
import ac.data.constant.Policies;
import ac.data.constant.Policies.Policy;
import ac.data.constant.Role.RoleType;
import ac.data.constant.Technology.Effect;
import ac.data.constant.Texts;
import ac.engine.ai.GameInterface;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.City.CityType;
import ac.engine.data.CityPopulation;
import ac.engine.data.DataAccessor;
import ac.engine.data.IdKeyedData;
import ac.engine.data.Monarch;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.engine.data.StateDiplomacy;
import ac.engine.data.StateEconomic;
import ac.engine.data.Treaty;
import ac.engine.util.Utils;
import ac.util.RandomUtil;

public class StateDevelopment {
	protected StateDevelopment(DataAccessor data, GameInterface ai) {
		this.data = data;
		this.param = data.GetParam();
		this.utils = data.GetUtils();
		this.ai = ai;
	}
	
	public void HandleMonthlyProduce(State state) {
		StateEconomic economic = state.GetEconomic();
		Resource<Long> resource = state.GetResource();
		Resource<Long> total_yields = utils.state_util.GetMonthlyProduce(state);	
		data.GetMarket().AddResourceToMarket(total_yields, param.market_produce_ratio, false);
		Resource<Long> export = utils.state_util.GetExportResources(state, total_yields);
		long fixed_expense = utils.state_util.GetExpense(state);
		long net_income = utils.state_util.GetNetIncome(state, total_yields, export, fixed_expense);
		long dynamic_expense = 0;
		if (net_income > 0) {
			economic.SetNetIncome(net_income);
			long tech_expense = economic.GetTechBudget();
			dynamic_expense += tech_expense;
			if (state.GetTechnology().IncreaseResearchProgress(utils.state_util.GetTechBoost(tech_expense, utils.state_util.GetTechMultiplier(state)))) {
				ai.OnTechnologyComplete(state);
			}
		} else {
			state.GetEconomic().SetNetIncome(0);
		}
		Resource.AddResource(resource, total_yields);
		Resource.SubtractResource(resource, export);
		
		long remaining_gold = state.GetResource().gold;
		remaining_gold = remaining_gold + utils.state_util.GetExportIncome(state, export) - fixed_expense - dynamic_expense;
		HandleFoodPayment(state);
		double uncovered_expense_ratio = 0;
		if (remaining_gold < 0) {
			remaining_gold  = HandleDebt(state, remaining_gold, resource);
			if (remaining_gold < 0) {
				uncovered_expense_ratio = (double)-remaining_gold / fixed_expense;
				remaining_gold = 0;
			}
		}
		HandleUncoveredExpense(state, uncovered_expense_ratio);
		resource.gold = remaining_gold;
	}
	
	public void CollectGovernmentPoints(State state) {
		for (AbilityType type : AbilityType.values()) {
			Policy policy = state.GetPolicy().GetPolicy(type);
			if (policy == Policy.NONE) continue;
			int progress = state.GetPolicy().IncreasePolicyProgress(type, utils.state_util.GetPolicyPoint(state, type));
			if (progress >= Policies.GetCost(policy)) {
				IdKeyedData object = state.GetPolicy().GetPolicyObject(type);
				IdKeyedData object2 = state.GetPolicy().GetPolicyObject2(type);
				long context_quantity = state.GetPolicy().GetPolicyContextQuantity(type);
				switch (policy) {
				case CONVERT_FOREIGNERS:
					CityPopulation race_pop = ((City)object).GetPopulation();
					int num_races = race_pop.NumRaces();
					double owner_race_pct = race_pop.GetPopRatio(0);
					double policy_convert_foreigner_ratio = param.policy_convert_foreigner_ratio;
					if (state.GetPolicy().HasIdeology(Ideology.CONFUCIAN)) {
						policy_convert_foreigner_ratio *= 1.5;
					}
					double conversion_rate = policy_convert_foreigner_ratio * (state.GetTechnology().HasEffect(Effect.BOOST_FOREIGNER_CONVERSION) ? 2 : 1);
					double discount = Math.max(0, (1.0 - owner_race_pct - conversion_rate) / (1.0 - owner_race_pct));
					for (int i = 1; i <= num_races; ++i) {
						race_pop.SetPopRatio(i, race_pop.GetPopRatio(i) * discount);
					}
					race_pop.Clear();
					break;
				case INCREASE_HAPPINESS:
					((City)object).GetPopulation().ChangeHappiness(param.policy_happiness_increase);
					break;
				case SUPPRESS_REVOLT:
					((City)object).Get().riot = Math.max(0, ((City)object).Get().riot - param.suppress_revolt_riot_decrease);
					((City)object).GetPopulation().ChangeHappiness(-param.suppress_revolt_happiness_decrease);
					break;
				case ESTABLISH_COUNTY:
					if (((City)object).GetType() != CityType.CAPITAL && utils.state_util.IsNewCountyAllowed(state)) {
						((City)object).SetType(CityType.COUNTY);
					}
					break;
				case ESTABLISH_JIMI_COUNTY:
					if (((City)object).GetType() == CityType.NONE) {
						((City)object).SetType(CityType.JIMI_COUNTY);
					}
					break;
				case CHANGE_CAPITAL:
					utils.city_util.ChangeCapital((City)object);
					break;
				case INCREASE_RELATIONSHIP:
					IncreaseRelationship(state, (State)object);
					break;
				case DECREASE_RELATIONSHIP:
					DecreaseRelationship((State)object, (State)object2);
					DecreaseRelationship((State)object2, (State)object);
					break;
				case DENOUNCE:
					DecreaseRelationship((State)object, (State)object2);
					break;
				case PROPOSE_ALLY:
					InitiateTreaty(state, (State)object, Relationship.ALLY);
					break;
				case PROPOSE_OPEN_BORDER:
					InitiateTreaty(state, (State)object, Relationship.OPEN_BORDER);
					break;
				case PROPOSE_VASSAL:
					InitiateTreaty(state, (State)object, Relationship.VASSAL);
					break;
				case PROPOSE_SUZERAINTY:
					InitiateTreaty(state, (State)object, Relationship.SUZERAINTY);
					break;
				case PROPOSE_ALLIANCE:
					InitiateTreaty(state, (State)object, Relationship.ALLIANCE);
					break;
				case CEASE_ALLY:
					CeaseTreaty(state, (State)object, Relationship.ALLY);
					break;
				case CEASE_OPEN_BORDER:
					CeaseTreaty(state, (State)object, Relationship.OPEN_BORDER);
					break;
				case CEASE_VASSAL:
					CeaseTreaty(state, (State)object, Relationship.VASSAL);
					break;
				case CEASE_SUZERAINTY:
					CeaseTreaty(state, (State)object, Relationship.SUZERAINTY);
					break;
				case CEASE_ALLIANCE:
					CeaseTreaty(state, (State)object, Relationship.ALLIANCE);
					break;
				case INCREASE_PRESTIGE:
					state.Get().prestige = Math.min(param.policy_prestige_increase_cap, state.Get().prestige + param.policy_prestige_increase);
					break;
				case INCREASE_STABILITY:
					state.Get().stability = Math.min(ConstStateData.kMaxStability, state.Get().stability + 1);
					break;
				case CONVERT_RECRUITMENT_SOLDIERS_TO_FUBING:
					ConvertSoldierType(SoldierType.RECRUITMENT, SoldierType.FUBING, (Army)object);
					break;
				case CONVERT_RECRUITMENT_SOLDIERS_TO_CONSCRIPTION:
					ConvertSoldierType(SoldierType.RECRUITMENT, SoldierType.CONSCRIPTION, (Army)object);
					break;
				case CONVERT_CONSCRIPTION_SOLDIERS_TO_FUBING:
					ConvertSoldierType(SoldierType.CONSCRIPTION, SoldierType.FUBING, (Army)object);
					break;
				case CONVERT_CONSCRIPTION_SOLDIERS_TO_RECRUITMENT:
					ConvertSoldierType(SoldierType.CONSCRIPTION, SoldierType.RECRUITMENT, (Army)object);
					break;
				case MILITARY_TRAINING:
					MilitaryTraing(state);
					break;
				case ADOPT_IDEOLOGY:
					AdoptIdeology(state, Ideology.values()[(int)context_quantity]);
					break;
				case MIGRATE:
					Migrate(state, (City)object, (City)object2, context_quantity);
				case NONE:
				default:
					break;
				}
				state.GetPolicy().ResetPolicy(Policies.GetType(policy));
				ai.OnPolicyComplete(state, policy, object, object2);
			}
		}
	}

	public void CoronateNewKing(State state) {
		utils.person_util.CoronateNewKing(state, ai);
	}
	
	public void CheckPersonDeath() {
		for (Person person : data.PollDeadPeople()) {
			person.OnDead();
			State state = person.GetOwner();
			if (state == null || !state.Playable()) continue;
			if (person.GetRoleType() == RoleType.KING) {
				utils.person_util.CoronateNewKing(state, ai);
				ai.OnMonarchDeath((Monarch)person);
			} else {
				if (!person.IsFake()) {
					data.AddMessage(state.GetName() + Texts.minister + person.GetName() + Texts.dead);
				}
				person.ResetRole();
				ai.OnPersonDeath(person);
			}
			state.RemovePerson(person);
		}
	}
	
	public void CheckArmyNumber(State state) {
		utils.state_util.CheckArmyNumber(state);
	}
	
	private void HandleFoodPayment(State state) {
		// Recruitment
		long food_payment = utils.state_util.GetFoodPayment(state);
		if (food_payment > 0) {
			long balance = state.GetResource().food - food_payment;
			if (balance < 0) {
				double ratio = (double)(balance) / food_payment;
				double soldier_decrease_ratio = Math.min(-ratio, param.food_shortage_army_decrease);
				for (Army army : state.GetMilitary().GetArmies()) {
					army.DecreaseTypedSoldier(SoldierType.RECRUITMENT, soldier_decrease_ratio);
					// morale
					double recruitment_soldier_ratio = army.GetSoldierTypeDistribution()[SoldierType.RECRUITMENT.ordinal()];
					army.ChangeMorale(ratio * recruitment_soldier_ratio * param.food_shortage_morale_decrease,
							army.GetSoldierTypeDistribution()[SoldierType.CONSCRIPTION.ordinal()]);
				}
				balance = 0;
			}
			state.GetResource().food = balance;
		}
		
		// Fubing
		long affordable_fubing = utils.state_util.GetFubingAffordability(state);
		long total_fubing = utils.state_util.GetTypedSoldiers(state, SoldierType.FUBING);
		if (state.GetPolicy().HasIdeology(Ideology.DUJUN)) {
			affordable_fubing *= 2;
		}
		if (affordable_fubing < total_fubing) {
		long decrease = Math.min(total_fubing - affordable_fubing, (long)(total_fubing * param.food_shortage_army_decrease));
			for (Army army : state.GetMilitary().GetArmies()) {
				army.DecreaseTypedSoldier(SoldierType.FUBING, (double)decrease / total_fubing);
				// morale
				double fubing_soldier_ratio = army.GetSoldierTypeDistribution()[SoldierType.FUBING.ordinal()];
				army.ChangeMorale(- 1.0 * fubing_soldier_ratio * param.food_shortage_morale_decrease,
						army.GetSoldierTypeDistribution()[SoldierType.CONSCRIPTION.ordinal()]);
			}
		}
	}

	
	private long HandleDebt(State state, long balance, Resource<Long> resource) {
		double debt = -balance;
		double asset = 0;
		//Resource<Long> resource = state.GetResource();
		asset = resource.Aggregate((n,  price) -> n * price, data.GetMarket()::GetPrice);
		double sell_ratio = (asset <= debt) ? 1.0 : (debt / asset);
		long income = data.GetMarket().AddResourceToMarket(resource, sell_ratio, true);
		return income - balance;
	}
	
	private void HandleUncoveredExpense(State state, double uncovered_expense_ratio) {
		if (uncovered_expense_ratio > 0) {
			// Lost one improvement
			for (City city : state.GetOwnedCities()) {
				ImprovementType type = ImprovementType.values()[RandomUtil.SampleFromUniformDistribution(
						0, ImprovementType.values().length - 1, data.GetRandom())];
				if (city.GetImprovements().GetCount(type) > 1) city.GetImprovements().DecreaseCount(type);
				city.GetMilitary().GetGarrison().ChangeTrainingLevel(param.unit_training_monthly_change * -1 * uncovered_expense_ratio);
			}
			for (Army army : state.GetMilitary().GetArmies()) {
				double recruitment_soldier_ratio = army.GetSoldierTypeDistribution()[SoldierType.RECRUITMENT.ordinal()];
				army.ChangeMorale(param.unit_morale_monthly_change * -1 * uncovered_expense_ratio * recruitment_soldier_ratio,
						1.0 - recruitment_soldier_ratio);
				army.ChangeTrainingLevel(param.unit_training_monthly_change * -1 * uncovered_expense_ratio);
			}
			return;
		} 
		// Covered
		for (City city : state.GetOwnedCities()) {
			city.GetMilitary().GetGarrison().ChangeTrainingLevel(param.unit_training_monthly_change);
			city.GetMilitary().GetGarrison().ChangeMorale(param.unit_morale_monthly_change);
		}
		for (Army army : state.GetMilitary().GetArmies()) {
			if (utils.army_util.IsArmyReinforceable(army)) {
				army.ChangeTrainingLevel(param.unit_training_monthly_change);
				army.ChangeMorale(param.unit_morale_monthly_change);
			}
		}
	}
	
	private void ConvertSoldierType(SoldierType from, SoldierType to, Army army) {
		HashMap<City, Long> conscription_changes = army.ConvertSoldiers(from, to, param.base_recruitment);
		if (conscription_changes == null) return;
		for (Entry<City, Long> entry : conscription_changes.entrySet()) {
			entry.getKey().Get().population += entry.getValue();
		}
	}
	
	private void IncreaseRelationship(State state, State target) {
		int cap = utils.state_util.GetMaxRelationship(state, target);
		state.GetDiplomacy().IncreaseAttitude(target, param.policy_attitude_increase, cap);
		target.GetDiplomacy().IncreaseAttitude(state, param.policy_attitude_self_increase, cap);
	}
	
	private void DecreaseRelationship(State state, State target) {
		state.GetDiplomacy().DecreaseAttitude(target, param.policy_attitude_decrease);
		utils.diplomacy_util.CheckAndResetRealationship(state, target);
	}
	
	private void InitiateTreaty(State state, State target, Relationship relation) {
		Treaty treaty = data.CreateNewTreaty(state, target, relation, data.GetDate().CreateDate(param.treaty_expire_days),
				utils.diplomacy_util.UnstableIfRejected(state));
		ai.OnDiplomacyProposal(treaty);
	}
	
	private void CeaseTreaty(State state, State target, Relationship relation) {
		StateDiplomacy state_diplomacy = state.GetDiplomacy();
		StateDiplomacy target_diplomacy = target.GetDiplomacy();
		int attitude = state_diplomacy.GetAttitude(target) + param.delta_attitude_for_free_cancellation;
		if (relation == Relationship.ALLIANCE) {
			utils.diplomacy_util.CancelAlliance(state, target);
			if (attitude > param.min_attitude_for_alliance) {
				state.Get().prestige -= param.prestige_decrease_for_treaty_cancellation;
			}
		} else if (relation == Relationship.SUZERAINTY) {
			if (target_diplomacy.GetSuzerainty() == state) {
				utils.diplomacy_util.CancelVassalage(state, target);
				if (attitude > param.min_attitude_for_alliance) {
					state.Get().prestige -= param.prestige_decrease_for_treaty_cancellation;
				}
			}
		} else if (relation == Relationship.VASSAL) {
			if (state_diplomacy.GetSuzerainty() == target) {
				utils.diplomacy_util.CancelVassalage(target, state);
				if (attitude > param.min_attitude_for_alliance) {
					state.Get().prestige -= param.prestige_decrease_for_treaty_cancellation;
				}
			}
		} else if (relation == Relationship.OPEN_BORDER) {
			if (!utils.diplomacy_util.AreAlliance(state, target) && target_diplomacy.GetSuzerainty() != state) {
				CheckTrespass(state, target);
				target_diplomacy.SetOpenBorder(state, false);
				if (attitude > param.min_attitude_for_open_border) {
					state.Get().prestige -= param.prestige_decrease_for_treaty_cancellation;
				}
			}
		} else if (relation == Relationship.ALLY) {
			if (!utils.diplomacy_util.AreAlliance(state, target) && state_diplomacy.GetSuzerainty() != target &&
					target_diplomacy.GetSuzerainty() != state) {
				CheckTrespass(state, target);
				target_diplomacy.SetOpenBorder(state, false);
				state_diplomacy.SetOpenBorder(target, false);
				utils.diplomacy_util.CancelAlly(state, target);
				if (attitude > param.min_attitude_for_ally) {
					state.Get().prestige -= param.prestige_decrease_for_treaty_cancellation;
				}
			}
		}
	}
	
	private void CheckTrespass(State state, State target) {
		for (Army army : target.GetMilitary().GetArmies()) {
			if (utils.army_util.GetPositionOwner(army) == state) {
				army.SetRetreat(true);
			}
		}
		if (utils.diplomacy_util.BorderOpened(target, state)) {
			for (Army army : state.GetMilitary().GetArmies()) {
				if (utils.army_util.GetPositionOwner(army) == target) {
					state.Get().prestige -= param.prestige_decrease_for_trespassing;
					return;
				}
			}
		}
	}
	
	private void MilitaryTraing(State state) {
		for (Army army : state.GetMilitary().GetArmies()) {
			if (utils.army_util.IsArmyReinforceable(army)) {
				army.ChangeTrainingLevel(param.unit_training_policy_increase);
				army.ChangeMorale(param.unit_morale_policy_increase);
			}
		}
		for (City city : state.GetOwnedCities()) {
			city.GetMilitary().GetGarrison().ChangeTrainingLevel(param.unit_training_policy_increase);
			city.GetMilitary().GetGarrison().ChangeMorale(param.unit_morale_policy_increase);
		}
	}
	
	private void AdoptIdeology(State state, Ideology ideology) {
		state.GetPolicy().SetIdeology(ideology);
		utils.state_util.DecreaseStability(state);
	}
	
	
	private void Migrate(State state, City from, City to, long context_quantity) {
		if (from.GetOwner() != state || to.GetOwner() != state) return;
		if (from.GetTransportation(to) == Integer.MAX_VALUE) return;
		long quantity = context_quantity;
		quantity = Math.min(quantity, from.GetTotalPopulation() - param.city_min_population);
		quantity = Math.min(quantity, utils.pop_util.GetAvailableLabor(from));
		if (quantity < 0) return;
		from.Get().labor *= (from.GetTotalPopulation() - quantity) / from.GetTotalPopulation();
		utils.pop_util.CityReducePopulation(from, quantity);
		double migration_cost = Math.min((param.migration_cost_base + from.GetTransportation(to)) / param.migration_cost_denominator, param.max_migration_cost);
		quantity -= quantity * migration_cost;
		TreeMap<State, Double> race_pop = new TreeMap<State, Double>();
		for (int i = 1; i < from.GetPopulation().NumRaces(); ++i) {
			race_pop.put(from.GetPopulation().GetRace(i), quantity * from.GetPopulation().GetPopRatio(i));
		}
		for (int i = 1; i < to.GetPopulation().NumRaces(); ++i) {
			State race = to.GetPopulation().GetRace(i);
			race_pop.put(race, to.Get().population * from.GetPopulation().GetPopRatio(i) + race_pop.getOrDefault(race, 0.0));
		}
		to.Get().population += quantity;
		for (Entry<State, Double> pair : race_pop.entrySet()) {
			pair.setValue(pair.getValue() / to.Get().population);
		}
		to.GetPopulation().ResetPopRatio(race_pop);
		int day_passed = data.GetDate().GetDifference(new Date(data.GetDate().GetYear(), 1, 1));
		to.Get().labor += day_passed * quantity;
	}
	
	private DataAccessor data;
	private Parameters param;
	private Utils utils;	
	private GameInterface ai;
}
