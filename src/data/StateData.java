package ac.data;

import java.util.ArrayList;

import ac.data.base.Resource;
import ac.data.constant.Ability;
import ac.data.constant.ConstStateData;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Technology.TechnologyType;

public class StateData {
//	public String name;
//	public String official_name;
//	public String alias;
//	public String nobility;
//	public String family_name;
//	public String race;
	
	public boolean playable;
	public String alias;
	
	public int capital = -1;
	public int stability = 4;
	public int prestige;
	public int[] points = new int[Ability.kMaxTypes];
	public Ideology[] ideologies = { Ideology.NONE, Ideology.NONE, Ideology.NONE };
	
	public class EconomicData {
		public int food_tax_pct = 5;
		public int food_budget_pct = 10;
		public int horse_budget_pct = 20;
		public int iron_budget_pct = 20;
		public int research_budget_pct = 10;
	}
	
	public EconomicData economic = new EconomicData();
	
	public class TechnologyData {
		public TechnologyData() {
			for (int i = 0; i < obtained.length; ++i) obtained[i] = -1;
		}
		public int[] obtained = new int[TechnologyType.values().length];
		public TechnologyType researching = TechnologyType.ECONOMIC;
		public long progress = 0;
	}
	
	public TechnologyData technologies = new TechnologyData();
	
	public class PolicyData {
		public int policy_id = -1;
		public int progress = 0;
		public int object = -1;
		public int object2 = -1;
		public long quantity = -1;
	}
	
	public PolicyData[] policies = new PolicyData[Ability.kMaxTypes];
	
	public Resource<Long> resources = new Resource<Long>(0L);
	
	public static class DiplomacyData {
		public static class StateRelationship {
			public int attitude;
			// public boolean at_war;
			public boolean ally;
			public boolean open_border;
			public boolean alliance;
		}
		//public StateRelationship[] states;
		public ArrayList<StateRelationship> states = new ArrayList<StateRelationship>();
		public int suzerainty_state = -1;
	}
	
	public DiplomacyData diplomacy = new DiplomacyData();
	
	public class MilitaryData {
		public ArmyData[] armies = new ArmyData[ConstStateData.kMaxArmies];
		
	}
	
	public MilitaryData military = new MilitaryData();
	
	public StateData() {
		for (int i = 0; i < Ability.kMaxTypes; ++i) policies[i] = new PolicyData();
	}
}
