package ac.engine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ac.data.ArmyData.SoldierType;
import ac.data.CityData.Profession;
import ac.data.base.Pair;
import ac.data.base.Resource;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.ConstCityData;
import ac.data.constant.ConstStateData;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Improvement;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Improvement.SpecialImprovementType;
import ac.data.constant.Policies.Policy;
import ac.data.constant.Technology.Effect;
import ac.data.constant.Texts;
import ac.engine.ai.GameInterface;
import ac.engine.data.Army;
import ac.engine.data.Army.Status;
import ac.engine.data.City;
import ac.engine.data.City.CityType;
import ac.engine.data.CityImprovements;
import ac.engine.data.CityPopulation;
import ac.engine.data.DataAccessor;
import ac.engine.data.IdKeyedData;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.util.ContainerUtil;
import ac.util.RandomUtil;

public class CityUtil extends BaseUtil {

	public CityUtil(DataAccessor data, Utils utils) {
		super(data, utils);
	}
	
	public long GetProfessionalPopulation(City city, Profession profession) {
		return (long) ((profession == Profession.WORKER || profession == Profession.MERCHANT ?
					(utils.pop_util.GetAvailableLabor(city) ) : city.Get().population)
				* city.GetPopulation().GetProfessionRatio(profession));
	}
	
	public long GetFoodYield(City city) {
		int rain_level = city.GetNaturalInfo().GetRainLevelOrDefault();
		int heat_level = city.GetNaturalInfo().GetTemperatureLevelOrDefault();
		long total_peasant = GetProfessionalPopulation(city, Profession.PEASANT);
		return utils.prod_util.GetFoodYield(city, total_peasant, rain_level, heat_level);
	}
	
	public Resource<Long> GetIndustryProduction(City city) {
		IndustryPopulationDistribution dist = utils.city_util.GetIndustryPopulationDistribution(city);
		Resource<Long> city_yields = new Resource<Long>(0L);
		if (dist.worker_per_improvment > 0) {
			for (ImprovementType type : Improvement.kIndustryImprovements) {
				int count = city.GetImprovements().GetCount(type);
				if (count <= 0) continue;
				Resource.AddResource(city_yields, utils.prod_util.GetProduction(city.GetOwner(), type, count, dist.worker_per_improvment));
			}
		}
		if (dist.workshop_worker > 0) {
			Resource.AddResource(city_yields, utils.prod_util.GetProduction(city.GetOwner(), ImprovementType.WORKSHOP, dist.workshop_worker));
		}
		return city_yields;
	}
	
	public static class IndustryPopulationDistribution {
		public long workshop_worker;
		public double worker_per_improvment = -1;
	}
	
	public IndustryPopulationDistribution GetIndustryPopulationDistribution(City city) {
		long worker = GetProfessionalPopulation(city, Profession.WORKER);
		int total_improvements = 0;
		for (int i = 0; i < Improvement.kIndustryImprovements.length; ++i) {
			total_improvements += city.GetImprovements().GetCount(Improvement.kIndustryImprovements[i]);;
		}
		long covered_worker = Math.min(worker, total_improvements * param.population_per_industry_improvement);
		IndustryPopulationDistribution dist = new IndustryPopulationDistribution();
		dist.workshop_worker = worker - covered_worker;
		if (total_improvements > 0) dist.worker_per_improvment = (double)covered_worker / total_improvements;
		return dist;
	}
	
	public long GetCommerceIncome(City city) {
		long merchang_num = GetProfessionalPopulation(city, Profession.MERCHANT);
		double unit_income = utils.prod_util.GetIncomePerMerchant(city, city.GetPopulation().GetProfessionRatio(Profession.MERCHANT));
		return (long) (unit_income * merchang_num);
	}

