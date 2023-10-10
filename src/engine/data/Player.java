package ac.engine.data;

import java.util.ArrayList;
import java.util.Vector;

import ac.data.ArmyData.SoldierType;
import ac.data.CityData;
import ac.data.CityData.RecruitmentData;
import ac.data.PlayerData;
import ac.data.PlayerData.InformationData;
import ac.data.PlayerData.InformationData.InformationType;
import ac.data.base.Date;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Policies.Policy;
import ac.data.constant.Technology;
import ac.data.constant.Unit;
import ac.engine.data.CityMilitary.RecruitmentInfo;

public class Player extends Data {

	protected Player(DataAccessor accessor, PlayerData data) {
		super(accessor);
		this.data = data;
		for (InformationData info : data.important_messages) {
			information_list.add(new Information(info));
		}
	}
	
	public void SetPlayer(State state) {
		data.player = state.id;
	}
	
	public State GetState() {
		return accessor.GetState(data.player);
	}
	
	public ImprovementType GetNextConstructionImprovement(City city) {
		Vector<ImprovementType> queue = data.construction_waiting_list.get(city.id);
		if (queue == null || queue.isEmpty()) return null;
		return queue.get(0);
	}
	
	public ArrayList<ImprovementType> GetConstructionQueue(City city) {
		ArrayList<ImprovementType> list = new ArrayList<ImprovementType>();
		Vector<ImprovementType> queue = data.construction_waiting_list.get(city.id);
		if (queue == null) return list;
		list.addAll(queue);
		return list;
	}
	
	public RecruitmentInfo GetNextRecruitment(City city) {
		Vector<CityData.RecruitmentData> queue = data.recruitment_waiting_list.get(city.id);
		if (queue == null || queue.isEmpty()) return null;
		return new RecruitmentInfo(city, accessor.GetConstData(), queue.get(0));
	}
	
	public ArrayList<RecruitmentInfo> GetRecruitmentQueue(City city) {
		ArrayList<RecruitmentInfo> list = new ArrayList<RecruitmentInfo>();
		Vector<RecruitmentData> queue = data.recruitment_waiting_list.get(city.id);
		if (queue == null) return list;
		for (RecruitmentData data : queue) {
			list.add(new RecruitmentInfo(city, accessor.GetConstData(), data));
		}
		return list;
	}
	
	public boolean HasAutoAppease(City city) {
		return !data.auto_appease_opt_out.contains(city.id);
	}
	
	public boolean DoesPolicyAutoRenew(AbilityType type) {
		return data.policy_auto_renew.contains(type);
	}
	
	public ImprovementType PopNextConstructionImprovement(City city) {
		Vector<ImprovementType> queue = data.construction_waiting_list.get(city.id);
		if (queue == null) return null;
		return queue.remove(0);
	}
	
	public void AppendConstructionImprovement(City city, ImprovementType impr) {
		Vector<ImprovementType> queue = data.construction_waiting_list.get(city.id);
		if (queue == null) {
			queue = new Vector<ImprovementType>();
			data.construction_waiting_list.put(city.id, queue);
		}
		queue.add(impr);
	}
	
	public ImprovementType CancelLastConstructionImprovement(City city) {
		Vector<ImprovementType> queue = data.construction_waiting_list.get(city.id);
		if (queue == null) return null;
		return queue.remove(queue.size() - 1);
	}
	
	public void CancelAllConstructionImprovement(City city) {
		data.construction_waiting_list.remove(city.id);
	}
	
	public RecruitmentInfo PopNextRecruitment(City city) {
		Vector<RecruitmentData> queue = data.recruitment_waiting_list.get(city.id);
		if (queue == null) return null;
		return new RecruitmentInfo(city, accessor.GetConstData(), queue.remove(0));
	}
	
	public void AppendRecruitment(City city, Army army, Unit unit, SoldierType type) {
		Vector<RecruitmentData> queue = data.recruitment_waiting_list.get(city.id);
		if (queue == null) {
			queue = new Vector<RecruitmentData>();
			data.recruitment_waiting_list.put(city.id, queue);
		}
		RecruitmentData recruitment = new RecruitmentData();
		recruitment.army = army.id;
		recruitment.unit = accessor.GetConstData().GetUnitIndex(unit);
		recruitment.soldier_type = type;
		queue.add(recruitment);
	}
	
	public RecruitmentInfo CancelLastRecruitment(City city) {
		Vector<RecruitmentData> queue = data.recruitment_waiting_list.get(city.id);
		if (queue == null) return null;
		RecruitmentData data = queue.remove(queue.size() - 1);
		if (data == null) return null;
		return new RecruitmentInfo(city, accessor.GetConstData(), data);
	}
	
	public void CancelAllRecruitment(City city) {
		data.recruitment_waiting_list.remove(city.id);
	}
	
	public void SetAutoAppease(City city, boolean opt_out) {
		if (opt_out) {
			data.auto_appease_opt_out.add(city.id);
			return;
		}
		data.auto_appease_opt_out.remove(city.id);
	}
	
	public void SetPolicyAutoRenew(AbilityType type, boolean auto_renew) {
		if (auto_renew) {
			data.policy_auto_renew.add(type);
		} else {
			data.policy_auto_renew.remove(type);
		}
	}
	
	public class Information {
		private Information(InformationData info) {
			this.info = info;
		}
		
		public Date GetDate() {  return info.date;  }
		public InformationType GetType() {  return info.type;  }
		public Technology GetTech() {  return accessor.GetConstData().techs.get(info.context_id);  }
		public Policy GetPolicy()  {  return Policy.values()[info.context_id];  }
		public City GetCity() {  return accessor.cities.get(info.context_id);  }
		public Person GetMonarch() {  return accessor.monarchs.get(info.context_id);  }
		public Person GetPerson() {  return accessor.persons.get(info.context_id);  }
		public int GetNumber() { return info.context_id;  }
		public Treaty GetTreaty()  {
			for (Treaty treaty : accessor.treaties) {
				if (treaty.id == info.context_id) return treaty;
			}
			return null;
		}
		
		private InformationData info;
	}
	
	public ArrayList<Information> GetInformationList() {
		return information_list;
	}
	
	public void AddInformation(InformationType type, IdKeyedData context) {
		AddInformation(type, context.id);
	}
	
	public void AddInformation(InformationType type, Policy context) {
		AddInformation(type, context.ordinal());
	}
	
	public void AddInformation(InformationType type, Technology tech) {
		AddInformation(type, accessor.GetConstData().GetTechIndex(tech));
	}
	
	public void RemoveInformation(Information info) {
		data.important_messages.remove(info.info);
		information_list.remove(info);
	}
	
	private void AddInformation(InformationType type, int context_id) {
		InformationData info = new InformationData();
		info.date = accessor.GetDate().CreateDate(0);
		info.type = type;
		info.context_id = context_id;
		data.important_messages.add(info);
		information_list.add(new Information(info));
	}

	private PlayerData data;
	private ArrayList<Information> information_list = new ArrayList<Information>();
}
