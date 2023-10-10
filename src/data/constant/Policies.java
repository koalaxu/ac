package ac.data.constant;

import java.util.ArrayList;
import java.util.HashMap;

import ac.data.constant.Ability.AbilityType;

public class Policies {
	public enum Policy {
		NONE,
		INCREASE_PRESTIGE,
		INCREASE_STABILITY,
		CONVERT_FOREIGNERS,
		INCREASE_HAPPINESS,
		ESTABLISH_COUNTY,
		INCREASE_RELATIONSHIP,
		DECREASE_RELATIONSHIP,
		DENOUNCE,
		CONVERT_RECRUITMENT_SOLDIERS_TO_FUBING,
		CONVERT_RECRUITMENT_SOLDIERS_TO_CONSCRIPTION,
		CONVERT_CONSCRIPTION_SOLDIERS_TO_FUBING,
		CONVERT_CONSCRIPTION_SOLDIERS_TO_RECRUITMENT,
		PROPOSE_ALLY,
		PROPOSE_OPEN_BORDER,
		PROPOSE_VASSAL,
		PROPOSE_SUZERAINTY,
		PROPOSE_ALLIANCE,
		CEASE_ALLY,
		CEASE_OPEN_BORDER,
		CEASE_VASSAL,
		CEASE_SUZERAINTY,
		CEASE_ALLIANCE,
		CHANGE_CAPITAL,
		SUPPRESS_REVOLT,
		MILITARY_TRAINING,
		ADOPT_IDEOLOGY,
		ESTABLISH_JIMI_COUNTY,
		MIGRATE,
	}
	
	private static int[] policy_cost = { 0, 100, 100, 20, 10, 100, 50, 50, 20, 10, 10, 10, 10, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 100, 15, 15, 150, 50, 5 };
	private static AbilityType[] policy_types = { null, AbilityType.MILITARY, AbilityType.MILITARY, AbilityType.ADMIN,
			AbilityType.ADMIN, AbilityType.ADMIN,
			AbilityType.DIPLOMACY, AbilityType.DIPLOMACY, AbilityType.DIPLOMACY,
			AbilityType.MILITARY, AbilityType.MILITARY, AbilityType.MILITARY, AbilityType.MILITARY,
			AbilityType.DIPLOMACY, AbilityType.DIPLOMACY, AbilityType.DIPLOMACY, AbilityType.DIPLOMACY, AbilityType.DIPLOMACY,
			AbilityType.DIPLOMACY, AbilityType.DIPLOMACY, AbilityType.DIPLOMACY, AbilityType.DIPLOMACY, AbilityType.DIPLOMACY,
			AbilityType.ADMIN, AbilityType.MILITARY, AbilityType.MILITARY, AbilityType.ADMIN, AbilityType.ADMIN, AbilityType.MILITARY,
	};
	
	public static int GetCost(Policy policy) {
		return policy_cost[policy.ordinal()];
	}
	
	public static AbilityType GetType(Policy policy) {
		return policy_types[policy.ordinal()];
	}
	
	public static HashMap<AbilityType, ArrayList<Policy>> policies = new HashMap<AbilityType, ArrayList<Policy>>(){
		private static final long serialVersionUID = 1L; {
		for (AbilityType type : AbilityType.values()) {
			ArrayList<Policy> typed_policies = new ArrayList<Policy>();
			put(type, typed_policies);
			for (Policy policy : Policy.values()) {
				if (GetType(policy) == type) typed_policies.add(policy);
			}
		}
	}};
}