	public void HandleRain() {
		int base_rain_level = RandomUtil.GetRandomIndexFromProbabilities(param.rain_level_prob, data.GetRandom()) - 1;
		for (City city : data.GetAllCities()) {
			city.GetNaturalInfo().SetRainAdjustment(base_rain_level);
		}
		int num_adjustments = RandomUtil.SampleFromUniformDistribution(0, 3, data.GetRandom());
		for (int i = 0; i < num_adjustments; ++i) {
			int city_index = RandomUtil.SampleFromUniformDistribution(0, data.GetAllCities().size() - 1, data.GetRandom());
			int adjustment = RandomUtil.WhetherToHappend(0.5, data.GetRandom()) ? 1 : -1;
			PropagateCities(data.GetAllCities().get(city_index),
					city -> { city.GetNaturalInfo().SetRainAdjustment(adjustment); },
					neighbor -> { return RandomUtil.WhetherToHappend(param.adjustment_propagate_prob / neighbor.second, data.GetRandom()); });
		}
 	}
	
	public void HandleTemperature() {
		int num_adjustments = RandomUtil.SampleFromUniformDistribution(0, 4, data.GetRandom());
		for (int i = 0; i < num_adjustments; ++i) {
			int city_index = RandomUtil.SampleFromUniformDistribution(0, data.GetAllCities().size() - 1, data.GetRandom());
			int adjustment = RandomUtil.WhetherToHappend(0.5, data.GetRandom()) ? 1 : -1;
			PropagateCities(data.GetAllCities().get(city_index),
					city -> { city.GetNaturalInfo().SetTemperatureAdjustment(adjustment); },
					neighbor -> { return RandomUtil.WhetherToHappend(param.adjustment_propagate_prob / neighbor.second, data.GetRandom()); });
		}
 	}
	
	public void HandleDisaster() {
		ArrayList<String> flood_city_names = new ArrayList<String>();
		ArrayList<String> locust_city_names = new ArrayList<String>();
		for (City city : data.GetAllPlayableCities()) {
			int rain_adjustment = city.GetNaturalInfo().GetRainAdjustment();			
			if (rain_adjustment > 0) {
				double flood_prob = city.GetNaturalInfo().GetFloodRisk() * param.disaster_risk_multiplier * rain_adjustment +
						param.disaster_base_prob * rain_adjustment;
				if (RandomUtil.WhetherToHappend(flood_prob, data.GetRandom())) {
					city.GetNaturalInfo().SetFlood(RandomUtil.SampleFromUniformDistribution(
							param.flood_min_severity, param.flood_max_severity, data.GetRandom()));
					flood_city_names.add(city.GetName());
				}
			} else if (rain_adjustment < 0) {
				double locust_prob = city.GetNaturalInfo().GetFloodRisk() * param.disaster_risk_multiplier * -rain_adjustment +
						param.disaster_base_prob * -rain_adjustment;
				if (RandomUtil.WhetherToHappend(locust_prob, data.GetRandom())) {
					city.GetNaturalInfo().SetLocust(RandomUtil.SampleFromUniformDistribution(
							param.locust_min_severity, param.locust_max_severity, data.GetRandom())
							* (city.GetOwner().GetTechnology().HasEffect(Effect.REDUCE_LOCUST_DAMAGE) ? 0.75 : 1.0));
					locust_city_names.add(city.Get().name);
				}
			}
		}
		if (!flood_city_names.isEmpty()) {
			data.AddMessage(String.join(",", flood_city_names) + Texts.happen + Texts.flood);
		}
		if (!locust_city_names.isEmpty()) {
			data.AddMessage(String.join(",", locust_city_names) + Texts.happen + Texts.locust);
		}
	}
	
	public boolean IsImprovementAvailable(City city, ImprovementType type) {
		CityImprovements city_impr = city.GetImprovements();
		if (type == ImprovementType.FARM) {
			return city_impr.GetCount(ImprovementType.FARM) + city_impr.GetCount(ImprovementType.AQEDUCTED_FARM) +
					city_impr.GetCount(ImprovementType.IRRIGATED_FARM) < GetMaxImprovement(city, ImprovementType.FARM);
		} else if (type == ImprovementType.AQEDUCTED_FARM) {
			return city_impr.GetCount(ImprovementType.FARM) > 0;
		} else if (type == ImprovementType.SPECIAL && city_impr.GetSpecialImprovement() == SpecialImprovementType.NONE) {
			return false;
		}
		return city_impr.GetCount(type) < city_impr.GetMaxCount(type);
	}
	
