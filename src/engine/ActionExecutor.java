package ac.engine;

import ac.data.ArmyData.SoldierType;
import ac.data.base.Resource;
import ac.data.base.Resource.ResourceType;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Parameters;
import ac.data.constant.Policies.Policy;
import ac.data.constant.Role;
import ac.data.constant.Role.RoleType;
import ac.data.constant.Technology.Effect;
import ac.data.constant.Technology.TechnologyType;
import ac.data.constant.Unit;
import ac.engine.Action.ActionType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.IdKeyedData;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.engine.data.StateEconomic;
import ac.engine.data.Treaty;
import ac.engine.util.Utils;

public class ActionExecutor {
	protected ActionExecutor(DataAccessor data) {
		this.data = data;
		this.utils = data.GetUtils();
		params = data.GetParam();
	}
	
	public void Execute(Action action) {
		if (!ExecuteActionInternal(action)) {
			if (action.type != ActionType.RESPOND_TO_TREATY) {
				System.err.println("Action failed: " + action.toString());
			}
		}
	}
	
	private boolean ExecuteActionInternal(Action action) {
		switch (action.type) {
		case APPEASE_SUBJECT_WITH_FOOD:
			return AppeaseSubjectWithFood((City)action.object, (Long)action.quantity);
		case CONSTRUCT_IMPROVEMENT:
			return ConstructImprovement((City)action.object, ImprovementType.values()[action.quantity.intValue()]);
		case ALLOCATE_PROFESSION:
			return AllocateProfession((City)action.object, (Integer)action.quantity, (Integer)action.quantity2, (Integer)action.quantity3);
		case CHANGE_ADVANCED_UNIT_PERCENTAGE:
			return ChangeAdvancedUnitPercentage((City)action.object, (Integer)action.quantity);
		case CHANGE_FOOD_TAX:
			return ChangeFoodTax((State)action.object, (Integer)action.quantity);
		case ALLOCATE_BUDGET:
			return AllocateBudget((State)action.object, (Integer)action.quantity, (Integer)action.quantity2, (Integer)action.quantity3, (Integer)action.quantity4);
		case RESEARCH_TECHNOLOGY_TYPE:
			return ResearchTechnologyType((State)action.object, TechnologyType.values()[action.quantity.intValue()]);
		case RECRUIT_ARMY:
			return RecruitArmy((City)action.object2, (Army)action.object, (Unit)action.object3,
					SoldierType.values()[action.quantity.intValue()]);
		case REBASE_ARMY:
			return RebaseArmy((Army)action.object, (City)action.object2);
		case TARGET_ARMY:
			return TargetArmy((Army)action.object, (Army)action.object2, action.flag);
		case TARGET_CITY:
			return TargetCity((Army)action.object, (City)action.object2);
		case RESET_TARGET:
			return ResetTarget((Army)action.object);
		case HIRE_OFFICER:
			return HireOfficer((State)action.object);
		case ASSIGN_MINISTER:
			return AssignMinister((Person)action.object, Role.kMinisters[action.quantity.intValue()]);
		case ASSIGN_GENERAL:
			return AssignGeneral((Person)action.object, (Army)action.object2);
		case ASSIGN_GOVERNOR:
			return AssignGovernor((Person)action.object, (City)action.object2);
		case ADOPT_POLICY:
			return AdoptPolicy((State)action.object, (IdKeyedData)action.object2,(IdKeyedData)action.object3, action.quantity2,
					Policy.values()[action.quantity.intValue()]);
		case RESPOND_TO_TREATY:
			return RespondToTreaty((Treaty)action.object, action.quantity.intValue() > 0);
		};
		return false;
	}

	private boolean AppeaseSubjectWithFood(City city, Long food) {
		Resource<Long> owner_resource = city.GetOwner().GetResource();
		long current_food = owner_resource.food;
		if (food > current_food) return false;
		city.Get().remaining_food += food;
		owner_resource.food = current_food - food;
		return true;
	}
	
	private boolean ResearchTechnologyType(State state, TechnologyType type) {
		state.GetTechnology().SetResearchingTechnologyType(type);
		return true;
	}
	
	private boolean ConstructImprovement(City city, ImprovementType type) {
		State state = city.GetOwner();
		if (!state.Playable() || !utils.state_util.IsImprovementAvailable(state, type) ||
				!utils.state_util.IsImprovementAffordable(state, city, type)) return false;
		Resource.SubtractResource(state.GetResource(), data.GetParam().GetImprovementCost(type));
		int construction_days = params.improvement_construction_days[type.ordinal()];
		if (city.GetOwner().GetPolicy().HasIdeology(Ideology.MOHISM)) {
			construction_days = construction_days * 8 / 10;
		}
		city.GetImprovements().SetCurrentConstruction(type, construction_days);
		return true;
	}
	
	private boolean AllocateProfession(City city, int... profession_pcts) {
		if (!utils.city_util.IsProfessionAllocationAllowed(city, profession_pcts)) return false;
		city.GetPopulation().SetProfessionTargetPct(profession_pcts);
		return true;
	}
	
	private boolean ChangeAdvancedUnitPercentage(City city, int pct) {
		if (pct < 0 || pct > 100) return false;
		city.GetMilitary().SetAdvancedUnitPercent(pct);
		return true;
	}
	
	private boolean ChangeFoodTax(State state, int pct) {
		if (pct < 0 || pct > 100) return false;
		if (state.Get().stability < 1) return false;
		if (!state.GetTechnology().HasEffect(Effect.UNBLOCK_TAX_RATE_CHANGE)) return false;
		int old_pct = state.GetEconomic().GetFoodTaxPercentage();
		state.GetEconomic().SetFoodTaxPercentage(pct);
		if (old_pct < pct) {
			utils.state_util.DecreaseStability(state);
		}
		return true;
	}
	
