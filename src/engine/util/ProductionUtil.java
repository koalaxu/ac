package ac.engine.util;

import ac.data.base.Resource;
import ac.data.base.Resource.ResourceType;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Role.RoleType;
import ac.data.constant.Technology;
import ac.data.constant.Technology.Effect;
import ac.data.constant.ConstStateData;
import ac.data.constant.Improvement;
import ac.engine.data.City;
import ac.engine.data.City.CityType;
import ac.engine.data.CityCommerce;
import ac.engine.data.CityImprovements;
import ac.engine.data.CityPopulation;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.engine.data.StateTechnology;
import ac.util.StringUtil;

public class ProductionUtil extends BaseUtil {

	public ProductionUtil(DataAccessor data, Utils utils) {
		super(data, utils);
	}

	// Industry
	public Resource<Long> GetProduction(State state, ImprovementType improvement_type, int number, double worker_per_improvment) {
		return GetProductionInternal(state, improvement_type, number * worker_per_improvment);
	}
	
	public Resource<Long> GetProduction(State state, ImprovementType improvement_type, long worker) {
		return GetProductionInternal(state, improvement_type, (double)worker);
	}
	
	// Agriculture
	public double GetNatureBase(ImprovementType improvement_type, int rain_level, int heat_level) {
		double irrigation = GetFarmYieldParam(improvement_type);
		double rain_multiplier = 0.8 + 0.1 * Math.min(2, rain_level);
		double heat = (heat_level - 3) * 0.005;
		return Math.min(1.0, param.natural_base + irrigation * rain_multiplier + 0.1 * rain_level) + heat;
	}
	
	public double GetAgricultureTechMultiplier(State state) {
		double m = 1.0;
		StateTechnology tech = state.GetTechnology();
		for (Technology t : data.GetConstData().agriculture_boost_techs) {
			if (tech.Has(t)) {
				m += t.agriculture_boost;
			}
		}
		return m;
	}
	
	public double GetAgricultureGovernorMultiplier(City city) {
		return 1.0 + param.governor_algriculture_boost *
				(utils.person_util.GetAbility(city.GetGovernor(), AbilityType.ADMIN) - param.base_ability)
				+ param.governor_algriculture_boost *
				(utils.person_util.GetAbility(city.GetOwner().GetOfficer(RoleType.ADMIN_OFFICER),
						AbilityType.ADMIN) - param.base_ability);
	}
	
	public double GetAgricultureSiegeMultiplier(City city) {
		if (utils.city_util.IsUnderSiege(city) != null) {
			return param.siege_agriculture_multiplier;
		}
		return 1.0;
	}
	
	public double GetAgricultureDisasterMultiplier(City city) {
		return 1.0 * (1.0 - city.GetNaturalInfo().GetFloodSeverity()) * (1.0 - city.GetNaturalInfo().GetLocustSeverity()
				* GetAgricultureSiegeMultiplier(city));
	}
	
	public double GetAgricultureLaborMultiplier(City city) {
		if (city.Get().population <= 0) return 1.0;
		return 1.0 - Math.min(param.max_labor_punishment,
				param.labor_punishment_multiplier * ((city.Get().labor + city.Get().military_service) / 30 / city.Get().population));
	}
	
	public double GetIdeologyProductionMultiplier(City city) {
		return GetIdeologyProductionMultiplier(city.GetOwner());
	}
	
	public double GetIdeologyProductionMultiplier(State state) {
		if (state.GetPolicy().HasIdeology(Ideology.TAOISM)) return 1.15;
		return 1;
	}
	
	public String GetAgricultureTechMultiplierTooltip(State state) {
		String str = "<html>";
		StateTechnology tech = state.GetTechnology();
		for (Technology t : data.GetConstData().agriculture_boost_techs) {
			if (tech.Has(t)) {
				str += t.name + " +" + StringUtil.Percentage(t.agriculture_boost) + "<br>";
			}
		}
		return str + "</html>";
	}
	