	public int GetBuildingMaintenanceCost(City city) {
		int cost = 0;
		CityImprovements city_impr = city.GetImprovements();
		for (int i = 0; i < Improvement.kAuxiliaryImprovements.length; ++i) {
			ImprovementType type = Improvement.kAuxiliaryImprovements[i];
			cost += city_impr.GetCount(type) * param.auxiliary_improvement_maintenance_cost[i];
		}
		return cost;
	}
	
	public long GetStabilityCost(City city) {
		double multiplier = param.stability_cost_multiplier;
		if (city.GetOwner().GetPolicy().HasIdeology(Ideology.LEGALISM)) multiplier *= 2;
		return (long) (city.Get().population * (100 - city.GetPopulation().GetHappiness()) / 100 * multiplier);
	}
	
	public int GetRiotPoint(City city) {
		return (int)ComputeRiotPointInternal(city, 0);
	}
	
	public boolean HasRiotRisk(City city) {
		return ComputeRiotPointInternal(city, 1) > 0;
	}
	
	private double ComputeRiotPointInternal(City city, int stability_buffer) {
		boolean state_has_agriculural_war = city.GetOwner().GetPolicy().HasIdeology(Ideology.AGRICULURAL_WAR);
		boolean state_has_dujun = city.GetOwner().GetPolicy().HasIdeology(Ideology.DUJUN);
		boolean state_has_military_bureau = city.GetOwner().GetPolicy().HasIdeology(Ideology.MILITARY_BUREAU);
		double riot_point = state_has_military_bureau ? param.riot_points_with_military_bureau : param.riot_base_points;
		
		double stability_penalty = param.riot_stability_penalty * (ConstStateData.kMaxStability - city.GetOwner().Get().stability - stability_buffer);
		double prestige_calibration = Math.abs(city.GetOwner().Get().prestige) > param.riot_prestige_threshold ?
				-param.riot_prestige_multiplier * city.GetOwner().Get().prestige : 0;
		if (state_has_agriculural_war || state_has_dujun) prestige_calibration *= 2;
		double foreigner_penalty = -1.0;
		CityPopulation city_pop = city.GetPopulation();
		int city_type_index = city.GetType().ordinal();
		for (int i = 1; i < city_pop.NumRaces(); ++i) {
			State foreigner = city_pop.GetRace(i);
			foreigner_penalty += (foreigner.GetDescription().IsNonHuaxiaRace() ? param.riot_non_huaxia_foreign_multiplier[city_type_index] : 1.0)
					* city_pop.GetPopRatio(i);
		}
		int happiness = city.GetPopulation().GetHappiness();
		double happiness_calibration = (double)(happiness > param.riot_happiness_max_threshold ? (param.riot_happiness_max_threshold - happiness) :
			(happiness < param.riot_happiness_min_threshold ? (happiness - param.riot_happiness_min_threshold) : 0)) / 100.0;
		riot_point = (riot_point + stability_penalty + prestige_calibration + foreigner_penalty + happiness_calibration)
				* param.riot_point_multiplier_by_city_type[city_type_index];
		if (state_has_agriculural_war) {
			riot_point *= 2;
		}
		return riot_point;
	}
	
	public boolean CheckRiot(City city) {
		return city.Get().riot >= ConstCityData.kMaxRiotPoints;
	}
	
