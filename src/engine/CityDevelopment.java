package ac.engine;

import java.util.ArrayList;

import ac.data.base.Date;
import ac.data.base.Resource;
import ac.data.constant.ConstCityData;
import ac.data.constant.ConstStateData;
import ac.data.constant.Texts;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Improvement.SpecialImprovementType;
import ac.data.constant.Parameters;
import ac.data.constant.Technology.Effect;
import ac.engine.ai.GameInterface;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.City.CityType;
import ac.engine.data.CityImprovements;
import ac.engine.data.CityMilitary;
import ac.engine.data.CityMilitary.RecruitmentInfo;
import ac.engine.data.CityPopulation;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.engine.util.Utils;
import ac.util.RandomUtil;

public class CityDevelopment {
	protected CityDevelopment(DataAccessor data, GameInterface ai) {
		this.data = data;
		this.param = data.GetParam();
		this.utils = data.GetUtils();
		this.ai = ai;
	}
	
	public void HandlePopulation(City city) {
		long food_consumed = utils.pop_util.FoodConsumption(city.Get().population, city.Get().remaining_food,
				utils.pop_util.RemainingMonthBeforeHarvest(data.GetDate().GetMonth() - 1));
		double nature_pop_growth_rate = utils.pop_util.GrowthRate(city.GetOwner(), city.Get().population, food_consumed, city.GetNaturalInfo().GetTemperatureLevel());
		double pop_growth_rate = nature_pop_growth_rate - utils.pop_util.DecreaseRate(city.GetPopulation().GetHappiness());
		city.Get().remaining_food -= food_consumed;
		
		// Adjust race distribution
		CityPopulation city_pop = city.GetPopulation();
		if (city.GetType() != CityType.JIMI_COUNTY) {
			int num_races = city_pop.NumRaces();
			if (pop_growth_rate > 0 && num_races > 1) {
				double rate = pop_growth_rate / (1.0 + city_pop.GetPopRatio(0));
				for (int i = 1; i <= num_races; ++i) {
					city_pop.SetPopRatio(i, city_pop.GetPopRatio(i) * (1 + rate) / (1 + pop_growth_rate));
				}
				city_pop.Clear();
			}
		}
		if (pop_growth_rate < 0) {
			pop_growth_rate = Math.max(pop_growth_rate, param.city_min_population / city.Get().population - 1.0);
		}
		city.Get().population = (long) (city.Get().population * (1 + pop_growth_rate));
		
		// Adjust profession distribution
		double[] prof_ratio = city_pop.GetProfessionRatios();
		double[] prof_target_ratio = city_pop.GetProfessionTargetRatios();
		double total_increase = 0;
		double total_decrease_denominator = 0;
		for (int i = 0; i < prof_ratio.length; ++i) {
			if (prof_ratio[i] < prof_target_ratio[i]) {
				double inc = Math.min(prof_target_ratio[i] - prof_ratio[i], param.profession_shift_ratio);
				prof_ratio[i] += inc;
				total_increase += inc;
			} else if (prof_ratio[i] > prof_target_ratio[i]) {
				total_decrease_denominator += prof_ratio[i];
			}
		}
		if (total_increase > 0) {
			for (int i = 0; i < prof_ratio.length; ++i) {
				if (prof_ratio[i] > prof_target_ratio[i]) {
					prof_ratio[i] = prof_ratio[i] * (total_decrease_denominator - total_increase) / total_decrease_denominator;
				}
			}
			city_pop.SetProfessionRatios(prof_ratio);
		}
		
		// Handle Happiness
		int dec = 0;
		if (city.GetOwner().Get().stability < ConstStateData.kMaxStability) dec++;
		if (nature_pop_growth_rate < param.pop_decrease_cutoff_for_unhappiness) dec++;
		if (dec > 0) city.GetPopulation().ChangeHappiness(-dec);
		
		// Handle Garrison Max
		if (pop_growth_rate > 0) city.GetMilitary().GetGarrison().UpdateGarrisonMax();
	}
	
	public void HandleClimate() {
		utils.city_util.HandleRain();
		utils.city_util.HandleTemperature();
		utils.city_util.HandleDisaster();
	}
	
	public void HandleHarvest() {
		for (City city : data.GetAllPlayableCities()) {
			long food = utils.city_util.GetFoodYield(city);
			State owner = city.GetOwner();
			//Resource<Long> taxed = new Resource<Long>(0L);
			long taxed_food = (long) (food * owner.GetEconomic().GetFoodTax());
			long collected_food = (long) (taxed_food * utils.prod_util.GetTaxEfficiency(city));
			owner.GetResource().food += collected_food;
			city.Get().remaining_food += (food - taxed_food);
			data.GetMarket().AddFoodToMarket(food * param.market_harvest_ratio);
		}
		for (City city : data.GetAllCities()) {
			city.GetNaturalInfo().ResetState();
		}
	}
	
	public void HandleConstructionDone() {
		Date today = data.GetDate();
		boolean new_impr_created = false;
		for (City city : data.GetAllCities()) {
			CityImprovements city_impr = city.GetImprovements();
			if (today.equals(city_impr.GetConsturctionCompleteDate())) {
				ImprovementType impr_type = city_impr.GetCurrentConstruction();
				if (city_impr.GetCount(impr_type) == 0) new_impr_created = true;
				city_impr.IncrementCount(impr_type);
				city_impr.ResetConstruction();
				if (impr_type == ImprovementType.SPECIAL) {
					HandleSpecialConstructionDone(city);
				}
			}
			if (today.equals(city.GetMilitary().GetRecruitmentCompleteDate())) {
				CityMilitary military = city.GetMilitary();
				RecruitmentInfo info = military.GetRecruitmentInfo();
				info.army.AddSoldiers(info.unit, info.type, city, param.base_recruitment);
				military.ResetRecruitment();
			}
		}
		if (new_impr_created) {
			utils.trans_util.UpdateTradeRoutes();
		}
	}
	
