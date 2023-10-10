package ac.engine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import ac.data.ArmyData.SoldierType;
import ac.data.CityData.Profession;
import ac.data.base.Resource;
import ac.data.base.Resource.ResourceType;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.ConstStateData;
import ac.data.constant.Ideologies;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Ideologies.IdeologyType;
import ac.data.constant.Role;
import ac.data.constant.Technology;
import ac.data.constant.Texts;
import ac.data.constant.Technology.TechnologyType;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Policies.Policy;
import ac.engine.ai.GameInterface;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.CityImprovements;
import ac.engine.data.CityMilitary.RecruitmentInfo;
import ac.engine.data.City.CityType;
import ac.engine.data.DataAccessor;
import ac.engine.data.IdKeyedData;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.util.MathUtil;

public class StateUtil extends BaseUtil {

	protected StateUtil(DataAccessor data, Utils utils) {
		super(data, utils);
	}
	
	public long GetTotalPopulation(State state) {
		long population = 0L;
		for (City city : state.GetOwnedCities()) {
			population += city.GetTotalPopulation();
		}
//		for (Army army : state.GetMilitary().GetArmies()) {
//			population += army.GetTypedSoldier(SoldierType.FUBING);
//			population += army.GetTypedSoldier(SoldierType.RECRUITMENT);
//		}
		return population;
	}
	
	public Resource<Long> GetMonthlyProduce(State state) {
		// Industrial
		Resource<Long> total_yields = GetIndustryProduction(state);
		// Commerce
		total_yields.gold += GetCommerceIncome(state);
		return total_yields;
	}
	
	public Resource<Long> GetIndustryProduction(State state) {
		Resource<Long> total_yields = new Resource<Long>(0L);
		for (City city : state.GetOwnedCities()) {
			Resource<Long> city_yields = utils.city_util.GetIndustryProduction(city);
			Resource.AddResource(total_yields, city_yields, utils.prod_util.GetTaxEfficiency(city));
		}
		return total_yields;
	}
	
	public long GetCommerceIncome(State state) {
		long income = 0;
		for (City city : state.GetOwnedCities()) {
			if (utils.city_util.IsUnderSiege(city) != null) continue;
			income += utils.city_util.GetCommerceIncome(city) * utils.prod_util.GetTaxEfficiency(city);
		}
		return income;
	}
	
	public long GetEstimatedAnnualFoodIncome(State state) {
		long total_food = 0;
		for (City city : state.GetOwnedCities()) {
			long food = utils.city_util.GetFoodYield(city);
			double tax_efficiency = utils.prod_util.GetTaxEfficiency(city);
			total_food += food * city.GetOwner().GetEconomic().GetFoodTax() * tax_efficiency;
		}
		return total_food;
	}
	
	public long GetSalaryExpense(State state) {
		return state.GetPersons().size() * param.minister_salary;
	}
	
	public double GetSoldierWage(State state) {
		return param.base_wage;
	}
	
	public long GetSoldierWageExpense(State state) {
		double wage = GetTypedSoldiers(state, SoldierType.RECRUITMENT) * GetSoldierWage(state);
		if (state.GetPolicy().HasIdeology(Ideology.DUJUN)) {
			wage *= 0.5;
		}
		return (long)wage;
	}
	
	public long GetExpense(State state) {
		long expense = 0;
		for (City city : state.GetOwnedCities()) {
			expense += utils.city_util.GetBuildingMaintenanceCost(city);
			expense += utils.city_util.GetStabilityCost(city);
			expense += utils.army_util.GetMaintenanceCost(city.GetMilitary().GetGarrison());
		}
		expense += GetSalaryExpense(state);
		expense += GetSoldierWageExpense(state);
		return expense;
	}
	
	public long GetFoodExpense(State state) {
		return GetFoodPayment(state) + GetFoodConsumption(state);
	}
	
	public long GetFoodPayment(State state) {
		long cost = GetTypedSoldiers(state, SoldierType.RECRUITMENT) / 12;
		if (state.GetPolicy().HasIdeology(Ideology.DUJUN)) {
			cost /= 2;
		}
		return cost;
	}
	