	public State ChangeOwner(City city, State new_owner) {
		double city_weight = GetCityWeight(city);
		city.SetType(null);
		State old_owner = city.GetOwner();
		if (old_owner != null) {
			old_owner.RemoveCity(city);
			if (old_owner.GetCapital() == city) {
				Collection<City> remaining_cities = old_owner.GetOwnedCities();
				if (!remaining_cities.isEmpty()) {
					City capital = ContainerUtil.FindTop(remaining_cities, new Comparator<City>() {
						@Override
						public int compare(City o1, City o2) {
							return (int) (GetValue(o2) - GetValue(o1));
						}
						public double GetValue(City city) {
							long value = city.GetTotalPopulation();
							for (City neighbor : city.GetNeighbors()) {
								final double discount = 0.8;
								if (neighbor.GetOwner() != null && neighbor.GetOwner().Playable()
										&& neighbor.GetOwner() != city.GetOwner()) {
									value *= discount;
								}
							}
							return value;
						}
					});
					ChangeCapital(capital);
				}
			}
			old_owner.UpdateNeighbors();
			if (old_owner.Playable()) {
				for (Army army : old_owner.GetMilitary().GetArmies()) {
					if (army.GetBaseCity() == city) {
						army.SetBaseCity(old_owner.GetCapital());
						army.SetRetreat(true);
					}
				}
				if (new_owner != null) {
					Resource<Long> looted = new Resource<Long>(0L);
					Resource.AddResource(looted, old_owner.GetResource(), city_weight * param.city_loot_multiplier);
					Resource.AddResource(new_owner.GetResource(), looted);
				}
			}
		}
		city.SetOwner(new_owner);
		city.Get().happiness = Math.min(param.max_conquered_city_happiness,
				param.conquered_city_happiness_sum - city.Get().happiness);
		if (new_owner != null) {
			new_owner.AddCity(city);
			new_owner.UpdateNeighbors();
			city.GetPopulation().RemoveRace(new_owner);
		}
		utils.army_util.ResetTargets(city.GetMilitary().GetGarrison());
		return old_owner;
	}
	
	public void ChangeCapital(City captial) {
		State state = captial.GetOwner();
		state.SetCapital(captial);
		utils.state_util.DecreaseStability(state);
	}
	
	public int MaxRouteLength(City city, ImprovementType type) {
		return (city.GetImprovements().GetCount(type) + 4) / 5 * 50;
	}
	
	public double GetMilitiaRatio(City city) {
		if (!city.GetOwner().Playable()) return 0;
		double solider_ratio = city.GetPopulation().GetProfessionRatio(Profession.SOLDIER) * city.Get().population
				/ city.GetMilitary().GetGarrison().GetTotalSoldier();
		return 1.0 - Math.min(1.0, solider_ratio);
	}
	
	public long GetMonthlyReinforcement(City city) {
		return param.base_reinforcement;
	}
	
	public boolean HandleCityLost(City city, boolean update_race, GameInterface ai) {
		State owner = city.GetOwner();
		Person governor = city.GetGovernor();
		if (governor != null) {
			governor.ResetAssignment();
		}
		for (Army army : owner.GetMilitary().GetArmies()) {
			long conscription = army.GetCityConscription(city);
			if (conscription <= 0L) continue;
			double conscription_ratio = (double)conscription / army.GetTypedSoldier(SoldierType.CONSCRIPTION);
			army.DecreaseTypedSoldier(SoldierType.CONSCRIPTION, conscription_ratio);
			army.RemoveCityConscription(city);
		}
		// Update races
		CityPopulation city_pop = city.GetPopulation();
		TreeMap<State, Double> race_pop = new TreeMap<State, Double>();
		for (int i = 0; i < city.GetPopulation().NumRaces(); ++i) {
			race_pop.put(city_pop.GetRace(i), city_pop.GetPopRatio(i) * (update_race ? (1.0 - param.conquered_city_min_race) : 1.0));
		}
		city_pop.ResetPopRatio(race_pop);
		
		for (AbilityType type : AbilityType.values()) {
			IdKeyedData obj = owner.GetPolicy().GetPolicyObject(type);
			IdKeyedData obj2 = owner.GetPolicy().GetPolicyObject2(type);
			Policy policy = owner.GetPolicy().GetPolicy(type);
			if (policy != null && (obj == city || obj2 == city)) {
				owner.GetPolicy().ResetPolicy(type);
				ai.OnPolicyInvalided(owner, policy, obj, obj2);
			}
		}
		city.SetType(CityType.NONE);
		city.Get().riot = 0;
		city.GetMilitary().GetGarrison().ChangeTrainingLevel(1.0);
		return city.GetOwner().GetCapital() == city;
	}
	
