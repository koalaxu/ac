package ac.engine.data;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import ac.data.CityData;
import ac.data.CityData.Profession;
import ac.data.CityData.RacePopulation;
import ac.data.constant.ConstCityData;

public class CityPopulation extends Data {

	protected CityPopulation(DataAccessor accessor, CityData data, City city) {
		super(accessor);
		this.data = data;
		this.pops = data.race_dist;
	}
	
	public int NumRaces() {
		return pops.size() + 1;
	}
	
	public State GetRace(int index) {
		if (index < 0 || index > pops.size()) return null;
		if (index == 0) return accessor.GetState(data.owner);
		return accessor.GetState(pops.get(index - 1).race);
	}
	
	public double GetPopRatio(State race) {
		for (RacePopulation race_pop : pops) {
			if (race_pop.race == race.id) return race_pop.ratio;
		}
		return 0;
	}
	
	public double GetPopRatio(int index) {
		if (index < 0 || index > pops.size()) return 0;
		if (index > 0) return pops.get(index - 1).ratio;
		double total = 1.0;
		for (RacePopulation pop : pops) {
			total -= pop.ratio;
		}
		return total;
	}
	
	public void SetPopRatio(int index, double value) {
		if (index <= 0 || index > pops.size()) return;
		pops.get(index - 1).ratio = value;
	}
	
//	public void ResetPopRatio() {
//		double native_ratio = GetPopRatio(0);
//		RacePopulation race_pop = new RacePopulation();
//		race_pop.race = data.owner;
//		race_pop.ratio = native_ratio;
//		pops.add(race_pop);
//	}
	
	public void ResetPopRatio(TreeMap<State, Double> race_pops) {
		pops.clear();
		for (Entry<State, Double> entry : race_pops.entrySet()) {
			RacePopulation race_pop = new RacePopulation();
			race_pop.race = entry.getKey().id;
			race_pop.ratio = entry.getValue();
			pops.add(race_pop);
		}
	}
	
	public void RemoveRace(State race) {
		pops.removeIf(race_pop -> race_pop.race == race.id);
	}
	
	public void Clear() {
		pops.removeIf(race_pop -> race_pop.ratio < kEpsilon);
	}
	
	public double GetProfessionRatio(Profession profession) {
		if (profession.ordinal() > 0) return data.profression_ratio[profession.ordinal() - 1];
		double ret = 1.0;
		for (int i = 0; i < data.profression_ratio.length; ++i) {
			ret -= data.profression_ratio[i];
		}
		return ret;
	}
	
	public int GetProfessionTargetPct(Profession profession) {
		if (profession.ordinal() > 0) return data.profression_target_pct[profession.ordinal() - 1];
		int ret = 100;
		for (int i = 0; i < data.profression_target_pct.length; ++i) {
			ret -= data.profression_target_pct[i];
		}
		return ret;
	}
	
	public double[] GetProfessionRatios() {
		profession_ratios[0] = 1.0;
		for (int i = 1 ; i < profession_ratios.length; ++i) {
			profession_ratios[i] = data.profression_ratio[i - 1];
			profession_ratios[0] -= profession_ratios[i];
		}
		return profession_ratios;
	}
	
	public double[] GetProfessionTargetRatios() {
		profession_target_ratios[0] = 1.0;
		for (int i = 1 ; i < profession_target_ratios.length; ++i) {
			profession_target_ratios[i] = (double)data.profression_target_pct[i - 1] / 100.0;
			profession_target_ratios[0] -= profession_ratios[i];
		}
		return profession_target_ratios;
	}
	
	public int GetHappiness() {
		return data.happiness;
	}
	
	public void SetProfessionRatios(double[] profession_ratios) {
		for (int i = 1 ; i < profession_ratios.length; ++i) {
			data.profression_ratio[i - 1] = profession_ratios[i];
		}
	}
	
	// 0 = WORKER; 1 = MERCHANT; 2 = SOILDERS
	public void SetProfessionTargetPct(int[] prefession_target_pct) {
		for (int i = 0 ; i < prefession_target_pct.length; ++i) {
			data.profression_target_pct[i] = prefession_target_pct[i];
		}
	}
	
	public void ChangeHappiness(int dec) {
		data.happiness = Math.max(0, Math.min(ConstCityData.kMaxHappiness, data.happiness + dec));
	}

	private CityData data;
	private ArrayList<RacePopulation> pops;
	private double[] profession_ratios = new double[Profession.values().length];
	private double[] profession_target_ratios = new double[Profession.values().length];
	
	private static double kEpsilon = 0.0001;
} 