	public long GetFoodConsumption(State state) {
		long food = 0L;
		for (Army army : state.GetMilitary().GetArmies()) {
			food += utils.army_util.GetDailyFoodConsumption(army);
		}
		return food * 30;
	}
	
	public long GetNetIncome(State state, Resource<Long> income_resource, Resource<Long> export_resource, long expense) {
		long income = GetExportIncome(state, export_resource) + income_resource.gold;
		return income - expense;
	}
	
//	public Resource<Long> SubstractMilitaryBudgetIncrease(State state, Resource<Long> income_resource) {
//		Resource<Long> military_budget_increase = new Resource<Long>(0L);	
//		Resource.Product(military_budget_increase, income_resource, state.GetEconomic().GetMilitaryBudgetRatio());
//		Resource.SubtractResource(income_resource, military_budget_increase);
//		return military_budget_increase;
//	}
	
	public Resource<Long> GetExportResources(State state, Resource<Long> income_resource) {
		Resource<Long> export = new Resource<Long>(0L);
		Resource<Long> possessed = new Resource<Long>(0L);
		Resource.AddResource(possessed, state.GetResource());
		Resource.AddResource(possessed, income_resource);
		export.Assign((x, y) -> { return Math.min(x, y); }, possessed, state.GetEconomic()::GetResourceExportQuantity);
			
//		for (ResourceType type : ResourceType.values()) {
//			if (type == ResourceType.GOLD) continue;
//			long export_quantity = state.GetEconomic().GetResourceExportQuantity(type);
//			export.Set(type, Math.min(possessed.Get(type), export_quantity));
//		}
		return export;
	}
	
	public long GetExportIncome(State state, Resource<Long> export_resource) {
		long income = (long) Math.ceil(export_resource.Aggregate((n, price) -> n * price, data.GetMarket()::GetPrice));
//		for (ResourceType type : ResourceType.values()) {
//			if (type == ResourceType.GOLD) continue;
//			income += export_resource.Get(type) * data.GetMarket().GetPrice(type);
//		}
		return income;
	}
	
	public long GetTechBudget(State state, long income) {
		return income > 0 ? (long) (income * state.GetEconomic().GetTechBudgetRatio()) : 0;
	}
	
	public long GetResourceImportBudget(State state, long income, ResourceType type) {
		return income > 0 ? (long) (income * state.GetEconomic().GetResourceImportBudgetRatio(type)) : 0;
	}

	public double GetTechMultiplier(State state) {
		TechnologyType type = state.GetTechnology().GetResearchingTechnologyType();
		Technology current = state.GetTechnology().GetCurrentTechnology(type);
		Technology tech = state.GetTechnology().GetResearchingTechnology();
		int current_index = current != null ? current.index : -1;
		int i = 1;
		for (State neighbor : state.GetNeighborStates()) {
			Technology neighbor_current = neighbor.GetTechnology().GetCurrentTechnology(type);
			if (neighbor_current != null && neighbor_current.index > current_index) {
				i++;
			}
		}
		double tech_multiplier = param.base_tech_multiplier * MathUtil.SumOfHarmonicSeries(i);
		tech_multiplier *= MathUtil.SumOfHarmonicSeries(state.GetOwnedCities().size()) / state.GetOwnedCities().size();
		if (state.GetPolicy().HasIdeology(Ideology.CONFUCIAN)) {
			tech_multiplier *= 0.8;
		} else if (state.GetPolicy().HasIdeology(Ideology.MOHISM)) {
			tech_multiplier *= 1.2;
		}
		if (tech != null && tech.year > data.GetDate().GetYear()) {
			tech_multiplier *= Math.pow(2, (data.GetDate().GetYear() - tech.year) / param.tech_penality_half_life_year);
		}
		return tech_multiplier;
	}	
	
	public int GetTechBoost(long expense_for_tech, double multiplier) {
		return (int) (expense_for_tech * multiplier);
	}
	
	public boolean IsImprovementAvailable(State state, ImprovementType type) {
		if (type == ImprovementType.IRRIGATED_FARM || type == ImprovementType.WORKSHOP) return false;
		return state.GetTechnology().Has(data.GetConstData().improvement_techs.get(type));
	}
	
