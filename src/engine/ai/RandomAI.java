package ac.engine.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import ac.data.ArmyData.SoldierType;
import ac.data.CityData.Profession;
import ac.data.base.Pair;
import ac.data.base.Resource;
import ac.data.constant.Ability;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.ConstCityData;
import ac.data.constant.ConstStateData;
import ac.data.constant.Ideologies;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Ideologies.IdeologyType;
import ac.data.constant.Improvement;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Parameters;
import ac.data.constant.Policies;
import ac.data.constant.Policies.Policy;
import ac.data.constant.Role;
import ac.data.constant.Role.RoleType;
import ac.data.constant.Technology.Effect;
import ac.data.constant.Technology.TechnologyType;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.ActionExecutor;
import ac.engine.ai.Estimator.AttackerEstimate;
import ac.engine.ai.Estimator.ChanceEstimate;
import ac.engine.ai.GameInterface.AI;
import ac.engine.ai.Ranker.EnemyComparator;
import ac.engine.ai.Ranker.FriendComparator;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.CityImprovements;
import ac.engine.data.City.CityType;
import ac.engine.data.DataAccessor;
import ac.engine.data.IdKeyedData;
import ac.engine.data.Monarch;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.engine.data.Treaty;
import ac.engine.data.Army.Status;
import ac.engine.util.Utils;
import ac.engine.util.CityUtil.IndustryPopulationDistribution;
import ac.util.ComparatorUtil;
import ac.util.ContainerUtil;
import ac.util.RandomUtil;

public class RandomAI implements GameInterface.AI {
	public static GameInterface.AICreator creator = new GameInterface.AICreator() {
		@Override
		public AI Create(DataAccessor data, ActionExecutor executor, State state) {
			return new RandomAI(data, executor, state);
		}
	};
	
	private RandomAI(DataAccessor data, ActionExecutor executor, State state) {
		this.data = data;
		params = data.GetParam();
		utils = new Utils(data);
		estimator = new Estimator(data);
		this.action_executor = executor;
		this.state = state;
	}
	
	@Override
	public void Process() {
		if (data.GetDate().GetDay() == 1) {
			HandleMonthStart();
			if (data.GetDate().GetMonth() % 3 == 1) {
				HandleSeasonStart();
			}
		}
		CheckConstruction();
		CheckMilitary();
		CheckProfessionPct();
		CheckTaxRate();
	}
	
	private void HandleMonthStart() {
		for (City city : state.GetOwnedCities()) {
			long minimum_consumption = (long) (city.Get().population / 12 * params.pop_growth_cutoff);
			long delta = minimum_consumption - city.Get().remaining_food;
			if (delta > 0) {
				Action action = new Action(ActionType.APPEASE_SUBJECT_WITH_FOOD);
				action.object = city;
				action.quantity = Math.min(delta, state.GetResource().food);
				action_executor.Execute(action);
			}
		}
		HireOfficers();
	}
	
	private void HandleSeasonStart() {
		for (AbilityType type : AbilityType.values()) {
			if (state.GetPolicy().GetPolicy(type) != Policy.NONE) continue;
			CheckPolicy(type);
		}
		Rebase();
	}
	
	@Override
	public void OnTechnologyComplete() {
		Action action = new Action(ActionType.RESEARCH_TECHNOLOGY_TYPE);
		action.object = state;
		action.quantity = RandomUtil.SampleFromUniformDistribution(0, TechnologyType.values().length - 1, data.GetRandom());
		action_executor.Execute(action);
	}
	
	@Override
	public void OnPolicyInvalided(Policy policy, IdKeyedData object, IdKeyedData object2) {
		CheckPolicy(Policies.GetType(policy));
	}
	
	@Override
	public void OnPolicyComplete(Policy policy, IdKeyedData object, IdKeyedData object2) {
		CheckPolicy(Policies.GetType(policy));
	}
	
	@Override
	public void OnCityRiotIncreased(City city, int threshold) {}
	
	@Override
	public void OnCityLost(City city) {
		//Pair<City, ImprovementType> next_construction = next_constructions.get(state);
		if (next_construction != null && next_construction.first == city) {
			// next_constructions.remove(state);
			ChooseNextConstruction();
		}
		Rebase();
	}
	
	@Override
	public void OnCityConquered(City city) {
		Rebase();
	}
	
	@Override
	public void OnMonarchDeath(Monarch person) {
	}
	
	@Override
	public void OnPersonDeath(Person person) {
		HireOfficers();
	}
	
	@Override
	public void OnStart() {
		Rebase();
	}
	
	@Override
	public void OnDiplomacyProposal(Treaty treaty) {
		Action action = new Action(ActionType.RESPOND_TO_TREATY);
		action.object = treaty;
		action.quantity = treaty.UnstableIfReject() ? 1 : 0;
		action_executor.Execute(action);
	}
	
