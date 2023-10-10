package ac.engine.util;

import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Improvement.ImprovementType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;

public class PopulationUtil extends BaseUtil {
	public PopulationUtil(DataAccessor data, Utils utils) {
		super(data, utils);
		max_consumption_rate = (param.pop_increase_cap / param.pop_increase_slope + param.pop_growth_cutoff) / 12;
	}
	
	public int RemainingMonthBeforeHarvest(int month) {
		return (21 - month) % 12 + 1;
	}
	
	public long FoodConsumption(long population, long remaining_food, int remaining_month) {
		long minimum_consumption = (long) (population / 12 * param.pop_growth_cutoff);
		if (remaining_food < minimum_consumption) return remaining_food;
		long food_consumption = remaining_food  / remaining_month;
		if (food_consumption < minimum_consumption) return minimum_consumption;
		return Math.min(food_consumption, (long)(population * max_consumption_rate));
	}
	
	public double GrowthRate(State state, long population, long consumed_food, int temperature) {
		double consumption_ratio = (double)consumed_food / (population / 12);
		double consumption_ratio_delta = consumption_ratio - param.pop_growth_cutoff;
		if (consumption_ratio >= param.pop_growth_cutoff) {
			double temperature_discount = 1.0 + Math.max(0, (temperature - 3) * param.pop_temperature_discount);
			double bonus = state.GetPolicy().HasIdeology(Ideology.CONFUCIAN) ? 0.001 : 0;
			return Math.min(param.pop_increase_cap, consumption_ratio_delta * param.pop_increase_slope * temperature_discount) + bonus;
			
		}
		return consumption_ratio_delta * param.pop_decrease_slope;
	}
	
	public double DecreaseRate(int happiness) {
		if (happiness >= param.happiness_pop_decrease_threshold) return 0;
		return (param.happiness_pop_decrease_threshold - happiness) * param.pop_decrease_from_unhappiness / 100;
	}
	
	public long GetAvailableLabor(City city) {
		long pop = city.GetTotalPopulation();
		pop -= GetTotalLabor(city);
		pop -= GetTotalMilitaryService(city);
		return Math.max(0L, pop);
	}
	
	public long GetTotalLabor(City city) {
		long labor = 0L;
		for (Army army : city.GetOwner().GetMilitary().GetArmies()) {
			labor += army.GetCitySupportingLabor(city);
		}
		ImprovementType impr_type = city.GetImprovements().GetCurrentConstruction();
		if (impr_type != null) {
			labor += param.building_labor_cost[impr_type.ordinal()];
		}
		return labor;
	}
	
	public long GetTotalMilitaryService(City city) {
		long labor = 0L;
		for (Army army : city.GetOwner().GetMilitary().GetArmies()) {
			labor += army.GetCityConscription(city);
		}
		return labor;
	}
	
	public long GetTotalMilitaryInService(City city) {
		long labor = 0L;
		for (Army army : city.GetOwner().GetMilitary().GetArmies()) {
			if (utils.army_util.IsArmyActioning(army)) {
				labor += army.GetCityConscription(city);
			}
		}
		return labor;
	}
	
	public void CityReducePopulation(City city, long decrease) {
		city.Get().population = Math.max(param.city_min_population, city.Get().population - decrease);
	}
	
	public boolean IsCityMigrationAffordable(City from, City to, long quantity) {
		if (from == null || to == null || quantity <= 0) return false;
		if (from.GetTransportation(to) == Integer.MAX_VALUE) return false;
		if (from.GetOwner() != to.GetOwner()) return false;
		if (param.max_migration_quantity < quantity) return false;
		return true;
	}
	
	private double max_consumption_rate;
}