	public boolean IsImprovementAffordable(State state, City city, ImprovementType type) {
		return Resource.NotLessThan(state.GetResource(), param.GetImprovementCost(type))
				&& utils.pop_util.GetAvailableLabor(city) >= param.building_labor_cost[type.ordinal()];
	}
	
	public boolean IsUnitAvailable(State state, Unit unit) {
		return GetMostAdvancedUnits(state).get(unit.type).index >= unit.index;
	}
	
	public boolean IsRecruitmentAffordable(State state, Unit unit) {
		Resource<Long> cost = utils.army_util.GetRecruitmentCost(unit);
		Resource<Long> debt = Resource.Debt(state.GetResource(), cost);
		
		return debt.CheckAll(x -> x <= 0);
	}
	
	public boolean DecreaseStability(State state) {
		state.Get().stability = Math.max(0, state.Get().stability - 1);
		return state.Get().stability <= 0;
	}
	
	public boolean IsPolicyAvailable(State state, Policy policy) {
		if (policy == Policy.MIGRATE && !state.GetPolicy().HasIdeology(Ideology.LEGALISM)) return false;
		return state.GetTechnology().Has(data.GetConstData().policy_techs.get(policy));
	}
	
	public boolean IsPolicyNeccessary(State state, Policy policy) {
		if (policy == Policy.INCREASE_STABILITY) {
			return state.Get().stability < ConstStateData.kMaxStability;
		}
		if (policy == Policy.ESTABLISH_COUNTY) {
			return IsNewCountyAllowed(state);
		}
		return true;
	}
	
	public boolean IsIdeologyAvailable(State state, Ideology ideology) {
		return state.GetTechnology().Has(data.GetConstData().ideology_techs.get(ideology));
	}
	
	public ArrayList<Ideology> GetAvailableIdelogies(State state, IdeologyType type) {
		ArrayList<Ideology> list = new ArrayList<Ideology>();
		for (Ideology ideology : Ideologies.typed_ideologies.get(type)) {
			if (ideology != Ideology.NONE && IsIdeologyAvailable(state, ideology)) list.add(ideology);
		}
		return list;
	}
	
	public int GetPolicyPoint(State state, AbilityType type) {
		int increase = state.GetKing().GetAbility(type);
		Person minister = state.GetOfficer(Role.kMinisters[type.ordinal()]);
		if (minister != null) {
			increase += minister.GetAbility(type);
		}
		return increase;
	}
	
	public int GetHiringCost(State state) {
		return (state.GetPersons().size() + 1) * param.hiring_cost;
	}
	
	public int GetAllowedCounties(State state) {
		int num = param.base_allowed_county_num;
		for (Technology tech : data.GetConstData().county_boost_techs) {
			if (state.GetTechnology().Has(tech)) num += tech.county_bonus;
		}
		return num;
	}
	
	public boolean IsNewCountyAllowed(State state) {
		int allowed_county = GetAllowedCounties(state);
		int potential_county = 0;
		for (City city : state.GetOwnedCities()) {
			CityType type = city.GetType();
			if (type == CityType.COUNTY && --allowed_county <= 0) return false;
			if (type == CityType.NONE || type == CityType.JIMI_COUNTY) potential_county++;
		}
		return potential_county > 0;
	}
	
	public HashMap<UnitType, Unit> GetMostAdvancedUnits(State state) {
		HashMap<UnitType, Unit> map = new HashMap<UnitType, Unit>();
		for (UnitType type : UnitType.values()) {
			map.put(type, state.GetTechnology().GetMostAdvancedUnit(type));
		}
		return map;
	}
	
	public int GetMaxArmyNumber(State state) {
		int city_number = state.GetOwnedCities().size();
		int max_army_number = 0;
		for (int i = 0; i < param.army_num_thresholds.length; ++i) {
			if (city_number >= param.army_num_thresholds[i]) max_army_number++;
			else break;
		}
		return max_army_number;
	}
	
	public long GetTotalSoldiers(State state) {
		long total = 0L;
		for (Army army : state.GetMilitary().GetArmies()) {
			total += army.GetTotalSoldier();
		}
		return total;
	}
	