	@Override
	public void OnArmyRouteBlocked(Army army) {
		army.ResetTarget();
	}
	
	@Override
	public void OnInvaded(City city, Army army) {
		AssignDefender(city, army, 0);
	}
	
	private void CheckConstruction() {
		//Pair<City, ImprovementType> next_construction = next_constructions.get(state);
		//Pair<Army, Unit> next_army = next_armies.get(state);
		if (next_construction == null && next_army == null) {
			ChooseNextConstructionOrArmy();
			return;
		}
		if (next_construction != null) {
			if (utils.state_util.IsImprovementAffordable(state, next_construction.first, next_construction.second)) {
				Action action = new Action(ActionType.CONSTRUCT_IMPROVEMENT);
				action.object = next_construction.first;
				action.quantity = next_construction.second.ordinal();
				action_executor.Execute(action);
				next_construction = null;
				//next_constructions.remove(state);
			}
		}
		if (next_army != null) {
			if (utils.state_util.IsRecruitmentAffordable(state, next_army.second)
					&& utils.army_util.IsArmyReinforceable(next_army.first)) {
				City city = ContainerUtil.FindTopWithFilter(state.GetOwnedCities(), new Ranker.CityAvailablePeasantComparator(utils.city_util).reversed(),
						c -> c.GetMilitary().GetRecruitmentInfo() != null);
				if (city != null) {
					Action action = new Action(ActionType.RECRUIT_ARMY);
					action.object = next_army.first;
					action.object2 = city;
					action.object3 = next_army.second;
					action.quantity = ChooseSoldierType(city).ordinal();
					action_executor.Execute(action);
					next_army = null;
				}
				//next_armies.remove(state);
			}
		}
	}
	
	private void CheckMilitary() {
		// Defend
		for (City city : state.GetOwnedCities()) {
			Army city_attacker = utils.city_util.IsUnderSiege(city);
			if (city_attacker != null) {
				AssignDefender(city, city_attacker, 0);
			}
		}
		
		// Maybe treat
		if (state.GetResource().food < utils.state_util.GetFoodExpense(state)) {
			for (Army army : state.GetMilitary().GetArmies()) {
				Army target = army.GetTarget();
				if (target != null && (target.GetTarget() == null || target.GetTarget().GetState() != state)) {
					Action action = new Action(ActionType.RESET_TARGET);
					action.object = army;
					action_executor.Execute(action);
				}
			}
			return;
		}
		
		// Assist Defend
		for (City city : data.GetAllPlayableCities()) {
			State owner = city.GetOwner();
			if (owner == null) continue;
			if (owner.GetDiplomacy().GetSuzerainty() == state || utils.diplomacy_util.AreAlliance(state, owner)) {
				Army city_attacker = utils.city_util.IsUnderSiege(city);
				if (city_attacker != null) {
					Army def = AssignDefender(city, city_attacker, 60);
					if (def != null) {
						Action action = new Action(ActionType.TARGET_ARMY);
					    action.object = def;
					    action.object2 = city_attacker;
					    action_executor.Execute(action);
					    // System.err.println("Assistant " + def.GetFullName() + city_attacker.GetFullName() + " " + city.GetName());
					}
				}
			}
		}
		
		if (state.Get().stability < ConstStateData.kMaxStability) return;
		// Assist Attack
		AttackerEstimate attacker = Estimator.GetAttacker(state);
		if (attacker.army == null || attacker.attack_score <= 0) return;
		for (City city : data.GetAllPlayableCities()) {
			Army city_attacker = utils.city_util.IsUnderSiege(city);
			if (city_attacker == null) continue;
			State attacker_state = city_attacker.GetState();
			if (state.GetDiplomacy().GetSuzerainty() == attacker_state ||
					utils.diplomacy_util.AreAlliance(state, attacker_state)) {
				// Check Diplomacy
				State target_state = city.GetOwner();
				if (utils.diplomacy_util.AreAlliance(state, target_state) ||
					target_state.GetDiplomacy().GetSuzerainty() == state ||
					state.GetDiplomacy().GetSuzerainty() == target_state ||
					utils.diplomacy_util.AreAlly(state, target_state)) continue;
				int estimated_duration_day = estimator.GetEstimatedDurationDays(attacker.army, city, attacker.army.GetBaseCity(),
						attacker.army.GetTotalSoldier());
				if (estimated_duration_day < 90) continue;
				Action action = new Action(ActionType.TARGET_CITY);
				action.object = attacker.army;
				action.object2 = city;
				action_executor.Execute(action);
				// System.err.println("Assistant attack" + attacker.army.GetFullName() + " " + city.GetName());
			}
		}
		
		// Attack
		if (RandomUtil.WhetherToHappend(0.9, data.GetRandom())) return;
		if (HasRiot()) return;
		attacker = Estimator.GetAttacker(state);
		if (attacker.army == null || attacker.attack_score <= 0) return;
		Collection<City> neighbor_cities = utils.state_util.GetNeighborCities(state);
		HashMap<City, Double> target_scores = new HashMap<City, Double>();
		
		for (City city : neighbor_cities) {
			double desired_score = Estimator.GetDesireScore(state, city);
			ChanceEstimate chance_estimate = estimator.GetChance(state, city, attacker);
			if (chance_estimate.chance_score > 0.1) {
				target_scores.put(city,
						desired_score * chance_estimate.chance_score * estimator.GetTargetStateDiscount(state, city.GetOwner()));
			}
		}
		City target_city = ContainerUtil.FindTopWithAttributes(neighbor_cities, target_scores, Comparator.reverseOrder(),
				city -> !target_scores.containsKey(city));
		if (target_city != null) {
			// Check Diplomacy
			State target_state = target_city.GetOwner();
			if (utils.diplomacy_util.AreAlliance(state, target_state)) {
				next_diplomacy_policy = new Pair<Policy, IdKeyedData>(Policy.CEASE_ALLIANCE, target_state);
				return;
			}
			if (target_state.GetDiplomacy().GetSuzerainty() == state) {
				next_diplomacy_policy = new Pair<Policy, IdKeyedData>(Policy.CEASE_SUZERAINTY, target_state);
				return;				
			}
			if (state.GetDiplomacy().GetSuzerainty() == target_state) {
				next_diplomacy_policy = new Pair<Policy, IdKeyedData>(Policy.CEASE_VASSAL, target_state);
				return;				
			}
			if (utils.diplomacy_util.AreAlly(state, target_state)) {
				next_diplomacy_policy = new Pair<Policy, IdKeyedData>(Policy.CEASE_ALLY, target_state);
				return;
			}
			Action action = new Action(ActionType.TARGET_CITY);
			action.object = attacker.army;
			action.object2 = target_city;
			action_executor.Execute(action);
		}
	}
	