	private boolean AllocateBudget(State state, int tech_budget, int food_budget, int horse_budget, int iron_budget) {
		if (tech_budget < 0) return false;
		int sum = tech_budget + Math.max(0, food_budget) + Math.max(0, horse_budget) + Math.max(0, iron_budget);
		if (sum > 100) return false;
		StateEconomic economic = state.GetEconomic();
		economic.SetTechBudgetPercentage(tech_budget);
		economic.SetResourcePurchaseBudgetPercentage(ResourceType.FOOD, food_budget);
		economic.SetResourcePurchaseBudgetPercentage(ResourceType.HORSE, horse_budget);
		economic.SetResourcePurchaseBudgetPercentage(ResourceType.IRON, iron_budget);
		return true;
	}
	
	private boolean RecruitArmy(City city, Army army, Unit unit, SoldierType soldier_type) {
		State state = city.GetOwner();
		if (!utils.state_util.IsUnitAvailable(state, unit)) return false;
		if (utils.state_util.GetTypedSoldiers(state, soldier_type) + utils.state_util.GetTypedSoldiersUnderConstruction(state, soldier_type)
				+ params.base_recruitment > utils.state_util.GetAllowedTypedSoldiers(state, soldier_type)) return false;
		if (!utils.state_util.IsRecruitmentAffordable(state, unit) || utils.city_util.GetAvailableSoldierCandidate(city) < params.base_recruitment
				|| !utils.army_util.IsArmyReinforceable(army)) return false;
		Resource.SubtractResource(state.GetResource(), utils.army_util.GetRecruitmentCost(unit));
		if (soldier_type != SoldierType.CONSCRIPTION) {
			city.Get().population -= params.base_recruitment;
		}
		city.GetMilitary().SetCurrentRecruitment(army, unit, soldier_type, params.base_recruitment_days);
		return true;
	}
	
	private boolean RebaseArmy(Army army, City city) {
		if (army.GetState() != city.GetOwner()) return false;
		army.SetBaseCity(city);
		return true;
	}
	
	private boolean TargetArmy(Army army, Army target, boolean pursue) {
		army.SetTarget(target, pursue);
		return true;
	}
	
	private boolean TargetCity(Army army, City target) {
		army.SetTarget(target);
		return true;
	}
	
	private boolean ResetTarget(Army army) {
		army.ResetTarget();
		return true;
	}
	
	private boolean HireOfficer(State state) {
		long current_gold = state.GetResource().gold;
		int hiring_cost = utils.state_util.GetHiringCost(state);
		if (hiring_cost > current_gold) return false;
		state.GetResource().gold = current_gold - hiring_cost;
		for (Person person :  data.GetAvailablePeople()) {
			if (person.GetOriginalState() == state) {
				person.SetOwner(state);
				state.AddPerson(person);
				return true;
			}
		}
		if (!utils.person_util.CanHirePeople(state)) return false;
		Person person = utils.person_util.CreateNewPerson(state);
		state.AddPerson(person);
		return true;
	}
	
	private boolean AssignMinister(Person person, RoleType role) {
		State state = person.GetOwner();
		if (state == null) return false;
		Person old_minister = state.GetOfficer(role);
		if (old_minister != null) {
			old_minister.ResetRole();
		}
		person.ResetRole();
		state.SetOfficer(role, person);
		return true;
	}
	
	private boolean AssignGeneral(Person person, Army army) {
		if (person.GetOwner() != army.GetState()) return false;
		Person old_general = army.GetGeneral();
		if (old_general != null) {
			old_general.ResetAssignment();
		}
		person.ResetAssignment();
		army.SetGeneral(person);
		return true;
	}
	
	private boolean AssignGovernor(Person person, City city) {
		if (person.GetOwner() != city.GetOwner()) return false;
		if (!utils.person_util.CityAllowsGovernor(city)) return false;
		Person old_governor = city.GetGovernor();
		if (old_governor != null) {
			old_governor.ResetAssignment();
		}
		person.ResetAssignment();
		city.SetGovernor(person);
		return true;
	}
	
	private boolean AdoptPolicy(State state, IdKeyedData object, IdKeyedData object2, Number number, Policy policy) {
		if (!utils.state_util.IsPolicyAvailable(state, policy)) return false;
		if (!utils.diplomacy_util.ValidateDiplomacyPolicy(policy, state, object)) return false;
		if (policy == Policy.ESTABLISH_COUNTY && !utils.state_util.IsNewCountyAllowed(state)) return false;
		if (policy == Policy.ADOPT_IDEOLOGY) {
			if (!utils.state_util.IsIdeologyAvailable(state, Ideology.values()[number.intValue()])) return false;
		}
		if (policy == Policy.MIGRATE) {
			if (!utils.pop_util.IsCityMigrationAffordable((City)object, (City)object2, number.longValue())) return false;
		}
		state.GetPolicy().SetPolicy(policy, object, object2, number != null ? number.longValue() : -1L);
		return true;
	}
	
	private boolean RespondToTreaty(Treaty treaty, boolean accept) {
		data.CloseTreaty(treaty);
		if (!utils.diplomacy_util.IsTreatyEligible(treaty)) return false;
		utils.diplomacy_util.HandleTreatyResponse(treaty, accept);
		return true;
	}
	
	private DataAccessor data;
	private Utils utils;
	private Parameters params;
}