	public void HandleCityImprovementDamage(City city) {
		ImprovementType type = RandomUtil.Sample(ImprovementType.values(), data.GetRandom());
		if (type == ImprovementType.SPECIAL) return;
		city.GetImprovements().DecreaseCount(type);
	}
	
	public Army IsUnderSiege(City city) {
		HashSet<Army> city_attacker = new HashSet<Army>();
		HashSet<Army> blocked_armies = new HashSet<Army>();
		for (Army army : data.GetArmiesByPosition(city.GetPosition())) {
			if (army.GetTotalSoldier() <= 0 || army.GetStatus() == Status.RETREAT) continue;
			Army target = army.GetTarget();
			if (target == null) continue;
			blocked_armies.add(target);
			if (army.GetState() != city.GetOwner() && target == city.GetMilitary().GetGarrison()) {
				city_attacker.add(army);
			}
		}
		for (Army army : city_attacker) {
			if (!blocked_armies.contains(army)) return army;
		}
		return null;
	}
	
	public double GetCityWeight(City city) {
		State owner = city.GetOwner();
		double total = 0;
		for (City c : owner.GetOwnedCities()) {
			total += c.GetTotalPopulation() * param.weight_coefficient_per_city_type[c.GetType().ordinal()];
		}
		if (total <= 0) return 0;
		return city.GetTotalPopulation() * param.weight_coefficient_per_city_type[city.GetType().ordinal()] / total;
	}
	
	public int GetMaxImprovement(City city, ImprovementType type) {
		return city.GetImprovements().GetMaxCount(type);
	}
	
	public int GetMinPeasantPct(City city) {
		return param.base_min_peasant_pct;
	}
	
	public boolean IsProfessionAllocationAllowed(City city, int... pcts) {
		if (!city.GetOwner().GetTechnology().HasEffect(Effect.UNBLOCK_GARRISON) && pcts[Profession.SOLDIER.ordinal() - 1] > 0) return false;
		int allowed_worker_pct = city.GetOwner().GetTechnology().HasEffect(Effect.UNLIMIT_WORKER) ? 100 : param.base_max_worker_pct;
		if (pcts[Profession.WORKER.ordinal() - 1] > allowed_worker_pct) return false;
		if (100 - pcts[0] - pcts[1] - pcts[2] < GetMinPeasantPct(city)) return false;
		return true;
	}
	
	public long GetAvailableSoldierCandidate(City city) {
		long pop = city.GetTotalPopulation() * (100 - city.GetPopulation().GetProfessionTargetPct(Profession.SOLDIER)) / 100;
 		pop -= GetTotalArmySoldiers(city);
 		return pop;
	}
	
	public long GetTotalArmySoldiers(City city) {
		long soldiers = 0L;
 		for (Army army : city.GetOwner().GetMilitary().GetArmies()) {
 			soldiers += army.GetCityConscription(city);
 			soldiers += army.GetCitySupportingLabor(city);
 		}
 		return soldiers;
	}
	
	private void PropagateCities(City seed, Consumer<City> func, Predicate<Pair<City, Double>> cond) {
		HashSet<City> visited = new HashSet<City>();
		Queue<City> queue = new LinkedList<City>();
		queue.add(seed);
		while (!queue.isEmpty()) {
			City city = queue.poll();
			visited.add(city);
			func.accept(city);
			for (Pair<City, Double> neighbor : city.GetDescription().GetNeighbors()) {
				if (!visited.contains(neighbor.first) && cond.test(neighbor)) {
					queue.add(neighbor.first);
				}
			}
		}
	}
}