	private Army AssignDefender(City city, Army target, int min_duration_day) {
		Army defender = ContainerUtil.FindTopWithFilter(state.GetMilitary().GetArmies(),
			ComparatorUtil.CreatePriorityComparator(
					ComparatorUtil.CreateAttributeComparator(army -> army.GetStatus() == Status.IDLE),
					new Ranker.ArmyDistanceComparator(city.GetPosition(), utils.trans_util),
					Ranker.army_soldier),
			army -> army.GetTotalSoldier() <= 0 || army.GetStatus() == Status.RETREAT ||
			(army.GetTarget() != null && !army.GetTarget().IsGarrison() && army.GetTarget().GetTarget() != null &&
			army.GetTarget().GetTarget().GetState() != state));
		if (defender != null) {
			int estimated_duration_day = estimator.GetEstimatedDurationDays(defender, city, defender.GetBaseCity(),
					defender.GetTotalSoldier());
			if (estimated_duration_day < min_duration_day) return null;
			Action action = new Action(ActionType.TARGET_ARMY);
			action.object = defender;
			action.object2 = target;
			action.flag = false;
			action_executor.Execute(action);
		}
		return defender;
	}
	
	private void ChooseNextConstructionOrArmy() {
		if (ChooseNextArmy()) return;
		ChooseNextConstruction();
	}
	
	private boolean ChooseNextArmy() {
		if (RandomUtil.WhetherToHappend(0.2, data.GetRandom())) return false;
		long max_affordable_soldiers = utils.state_util.GetTotalPopulation(state) / 20;
		long max_soldiers = utils.state_util.GetTotalSoldiers(state);
		if (max_affordable_soldiers - max_soldiers < params.base_recruitment) return false;
		Army target_army = ContainerUtil.FindTop(state.GetMilitary().GetArmies(), Ranker.army_soldier);
		final int[] expected_distribution = { 50, 30, 15, 5 };
		long target_army_max_soldiers = target_army.GetTotalSoldier() + params.base_recruitment;
		long min_balance = 0;
		UnitType selected_type = UnitType.MELEE;
		long selected_soldiers = 0L;
		for (UnitType type : UnitType.values()) {
			long soldiers = target_army.GetTypedSoldier(type);
			long balance = soldiers - target_army_max_soldiers * expected_distribution[type.ordinal()] / 100;
			if (balance < min_balance) {
				selected_type = type;
				min_balance = balance;
				selected_soldiers = soldiers;
			}
		}
		Unit most_advanced_unit = state.GetTechnology().GetMostAdvancedUnit(selected_type);
		Unit second_advanced_unit = state.GetTechnology().GetSecondAdvancedUnit(selected_type);
		double most_advanced_ratio = 0;
		for (Entry<Unit, Long> unit_quantity : target_army.GetUnitQuantities(selected_type).entrySet()) {
			if (unit_quantity.getKey() == most_advanced_unit) {
				most_advanced_ratio = (double)unit_quantity.getValue() / (selected_soldiers + params.base_recruitment);
			}
		}
		//next_armies.put(state, new Pair<Army, Unit>(target_army, most_advanced_ratio > 0.2 ? second_advanced_unit : most_advanced_unit));
		next_army = new Pair<Army, Unit>(target_army, most_advanced_ratio > 0.2 ? second_advanced_unit : most_advanced_unit);
		return true;
	}
	