	public long GetFoodYield(City city, long peasant, int rain_level, int heat_level) {
		CityImprovements improvements = city.GetImprovements();
		int farm = improvements.GetCount(ImprovementType.FARM);
		int aqeducted_farm = improvements.GetCount(ImprovementType.AQEDUCTED_FARM);
		int irrigated_farm = improvements.GetCount(ImprovementType.IRRIGATED_FARM);
		int total_farm = farm + aqeducted_farm + irrigated_farm;
		if (total_farm <= 0) return 0L;
		double peasant_multiplier = 0;
		peasant /= total_farm;
		for (int i = 0; i < 3 && peasant > 0; ++i) {
			long peasant_used = Math.min(param.population_per_farm, peasant);
			peasant_multiplier += peasant_used * param.peasant_productivity[i];
			peasant -= peasant_used;
		}
		int oxed_farm = improvements.GetCount(ImprovementType.OXED_FARM);
		int ironed_farm = improvements.GetCount(ImprovementType.IRONED_FARM);
		int oxed_ironed_farm = Math.min(oxed_farm, ironed_farm);
		oxed_farm -= oxed_ironed_farm;
		ironed_farm -= oxed_ironed_farm;
		int[] farm_types = { irrigated_farm, aqeducted_farm, farm };
		int[] improvement_types = { oxed_ironed_farm, ironed_farm, oxed_farm, Integer.MAX_VALUE };
		double[] bases = { GetNatureBase(ImprovementType.IRRIGATED_FARM, rain_level, heat_level),
				GetNatureBase(ImprovementType.AQEDUCTED_FARM, rain_level, heat_level),
				GetNatureBase(ImprovementType.FARM, rain_level, heat_level) };
		double iron_multiplier = GetFarmYieldParam(ImprovementType.IRONED_FARM);
		double ox_multiplier = GetFarmYieldParam(ImprovementType.OXED_FARM);
		double[] multipliers = { 1.0 + iron_multiplier + ox_multiplier, 1.0 + iron_multiplier, 1.0 + ox_multiplier, 1.0 };
		int i = 0, j = 0;
		double total_base = 0.0;
		while(i < 3) {
			if (farm_types[i] == improvement_types[j]) {
				total_base += bases[i] * multipliers[j] * farm_types[i];
				i++;
				j++;
			} else if (farm_types[i] < improvement_types[j]) {
				total_base += bases[i] * multipliers[j] * farm_types[i];
				improvement_types[j] -= farm_types[i];
				i++;
			} else if (farm_types[i] > improvement_types[j]) {
				total_base += bases[i] * multipliers[j] * improvement_types[j];
				farm_types[i] -= improvement_types[j];
				j++;
			}
		}
		double tech_multiplier = GetAgricultureTechMultiplier(city.GetOwner());
		double disaster_multiplier = GetAgricultureDisasterMultiplier(city);
		double labor_multiplier = GetAgricultureLaborMultiplier(city);
		double governor_multiplier = GetAgricultureGovernorMultiplier(city);
		double production_multiplier = GetIdeologyProductionMultiplier(city);
		return (long) (total_base * peasant_multiplier * tech_multiplier * disaster_multiplier * labor_multiplier
				* governor_multiplier * production_multiplier);
	}
	
	// Commerce
	public double GetCommercePoints(CityCommerce commerce, ImprovementType type) {
		double points = commerce.GetImports().containsKey(type) ? param.import_commerce_point : 0;
		points += commerce.GetNumTransfers(type) * param.transfer_commerce_point;
		points += commerce.GetNumExports(type) > 0 ? param.export_commerce_point : 0;
		return points;
	}
	
	public double GetIncomePerMerchant(City city, double merchant_pct) {
		CityCommerce commerce = city.GetCommerce();
		merchant_pct *= 100;
		double commerce_points = 0;
		for (ImprovementType type : Improvement.kIndustryImprovements) {
			commerce_points += GetCommercePoints(commerce, type);
		}
		return param.base_commerce_income + param.bonus_commerce_income * GetCommerceTechMultiplier(city.GetOwner()) *
				Math.min(merchant_pct, commerce_points / param.merchant_pct_commerce_point_coverage) / merchant_pct;
	}
	
	public double GetCommerceTechMultiplier(State state) {
		double m = 1.0;
		StateTechnology tech = state.GetTechnology();
		for (Technology t : data.GetConstData().commerce_boost_techs) {
			if (tech.Has(t)) {
				m += t.commerce_boost;
			}
		}
		return m;
	}
	
	// Tax
	public double GetTaxEfficiency(City city) {
		double tax_efficiency = param.tax_efficiency_base;
		State owner = city.GetOwner();
		tax_efficiency += GetTaxEfficiencyBoost(owner);
		double city_multiplier = GetTaxEfficiencyFromType(city);
		double stability_multiplier = GetTaxEfficiencyFromStability(owner);
		double foreigner_mulitplier = GetTaxEfficiencyFromStabilityFromRaces(city);
		double governor_multiplier = GetTaxEfficiencyFromStabilityFromGovernor(city);
		double ideology_multiplier = GetTaxEfficiencyFromStabilityFromIdeology(owner);
		return tax_efficiency * city_multiplier * stability_multiplier * foreigner_mulitplier * governor_multiplier * ideology_multiplier;
	}
	
