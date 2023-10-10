package ac.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import ac.data.CityData.RecruitmentData;
import ac.data.base.Date;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Improvement.ImprovementType;

public class PlayerData {
	public int player = -1;
	public HashMap<Integer, Vector<ImprovementType>> construction_waiting_list = new HashMap<Integer, Vector<ImprovementType>>();
	public HashMap<Integer, Vector<RecruitmentData>> recruitment_waiting_list = new HashMap<Integer, Vector<RecruitmentData>>();
	public HashSet<Integer> auto_appease_opt_out = new HashSet<Integer>();
	public HashSet<AbilityType> policy_auto_renew = new HashSet<AbilityType>();
	public static class InformationData {
		public Date date;
		public enum InformationType {
			UNKNOWN,
			TECH_COMPLETED,
			POLICY_INVALIDED,
			POLICY_COMPLETED,
			CITY_RIOT_INCREASED,
			CITY_INVADED,
			CITY_LOST,
			MONARCH_DIED,
			PERSON_DIED,
			TREATY_RECEIVED,
		}
		public InformationType type;
		public int context_id;
	}
	public ArrayList<InformationData> important_messages = new ArrayList<InformationData>();
}