	private void ChooseNextConstruction() {
		ArrayList<Pair<City, ImprovementType>> candidate_list = new ArrayList<Pair<City, ImprovementType>>();
		ArrayList<Double> probs = new ArrayList<Double>();
		ArrayList<ImprovementType> candidate_improvements = new ArrayList<ImprovementType>();
		for (ImprovementType type : ImprovementType.values()) {
			if (utils.state_util.IsImprovementAvailable(state, type)) candidate_improvements.add(type);
		}
		for (City city : state.GetOwnedCities()) {
			if (city.GetImprovements().GetCurrentConstruction() != null) continue;
			IndustryPopulationDistribution dist = utils.city_util.GetIndustryPopulationDistribution(city);
			CityImprovements impr = city.GetImprovements();
			for (ImprovementType type : candidate_improvements) {
				if (!utils.city_util.IsImprovementAvailable(city, type)) continue;
				if (utils.pop_util.GetAvailableLabor(city) < params.building_labor_cost[type.ordinal()]) continue;
				if (type == ImprovementType.FARM) {
					long total_peasant = utils.city_util.GetProfessionalPopulation(city, Profession.PEASANT);
					int farm = impr.GetCount(ImprovementType.FARM);
					int aqeducted_farm = impr.GetCount(ImprovementType.AQEDUCTED_FARM);
					int irrigated_farm = impr.GetCount(ImprovementType.IRRIGATED_FARM);
					int total_farm = farm + aqeducted_farm + irrigated_farm;
					if (total_farm > 0 && total_peasant / total_farm <= data.GetParam().population_per_farm) continue;
				}
				if (type.ordinal() >= Improvement.kIndustryImprovements[0].ordinal()
						&& type.ordinal() <= Improvement.kIndustryImprovements[Improvement.kIndustryImprovements.length - 1].ordinal()) {
					if (dist.worker_per_improvment <= params.population_per_industry_improvement && impr.GetCount(type) > 0) continue;
				}
				candidate_list.add(new Pair<City, ImprovementType>(city, type));
				probs.add(1.0);
			}
		}
		if (probs.isEmpty()) return;
		int index = RandomUtil.GetRandomIndexFromWeights(probs, data.GetRandom());
		//next_constructions.put(state, candidate_list.get(index));
		next_construction = candidate_list.get(index);
	}
	
	private SoldierType ChooseSoldierType(City city) {
		long allowed_recruitment = utils.state_util.GetAllowedTypedSoldiers(state, SoldierType.RECRUITMENT);
		long existing_recruitment = utils.state_util.GetTypedSoldiers(state, SoldierType.RECRUITMENT);
		long ongoing_recruitment = utils.state_util.GetTypedSoldiersUnderConstruction(state, SoldierType.RECRUITMENT) + params.base_recruitment;
		if (allowed_recruitment >= existing_recruitment + ongoing_recruitment &&
				AffordableRecruitment() > ongoing_recruitment * kSoldierAffordabilityBuffer) {
			return SoldierType.RECRUITMENT;
		}
		long allowed_fubing = utils.state_util.GetAllowedTypedSoldiers(state, SoldierType.FUBING);
		long existing_fubing = utils.state_util.GetTypedSoldiers(state, SoldierType.FUBING);
		long ongoing_fubing = utils.state_util.GetTypedSoldiersUnderConstruction(state, SoldierType.FUBING) + params.base_recruitment;
		if (allowed_fubing >= existing_fubing + ongoing_fubing && AffordableFubing(state) > ongoing_fubing * kSoldierAffordabilityBuffer) {
			return SoldierType.FUBING;
		}
		return SoldierType.CONSCRIPTION;
	}
	
	private long AffordableRecruitment() {
		long food_income = utils.state_util.GetEstimatedAnnualFoodIncome(state);
		Resource<Long> total_yields = utils.state_util.GetMonthlyProduce(state);
		long expense = utils.state_util.GetExpense(state);
		long food_payment = utils.state_util.GetFoodExpense(state);
		long affordable_recruitment = (long) Math.min((total_yields.gold - expense) / utils.state_util.GetSoldierWage(state),
				food_income / 12 + total_yields.food - food_payment);
		for (Army army : state.GetMilitary().GetArmies()) {
			affordable_recruitment += army.GetTypedSoldier(SoldierType.RECRUITMENT);
		}
		return affordable_recruitment;
	}
	
	private long AffordableFubing(State state) {
		return utils.state_util.GetFubingAffordability(state);
	}
	