	public long GetTypedSoldiers(State state, SoldierType type) {
		long soldiers = 0L;
		for (Army army : state.GetMilitary().GetArmies()) {
			soldiers += army.GetTypedSoldier(type);
		}
//		for (City city : state.GetOwnedCities()) {
//			soldiers += city.GetMilitary().GetRecruitedTypedSoldiers(type);
//		}
		return soldiers;
	}
	
	public long GetAllowedTypedSoldiers(State state, SoldierType type) {
		double ratio = 0;
		if (type == SoldierType.CONSCRIPTION) {
			ratio = 1.0;
		} else {
			for (Technology tech : data.GetConstData().soldier_type_techs.get(type)) {
				if (state.GetTechnology().Has(tech)) {
					ratio += (type == SoldierType.FUBING) ? tech.fubing : tech.recruitment;
				}
			}
		}
		
		long total_population = GetTotalPopulation(state);
		return (long) (total_population * ratio);
	}
	
	public long GetTypedSoldiersUnderConstruction(State state, SoldierType type) {
		long soldiers = 0L;
		for (City city : state.GetOwnedCities()) {
			RecruitmentInfo info = city.GetMilitary().GetRecruitmentInfo();
			if (info != null && info.type == type) soldiers += param.base_recruitment;
		}
		return soldiers;
	}
	
	public long GetFubingAffordability(State state) {
		long available_slot = 0L;
		for (City city : state.GetOwnedCities()) {
			CityImprovements impr = city.GetImprovements();
			int farm = impr.GetCount(ImprovementType.FARM);
			int aqeducted_farm = impr.GetCount(ImprovementType.AQEDUCTED_FARM);
			int irrigated_farm = impr.GetCount(ImprovementType.IRRIGATED_FARM);
			int total_farm = farm + aqeducted_farm + irrigated_farm;
			long total_peasant = utils.city_util.GetProfessionalPopulation(city, Profession.PEASANT);
			available_slot += Math.max(0,  total_farm * 3 * param.population_per_farm - total_peasant);
		}
		
		return (long) (available_slot * utils.prod_util.GetAgricultureTechMultiplier(state));
	}
	
	public long GetMonthlyReinforcement(State state) {
		return param.base_army_reinforcement;
	}
	
	public int GetMaxRelationship(State state, State target) {
		if (state.GetDiplomacy().GetSuzerainty() == target ||
				target.GetDiplomacy().GetSuzerainty() == state ||
				utils.diplomacy_util.AreAlliance(state, target)) {
			return ConstStateData.kMaxAttitude;
		}
		if (utils.diplomacy_util.AreAlly(state, target)) return param.max_attitude_with_ally;
		return param.max_attitude_without_ally;
	}
	
	public Collection<City> GetNeighborCities(State state) {
		HashSet<City> cities = new HashSet<City>();
		for (City city : state.GetOwnedCities()) {
			for (City neighbor : city.GetNeighbors()) {
				if (neighbor.GetOwner() != state) cities.add(neighbor);
			}
		}
		return cities;
	}
	
	public void CheckArmyNumber(State state) {
		int max_army_number = utils.state_util.GetMaxArmyNumber(state);
		while (state.GetMilitary().GetArmies().size() < max_army_number) {
			state.GetMilitary().AddArmy();
		}
	}
	
	public boolean HasEffect(State state, Technology.Effect effect) {
		return state.GetTechnology().Has(data.GetConstData().tech_effects.get(effect));
	}
	
	public void StateFall(State state, GameInterface ai) {
		for (Army army : state.GetMilitary().GetArmies()) {
			utils.army_util.AbandonArmy(army);
		}
		for (State other : data.GetAllPlayableStates()) {
			for (AbilityType type : AbilityType.values()) {
				IdKeyedData obj = other.GetPolicy().GetPolicyObject(type);
				IdKeyedData obj2 = other.GetPolicy().GetPolicyObject2(type);
				Policy policy = other.GetPolicy().GetPolicy(type);
				if (policy != null && (obj == state || obj2 == state)) {
					other.GetPolicy().ResetPolicy(type);
					ai.OnPolicyInvalided(other, policy, obj, obj2);
				}
			}
		}
		for (Person person : state.GetPersons()) {
			person.ResetOwner();
		}
		state.Get().playable = false;
		data.AddMessage(state.GetName() + Texts.fall);
	}
}
