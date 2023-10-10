package ac.data;

import java.util.ArrayList;
import ac.data.ArmyData.SoldierType;
import ac.data.base.Date;
import ac.data.constant.ConstCityData;
import ac.data.constant.Improvement;
import ac.data.constant.Improvement.ImprovementType;

public class CityData {
	public String name;
	public long population;
	public int owner;
	public long remaining_food;
	public long labor;
	public long military_service;
	public int happiness;
	public int riot;
	public boolean is_county;
	public boolean is_jimi_county;
	
	public enum Profession { PEASANT, WORKER, MERCHANT, SOLDIER };
	public int[] profression_target_pct = { 5, 5, 5 };
	public double[] profression_ratio = { 0.05, 0.05, 0.05 };
	
	public class ImprovementData {		
		public int[] agriculture_improvements = new int[Improvement.kAgricultureImprovements.length];
		public int[] industry_improvements = new int[Improvement.kIndustryImprovements.length];
		public int[] auxiliary_improvements = new int[Improvement.kAuxiliaryImprovements.length];
		public ImprovementType construction;
		public Date complete_date;
	}
	public ImprovementData improvements = new ImprovementData();
	
	public static class RacePopulation {
		public int race;
		public double ratio;
	}
	
	public ArrayList<RacePopulation> race_dist = new ArrayList<RacePopulation>();
	
	public static class RecruitmentData {
		public int army = -1;
		public SoldierType soldier_type;
		public int unit;
	}
	
	public class MilitaryData {
		// Garrison
		public long garrison_max;
		public ArmyData garrison = ArmyData.CreateGarrison();
		public int advanced_unit_pct = 20;
		// Recruiting
		public RecruitmentData recruiting;
		public Date complete_date;
	}
	
	public MilitaryData military = new MilitaryData();
	
	public class StatefulData {
		public int rain_level = 0;
		public int temperature_level = 0;
		public double flood_severity = 0;
		public double locust_severity = 0;
	}
	
	public StatefulData stateful_data;
	
	public void Reset() {
		owner = 0;
		remaining_food = 0;
		happiness = ConstCityData.kMaxHappiness;
	}
}