	private Pair<SoldierType, SoldierType> GetSoldierConversion() {
		long affordable_recruitment = (long) (AffordableRecruitment() / kSoldierAffordabilityBuffer);
		long existing_recruitment = utils.state_util.GetTypedSoldiers(state, SoldierType.RECRUITMENT);
		long ongoing_recruitment = utils.state_util.GetTypedSoldiersUnderConstruction(state, SoldierType.RECRUITMENT);
		long affordable_fubing = (long) (AffordableFubing(state) / kSoldierAffordabilityBuffer);
		long existing_fubing = utils.state_util.GetTypedSoldiers(state, SoldierType.FUBING);
		long ongoing_fubing = utils.state_util.GetTypedSoldiersUnderConstruction(state, SoldierType.FUBING);
		if (affordable_recruitment < existing_recruitment + ongoing_recruitment && existing_recruitment > 0) {
			return new Pair<SoldierType, SoldierType>(SoldierType.RECRUITMENT,
					affordable_fubing >= existing_fubing + params.base_recruitment ? SoldierType.FUBING : SoldierType.CONSCRIPTION);
		} else if (affordable_fubing < existing_fubing + ongoing_fubing && existing_fubing > 0) {
			return new Pair<SoldierType, SoldierType>(SoldierType.FUBING, SoldierType.CONSCRIPTION);
		}
		long max_affordable_soldiers = utils.state_util.GetTotalPopulation(state) / 20;
		long max_soldiers = utils.state_util.GetTotalSoldiers(state);
		long existing_conscription = utils.state_util.GetTypedSoldiers(state, SoldierType.CONSCRIPTION);
		long allowed_recruitment = utils.state_util.GetAllowedTypedSoldiers(state, SoldierType.RECRUITMENT);
		long allowed_fubing = utils.state_util.GetAllowedTypedSoldiers(state, SoldierType.FUBING);
		if (max_affordable_soldiers - max_soldiers < params.base_recruitment && existing_conscription >= params.base_recruitment) {
			if (affordable_recruitment >= existing_recruitment + ongoing_recruitment + params.base_recruitment &&
					allowed_recruitment >= existing_recruitment + ongoing_recruitment + params.base_recruitment) {
				return new Pair<SoldierType, SoldierType>(affordable_fubing > existing_fubing ? SoldierType.FUBING : SoldierType.CONSCRIPTION,
						SoldierType.RECRUITMENT);
			} else if (affordable_fubing >= existing_fubing + ongoing_fubing + params.base_recruitment &&
					allowed_fubing >= existing_fubing + ongoing_fubing + params.base_recruitment) {
				return new Pair<SoldierType, SoldierType>(SoldierType.CONSCRIPTION, SoldierType.FUBING);
			}
		}
		return null;
	}
	
	private void HireOfficers() {
		int person_appeared = 0;
		for (Person person :  data.GetAvailablePeople()) {
			if (person.GetOriginalState() == state) {
				person_appeared++;
			}
		}
		int person_needed = state.GetMilitary().GetArmies().size() + utils.state_util.GetAllowedCounties(state) + 1;
		person_needed = Math.max(person_needed, Ability.kMaxTypes);
		int person_allowed = utils.person_util.GetMaxAllowedOfficer(state);
		int person_to_hire = Math.max(person_appeared, Math.min(person_allowed, person_needed) - state.GetPersons().size());
		for (int i = 0; i < person_to_hire; ++i) {
			if (state.GetResource().gold < utils.state_util.GetHiringCost(state)) continue;
			Action action = new Action(ActionType.HIRE_OFFICER);
			action.object = state;
			action_executor.Execute(action);
		}
		
		ArrayList<Person> candidates = new ArrayList<Person>(state.GetPersons());
		// Assign Officer
		RoleType[] ordered_roles = { RoleType.ADMIN_OFFICER,  RoleType.MILITARY_OFFICER, RoleType.DIPLOMAT_OFFICER};
		for (RoleType role : ordered_roles) {
			if (candidates.isEmpty()) break;
			int role_type_index = role.ordinal() - Role.kMinisters[0].ordinal();
			AbilityType ability_type = AbilityType.values()[role_type_index];
			candidates.sort(ComparatorUtil.CreatePriorityComparator(Ranker.person_abilities.get(ability_type), Ranker.person_ability.reversed()));
			Person person = candidates.get(0);
			Action action = new Action(ActionType.ASSIGN_MINISTER);
			action.object = person;
			action.quantity = role_type_index;
			action_executor.Execute(action);
			candidates.remove(0);
		}
		// Assign governor/general
		candidates.clear();
		candidates.addAll(state.GetPersons());
		candidates.sort(ComparatorUtil.CreatePriorityComparator(Ranker.person_military_ability, Ranker.person_admin_ability.reversed(),
				Ranker.person_diplomacy_ability));
		for (Army army : state.GetMilitary().GetArmies()) {
			if (candidates.isEmpty()) break;
			Person person = candidates.get(0);
			Action action = new Action(ActionType.ASSIGN_GENERAL);
			action.object = person;
			action.object2 = army;
			action_executor.Execute(action);
			candidates.remove(0);
		}
		candidates.sort(ComparatorUtil.CreatePriorityComparator(Ranker.person_admin_ability, Ranker.person_military_ability,
				Ranker.person_diplomacy_ability));
		for (City city : state.GetOwnedCities()) {
			if (city.GetType() != CityType.CAPITAL && city.GetType() != CityType.COUNTY) continue;
			if (candidates.isEmpty()) break;
			Person person = candidates.get(0);
			Action action = new Action(ActionType.ASSIGN_GOVERNOR);
			action.object = person;
			action.object2 = city;
			action_executor.Execute(action);
			candidates.remove(0);
		}
	}
	