	public void HandleBarbarian() {
		ArrayList<String> city_names = new ArrayList<String>();
		for (City city : data.GetAllPlayableCities()) {
			if (RandomUtil.WhetherToHappend(city.GetNaturalInfo().GetBarbarianRisk() * param.barbarian_prob_multiplier
					* (city.GetOwner().GetTechnology().HasEffect(Effect.REDUCE_BARBARIAN_PROB) ? 0.7 : 1.0), data.GetRandom())) {
				if (city.GetImprovements().GetFinishedSpecialImprovement() == SpecialImprovementType.GREATWALL &&
						RandomUtil.WhetherToHappend(param.greatwall_barbarian_defence_prob, data.GetRandom())) continue;
				if (HandleBarabarianInvade(city)) {
					city_names.add(city.GetName());
				}
			}
		}
		if (!city_names.isEmpty()) {
			data.AddMessage(String.join(",", city_names) + Texts.happen + Texts.barbarianLoot);
		}
	}
	
	private boolean HandleBarabarianInvade(City city) {
		Army garrison = city.GetMilitary().GetGarrison();
		double soldiers = garrison.GetTotalSoldier() * garrison.GetMorale() * garrison.GetTrainingLevel();
		for (Army army : data.GetArmiesByPosition(city.GetPosition())) {
			soldiers += army.GetTotalSoldier() * army.GetMorale() * army.GetTrainingLevel();
			if (soldiers >= param.min_soldier_defend_barbarian) return false;;
		}
		int max_wall = utils.city_util.GetMaxImprovement(city, ImprovementType.WALL);
		int wall = city.GetImprovements().GetCount(ImprovementType.WALL);
		if (RandomUtil.WhetherToHappend(wall * param.wall_barbarian_defence_prob, data.GetRandom())) {
			city.GetImprovements().DecreaseCount(ImprovementType.WALL);
			return true;
		}
		double severity = 1.0 * ((double)(max_wall - wall) / max_wall) * (1.0 - (soldiers / param.min_soldier_defend_barbarian));
		if (severity > param.barbarian_loot_improvement_severity_threshold &&
				RandomUtil.WhetherToHappend(param.barbarian_loot_improvement_prob, data.GetRandom())) {
			utils.city_util.HandleCityImprovementDamage(city);
			city.GetPopulation().ChangeHappiness(-param.barbarian_loot_happiness_decrease);
			return true;
		}
		double max_loss_ratio = utils.city_util.GetCityWeight(city) * severity;
		double loss_ratio = RandomUtil.SampleFromUniformDistribution(max_loss_ratio * param.min_barbarian_loot_multiplier, max_loss_ratio, data.GetRandom());
		Resource.Reduce(city.GetOwner().GetResource(), loss_ratio);
		return true;
	}
	
	public void HandleLabor() {
		for (City city : data.GetAllPlayableCities()) {
			city.Get().labor += utils.pop_util.GetTotalLabor(city);
			city.Get().military_service += utils.pop_util.GetTotalMilitaryInService(city);
		}
	}
	
	public void ResetLaborForYear() {
		for (City city : data.GetAllPlayableCities()) {
			boolean state_has_taoism = city.GetOwner().GetPolicy().HasIdeology(Ideology.TAOISM);
			double threshold = state_has_taoism ? 0 : city.Get().population * param.labor_ratio_for_unhappiness;
			if (city.Get().labor / 30 > threshold) {
				int dec = state_has_taoism  ? -2 : -1;
				city.GetPopulation().ChangeHappiness(dec);
			}
			city.Get().labor = 0L;
			city.Get().military_service = 0L;
		}
	}
	
	public void HandleRiot() {
		for (City city : data.GetAllPlayableCities()) {
			int riot_point = utils.city_util.GetRiotPoint(city);
			if (riot_point > 0) {
				for (int i = 10000; i > 0 ; i -= 2000) {
					if (city.Get().riot < i && city.Get().riot + riot_point >= i) ai.OnCityRiotIncreased(city.GetOwner(), city, i);
				}
				city.Get().riot = Math.min(ConstCityData.kMaxRiotPoints, city.Get().riot + riot_point);
			}
			if (utils.city_util.CheckRiot(city)) {
				// System.err.println(city.Get().name + " riot!");
				utils.city_util.HandleCityLost(city, false, ai);
				State old_owner = utils.city_util.ChangeOwner(city, null);
				ai.OnCityLost(old_owner, city);
				data.AddMessage(city.GetName() + Texts.riot);
				utils.trans_util.UpdateTradeRoutes();
			}
		}
	}
	
	private void HandleSpecialConstructionDone(City city) {
		CityImprovements city_impr = city.GetImprovements();
		SpecialImprovementType type = city_impr.GetFinishedSpecialImprovement();
		if (type == null || type == SpecialImprovementType.NONE) return;
		data.AddMessage(city.GetName() + Texts.built + Texts.specialImprovements[type.ordinal()]);
		//System.err.println(city.GetName() + Texts.built + Texts.specialImprovements[type.ordinal()]);
		data.GetOverrides().ApplySpecialImprovement(type);
	}

	private DataAccessor data;
	private Parameters param;
	private Utils utils;
	private GameInterface ai;
}