	public double GetTaxEfficiencyBoost(State state) {
		double boost = 0;
		StateTechnology tech = state.GetTechnology();
		for (Technology t : data.GetConstData().tax_boost_techs) {
			if (tech.Has(t)) {
				boost += t.tax_boost;
			}
		}
		return boost;
	}
	
	public double GetTaxEfficiencyFromType(City city) {
		CityType type = city.GetType();
		if (type == CityType.CAPITAL) return 1.0;
		if (type == CityType.NONE) return param.feudal_tax_efficiency;
		if (type == CityType.JIMI_COUNTY) return param.jimi_county_tax_efficiency;
		double multiplier = param.county_tax_efficiency;
		multiplier -= param.county_tax_efficiency_transportation_penalty * city.GetTransportationToCapital()
				/ (city.GetOwner().GetTechnology().HasEffect(Effect.HALVE_DISTANCE_PENALTY_FOR_COUNTY) ? 2 : 1);
		return Math.max(param.min_county_tax_efficiency, multiplier);
	}
	
	public double GetTaxEfficiencyFromStability(State state) {
		return 1.0 - (ConstStateData.kMaxStability - state.Get().stability) * param.tax_stability_penalty;
	}
	
	public double GetTaxEfficiencyFromStabilityFromRaces(City city) {
		double foreigner_mulitplier = 1.0 - (1.0 - city.GetPopulation().GetPopRatio(0)) * param.tax_foreigner_discount;
		if (city.GetOwner().GetPolicy().HasIdeology(Ideology.NINE_RANK_SYSTEM)) {
			CityPopulation city_pop = city.GetPopulation();
			foreigner_mulitplier = city_pop.GetPopRatio(0);
			for (int i = 1; i < city_pop.NumRaces(); ++i) {
				double ratio = city_pop.GetPopRatio(i);
				if (!city_pop.GetRace(i).Playable() && !city_pop.GetRace(i).GetDescription().IsNonHuaxiaRace()) {
					foreigner_mulitplier += ratio * param.nine_rank_system_tax_nonplayable_foreigner_discount;
				} else {
					foreigner_mulitplier += ratio * param.tax_foreigner_discount;
				}
			}
		}
		return foreigner_mulitplier;
	}
	
	public double GetTaxEfficiencyFromStabilityFromGovernor(City city) {
		return 1.0 + param.governor_tax_efficiency_boost *
				(utils.person_util.GetAbility(city.GetGovernor(), AbilityType.ADMIN) +
						utils.person_util.GetAbility(city.GetGovernor(), AbilityType.DIPLOMACY) / 3
						- param.base_ability - param.base_ability / 3);
	}
	
	public double GetTaxEfficiencyFromStabilityFromIdeology(State state) {
		return state.GetPolicy().HasIdeology(Ideology.LEGALISM) ? 1.2 : 1;
	}
	
	public int GetMaxExportAmount(ResourceType type) {
		return Math.min((int)(data.GetMarket().GetLatestAmount(type) * param.max_export_ratio), param.max_export_amount);
	}
	
	private double GetFarmYieldParam(ImprovementType improvement_type) {
		return param.GetImprovementYield(improvement_type).food;
	}
	
	private Resource<Long> GetProductionInternal(State state, ImprovementType improvement_type, double worker) {
		Resource<Long> yield = new Resource<Long>(0L);
		double effectiveness = worker * GetIndustrialTechBoost(state, improvement_type) * GetIdeologyProductionMultiplier(state);
		Resource.CollectResource(yield, param.GetImprovementYield(improvement_type), effectiveness);
		return yield;
	}
	
	private double GetIndustrialTechBoost(State state, ImprovementType type) {
		if (type == ImprovementType.SALT || type == ImprovementType.IRON_MINE) {
			if (state.GetTechnology().HasEffect(Effect.BOOST_SALT_IRON)) return 1.5;
		} else if (type == ImprovementType.PASTURE) {
			if (state.GetTechnology().HasEffect(Effect.BOOST_HORSE)) return 1.2;
		} else if (type == ImprovementType.SILK) {
			if (state.GetTechnology().HasEffect(Effect.BOOST_SILK)) return 1.5;
		} else if (type == ImprovementType.FISH) {
			if (state.GetTechnology().HasEffect(Effect.BOOST_FISH)) return 1.5;
		} else if (type == ImprovementType.MINE) {
			if (state.GetTechnology().HasEffect(Effect.BOOST_MINE)) return 1.2;
		} else if (type == ImprovementType.CHINA) {
			if (state.GetTechnology().HasEffect(Effect.BOOST_CHINA)) return 1.5;
		}
		return 1.0;
	}
}