	private void CheckPolicy(AbilityType type) {
		// Check assignment
		Pair<Policy, IdKeyedData> prechosen_policy = null;
		if (type == AbilityType.DIPLOMACY) {
			prechosen_policy = next_diplomacy_policy;
			next_diplomacy_policy = null;
		}
		if (prechosen_policy != null) {
			Action action = new Action(ActionType.ADOPT_POLICY);
			action.object = state;
			action.object2 = prechosen_policy.second;
			action.quantity = prechosen_policy.first.ordinal();
			action_executor.Execute(action);
			return;
		}
		// Randomly adopt
		ArrayList<Pair<Policy, IdKeyedData>> candidates = new ArrayList<Pair<Policy, IdKeyedData>>();
		HashMap<Policy, IdKeyedData> target2s = new HashMap<Policy, IdKeyedData>();
		int context_int = -1;
		ArrayList<Double> probs = new ArrayList<Double>();
		Pair<SoldierType, SoldierType> convert_soldier_target_data = null;
		for (Policy policy : Policies.policies.get(type)) {
			if (policy == Policy.NONE) continue;
			if (!utils.state_util.IsPolicyAvailable(state, policy) || !utils.state_util.IsPolicyNeccessary(state, policy)) continue;
			IdKeyedData target = null;
			double probability = 1.0;
			ArrayList<State> enemies = new ArrayList<State>();
			ArrayList<State> friends = new ArrayList<State>();
			if (type == AbilityType.DIPLOMACY) {
				DiplomacyPerspective(enemies, friends);
			}
			switch (policy) {
			case CONVERT_FOREIGNERS:
				City most_foreign_city = ContainerUtil.FindTop(state.GetOwnedCities(), Ranker.city_foreigners);
				if (most_foreign_city.GetPopulation().GetPopRatio(0) >= 1) continue;
				target = most_foreign_city;
				break;
			case INCREASE_HAPPINESS:
				City unhappiest_city = ContainerUtil.FindTop(state.GetOwnedCities(), Ranker.city_unhappiness);
				if (unhappiest_city.GetPopulation().GetHappiness() >= ConstCityData.kMaxHappiness) continue;
				target = unhappiest_city;
				probability = 100;
				break;
			case ESTABLISH_COUNTY:
				target = ContainerUtil.FindTopWithFilter(state.GetOwnedCities(), Ranker.city_foreigners.reversed(), Filter.county_candidate_filter);
				break;
			case SUPPRESS_REVOLT:
				City most_riot_city = ContainerUtil.FindTop(state.GetOwnedCities(), Ranker.city_riot_points);
				if (most_riot_city == null || most_riot_city.Get().riot <= 0) continue;
				target = most_riot_city;
				if (most_riot_city.Get().riot >= params.suppress_revolt_riot_decrease) probability = 200;
				break;
			case INCREASE_RELATIONSHIP:
				for (int i = 0; i < friends.size(); ++i) {
					if (state.GetDiplomacy().GetAttitude(friends.get(i)) + params.policy_attitude_increase <=
							utils.state_util.GetMaxRelationship(state, friends.get(i))) {
						target = friends.get(i);
						break;
					}
				}
				if (target == null) continue;
				break;
			case DECREASE_RELATIONSHIP:
				if (enemies.isEmpty()) continue;
				target = enemies.get(0);
				if (enemies.size() >= 2) {
					target2s.put(policy, enemies.get(1));
				} else if (!friends.isEmpty()) {
					target2s.put(policy, friends.get(0));
				} else {
					continue;
				}
				break;
			case PROPOSE_ALLY:
				for (int i = 0; i < friends.size(); ++i) {
					if (!utils.diplomacy_util.AreAlly(state, friends.get(i)) &&
							state.GetDiplomacy().GetAttitude(friends.get(i)) >= params.min_attitude_for_ally) {
						target = friends.get(i);
						break;
					}
				}
				if (target == null) continue;
				break;
			case PROPOSE_OPEN_BORDER:
				for (int i = 0; i < friends.size(); ++i) {
					if (!utils.diplomacy_util.BorderOpened(friends.get(i), state) &&
							state.GetDiplomacy().GetAttitude(friends.get(i)) >= params.min_attitude_for_open_border) {
						target = friends.get(i);
						break;
					}
				}
				if (target == null) continue;
				break;				
			case PROPOSE_VASSAL:
			case PROPOSE_SUZERAINTY:
			case PROPOSE_ALLIANCE:
				for (int i = 0; i < friends.size(); ++i) {
					State friend = friends.get(i);
					if (!utils.diplomacy_util.AreAlliance(state, friend) &&
							state.GetDiplomacy().GetSuzerainty() != friend &&
									friend.GetDiplomacy().GetSuzerainty() != state &&
							state.GetDiplomacy().GetAttitude(friend) >= params.min_attitude_for_alliance) {
						if (policy == Policy.PROPOSE_VASSAL) {
							if (state.GetDiplomacy().GetSuzerainty() != null || !utils.diplomacy_util.BorderOpened(state, friend)) continue;
							if (utils.state_util.GetTotalPopulation(state) > utils.state_util.GetTotalPopulation(friend) * 0.25) continue;
						} else if (policy == Policy.PROPOSE_SUZERAINTY) {
							if (friend.GetDiplomacy().GetSuzerainty() != null || !utils.diplomacy_util.BorderOpened(friend, state)) continue;
							if (utils.state_util.GetTotalPopulation(state) < utils.state_util.GetTotalPopulation(friend) * 4) continue;
							if (state.Get().prestige < params.min_prestige_for_suzerainy) continue;
						} else {
							if (!utils.diplomacy_util.BorderOpened(state, friend) && !utils.diplomacy_util.BorderOpened(friend, state)) continue;
							if (utils.state_util.GetTotalPopulation(state) < utils.state_util.GetTotalPopulation(friend) * 0.25 ||
									utils.state_util.GetTotalPopulation(state) > utils.state_util.GetTotalPopulation(friend) * 4) continue;
						}
						target = friends.get(i);
						break;
					}
				}
				if (target == null) continue;
				break;	
			case INCREASE_PRESTIGE:
				if (state.Get().prestige + params.policy_prestige_increase > params.policy_prestige_increase_cap) continue;
				probability = state.Get().prestige < 0 ? 10 : 0.5;
				break;
			case INCREASE_STABILITY:
				if (state.Get().stability >= ConstStateData.kMaxStability) continue;
				probability = 1000;
				break;
			case MILITARY_TRAINING:
				break;
			case ADOPT_IDEOLOGY:
				if (state.Get().stability < ConstStateData.kMaxStability) continue;
				if (HasRiotRisk()) continue;
				int ideology_type = RandomUtil.SampleFromUniformDistribution(0, Ideologies.kMaxIdeologyTypes - 1, data.GetRandom());
				long deterministic_choice = ideology_type * 107 + state.GetName().hashCode() * 101 + data.GetRandomSeed();
				ArrayList<Ideology> ideology_candidates =  utils.state_util.GetAvailableIdelogies(state, IdeologyType.values()[ideology_type]);
				if (ideology_candidates.isEmpty()) continue;
				context_int = ideology_candidates.get((int)(deterministic_choice % ideology_candidates.size())).ordinal();
				if (state.GetPolicy().GetIdeology(IdeologyType.values()[ideology_type]).ordinal() == context_int) continue;
				probability = 1;
				break;
			default:
				continue;
			}
			candidates.add(new Pair<Policy, IdKeyedData>(policy, target));
			probs.add(probability);
		}
		if (type == AbilityType.MILITARY) {
			convert_soldier_target_data = GetSoldierConversion();
			if (convert_soldier_target_data != null) {
				IdKeyedData target =
						ContainerUtil.FindTop(state.GetMilitary().GetArmies(), Ranker.army_soldier_types.get(convert_soldier_target_data.first));
				if (convert_soldier_target_data.first == SoldierType.RECRUITMENT) {
					candidates.add(new Pair<Policy, IdKeyedData>(convert_soldier_target_data.second == SoldierType.FUBING ?
							Policy.CONVERT_RECRUITMENT_SOLDIERS_TO_FUBING : Policy.CONVERT_RECRUITMENT_SOLDIERS_TO_CONSCRIPTION, target));
				} else {
					candidates.add(new Pair<Policy, IdKeyedData>(convert_soldier_target_data.second == SoldierType.FUBING ?
							Policy.CONVERT_CONSCRIPTION_SOLDIERS_TO_FUBING : Policy.CONVERT_CONSCRIPTION_SOLDIERS_TO_RECRUITMENT, target));
				}
				probs.add(100.0);
			}
		}
		if (candidates.isEmpty()) return;
		int index = RandomUtil.GetRandomIndexFromWeights(probs, data.GetRandom());
		Action action = new Action(ActionType.ADOPT_POLICY);
		Policy policy = candidates.get(index).first;
		action.object = state;
		action.object2 = candidates.get(index).second;
		action.object3 = target2s.get(policy);
		action.quantity = policy.ordinal();
		action.quantity2 = context_int;
		action_executor.Execute(action);
	}
	
