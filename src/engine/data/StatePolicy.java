package ac.engine.data;

import ac.data.StateData.PolicyData;
import ac.data.constant.Policies;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Ideologies;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Ideologies.IdeologyType;
import ac.data.constant.Policies.Policy;

public class StatePolicy extends Data {

	protected StatePolicy(DataAccessor accessor, State state, PolicyData[] data) {
		super(accessor);
		this.state = state;
		this.data = data;
	}
	
	public Policy GetPolicy(AbilityType type) {
		int policy_id = data[type.ordinal()].policy_id;
		if (policy_id <= 0) return Policy.NONE;
		return Policy.values()[policy_id];
	}
	
	public int GetPolicyProgress(AbilityType type) {
		return data[type.ordinal()].progress;
	}
	
	public IdKeyedData GetPolicyObject(AbilityType type) {
		switch (GetPolicy(type)) {
		case CONVERT_FOREIGNERS:
		case INCREASE_HAPPINESS:
		case SUPPRESS_REVOLT:
		case ESTABLISH_COUNTY:
		case CHANGE_CAPITAL:
		case ESTABLISH_JIMI_COUNTY:
		case MIGRATE:
			return accessor.cities.get(data[type.ordinal()].object);
		case INCREASE_RELATIONSHIP:
		case DECREASE_RELATIONSHIP:
		case DENOUNCE:
		case PROPOSE_ALLY:
		case PROPOSE_OPEN_BORDER:
		case PROPOSE_VASSAL:
		case PROPOSE_SUZERAINTY:
		case PROPOSE_ALLIANCE:
		case CEASE_ALLY:
		case CEASE_VASSAL:
		case CEASE_SUZERAINTY:
		case CEASE_ALLIANCE:
		case CEASE_OPEN_BORDER:
			return accessor.states.get(data[type.ordinal()].object);
		case CONVERT_RECRUITMENT_SOLDIERS_TO_FUBING:
		case CONVERT_RECRUITMENT_SOLDIERS_TO_CONSCRIPTION:
		case CONVERT_CONSCRIPTION_SOLDIERS_TO_FUBING:
		case CONVERT_CONSCRIPTION_SOLDIERS_TO_RECRUITMENT:
			return state.GetMilitary().GetArmy(data[type.ordinal()].object);

		case INCREASE_STABILITY:
		case NONE:
		case INCREASE_PRESTIGE:
		default:
			break;
		}
		return null;
	}
	
	public IdKeyedData GetPolicyObject2(AbilityType type) {
		if (GetPolicy(type) == Policy.MIGRATE) {
			return accessor.cities.get(data[type.ordinal()].object2);
		}
		if (GetPolicy(type) == Policy.DECREASE_RELATIONSHIP) {
			return accessor.states.get(data[type.ordinal()].object2);
		}
		return null;
	}
	
	public long GetPolicyContextQuantity(AbilityType type) {
		return data[type.ordinal()].quantity;
	}
	
	public Ideology GetIdeology(IdeologyType type) {
		return state.Get().ideologies[type.ordinal()];
	}
	
	public boolean HasIdeology(Ideology ideology) {
		return (ideology == GetIdeology(Ideologies.ideology_types.get(ideology)));
	}
	
	public void SetPolicy(Policy policy, IdKeyedData object, IdKeyedData object2, Long quantity) {
		SetPolicy(policy, object == null ? -1 : object.id, object2 == null ? -1 : object2.id, quantity == null ? -1 : quantity.longValue());
	}
	
	public void ResetPolicy(AbilityType type) {
		PolicyData policy_data = data[type.ordinal()];
		policy_data.policy_id = 0;
		policy_data.object = -1;
		policy_data.object2 = -1;
		policy_data.quantity = - 1;
		policy_data.progress = 0;
	}
	
	public int IncreasePolicyProgress(AbilityType type, int inc) {
		PolicyData policy_data = data[type.ordinal()];
		policy_data.progress += inc;
		return policy_data.progress;
	}
	
	private void SetPolicy(Policy policy, int id, int id2, long quantity) {
		PolicyData policy_data = data[Policies.GetType(policy).ordinal()];
		policy_data.policy_id = policy.ordinal();
		policy_data.object = id;
		policy_data.object2 = id2;
		policy_data.quantity = quantity;
		policy_data.progress = 0;
	}
	
	public void SetIdeology(Ideology ideology) {
		IdeologyType type = Ideologies.ideology_types.get(ideology);
		state.Get().ideologies[type.ordinal()] = ideology;
	}

	private State state;
	private PolicyData[] data;
}