	private void Rebase() {
		ArrayList<City> cities = new ArrayList<City>(state.GetOwnedCities());
		cities.sort(new Comparator<City>() {
			@Override
			public int compare(City o1, City o2) {
				boolean next_to_enemy_1 = NextToEnemy(o1);
				boolean next_to_enemy_2 = NextToEnemy(o2);
				if (next_to_enemy_1 == next_to_enemy_2) {
					return (int) (o2.GetTotalPopulation() - o1.GetTotalPopulation());
				}
				return next_to_enemy_1 ? -1 : 1;
			}
			
			private boolean NextToEnemy(City city) {
				for (City neighbor : city.GetNeighbors()) {
					State state = neighbor.GetOwner();
					if (state != city.GetOwner() && state.Playable()) return true;
				}
				return false;
			}
		});
		int index = 0;
		ArrayList<Army> armies = state.GetMilitary().GetArmies();
		for (City city : cities) {
			Action action = new Action(ActionType.REBASE_ARMY);
			action.object = armies.get(index++);
			action.object2 = city;
			action_executor.Execute(action);
			if (index >= armies.size()) return;
		}
		for (; index < armies.size(); ++index) {
			Action action = new Action(ActionType.REBASE_ARMY);
			action.object = armies.get(index);
			action.object2 = state.GetCapital();
			action_executor.Execute(action);
		}
	}
	
	private void DiplomacyPerspective(ArrayList<State> enemies, ArrayList<State> friends) {
		long total_pop = utils.state_util.GetTotalPopulation(state);
		EnemyComparator enemy_comparator = new EnemyComparator(state);
		PriorityQueue<State> enemy_queue = new PriorityQueue<State>(enemy_comparator);
		enemy_queue.addAll(enemy_comparator.GetCandidates());
		while (!enemy_queue.isEmpty()) {
			State enemy = enemy_queue.poll();
			total_pop -= enemy_comparator.GetThreat(enemy);
			if (!enemies.isEmpty() && total_pop < 0) break;
			enemies.add(enemy);
		}
		for (State candidate : data.GetAllPlayableStates()) {
			if (candidate == state || enemies.contains(candidate)) continue;
			friends.add(candidate);
		}
		friends.sort(new FriendComparator(state, enemies, utils.state_util));
	}
	
	private void CheckProfessionPct() {
		if (!state.GetTechnology().HasEffect(Effect.UNBLOCK_GARRISON)) return;
		for (City city : state.GetOwnedCities()) {
			if (city.GetPopulation().GetProfessionTargetPct(Profession.SOLDIER) == 0) {
				Action action = new Action(ActionType.ALLOCATE_PROFESSION);
				action.object = city;
				action.quantity = city.GetPopulation().GetProfessionTargetPct(Profession.WORKER);
				action.quantity2 = city.GetPopulation().GetProfessionTargetPct(Profession.MERCHANT);
				action.quantity3 = 5;
				action_executor.Execute(action);
			}
		}
	}
	
	private void CheckTaxRate() {
		if (state.GetTechnology().HasEffect(Effect.UNBLOCK_TAX_RATE_CHANGE)) {
			if (state.GetEconomic().GetFoodTaxPercentage() > 5) {
				Action action = new Action(ActionType.CHANGE_FOOD_TAX);
				action.object = state;
				action.quantity = 5;
				action_executor.Execute(action);
			}
		}
	}
	
	private boolean HasRiotRisk() {
		for (City city : state.GetOwnedCities()) {
			if (utils.city_util.HasRiotRisk(city)) return true;
		}
		return false;
	}
	
	private boolean HasRiot() {
		for (City city : state.GetOwnedCities()) {
			if (utils.city_util.GetRiotPoint(city) > 0) return true;
		}
		return false;
	}

	private State state;
	private DataAccessor data;
	private Parameters params;
	private Utils utils;
	private Estimator estimator;
	
	private ActionExecutor action_executor;
	//private HashMap<State, Pair<City, ImprovementType>> next_constructions = new HashMap<State, Pair<City, ImprovementType>>();
	private Pair<City, ImprovementType> next_construction = null;
	//private HashMap<State, Pair<Army, Unit>> next_armies = new HashMap<State, Pair<Army, Unit>>();
	private Pair<Army, Unit> next_army = null;
	//private HashMap<State, Pair<Policy, IdKeyedData>> next_diplomacy_policy = new HashMap<State, Pair<Policy, IdKeyedData>>();
	Pair<Policy, IdKeyedData> next_diplomacy_policy = null;
	
	private static final double kSoldierAffordabilityBuffer = 1.5;
} 