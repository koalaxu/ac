package ac.engine.data;

import java.util.Collection;

import ac.data.StateData.DiplomacyData;
import ac.data.constant.ConstStateData;

public class StateDiplomacy extends Data {

	protected StateDiplomacy(DataAccessor accessor, State state, DiplomacyData data) {
		super(accessor);
		this.state = state;
		this.data = data;
	}
	
	public int GetAttitude(State state) {
		return data.states.get(state.id).attitude;
	}
	
	public void IncreaseAttitude(State state, int inc) {
		data.states.get(state.id).attitude = Math.min(data.states.get(state.id).attitude + inc, ConstStateData.kMaxAttitude);
	}
	
	public void IncreaseAttitude(State state, int inc, int cap) {
		data.states.get(state.id).attitude = Math.min(data.states.get(state.id).attitude + inc, cap);
	}
	
	public void DecreaseAttitude(State state, int dec) {
		data.states.get(state.id).attitude = Math.max(data.states.get(state.id).attitude - dec, ConstStateData.kMinAttitude);
	}
	
	public boolean IsAlly(State state) {
		if (state == null) return false;
		return data.states.get(state.id).ally;
	}
	
	public boolean IsAlliance(State state) {
		if (state == null) return false;
		return data.states.get(state.id).alliance;
	}
	
	public boolean BorderOpened(State state) {
		return data.states.get(state.id).open_border;
	}
	
	public State GetSuzerainty() {
		if (data.suzerainty_state <= 0) return null;
		return accessor.GetState(data.suzerainty_state);
	}
	
	public Collection<State> GetVassals() {
		return accessor.vassals.Get(state);
	}

	
	public void SetAlly(State state, boolean ally) {
		data.states.get(state.id).ally = ally;
	}
	
	public void SetOpenBorder(State state, boolean open_border) {
		data.states.get(state.id).open_border = open_border;
	}
	
	public void SetSuzerainty(State state) {
		if (state == null) {
			data.suzerainty_state = -1;
			return;
		}
		data.suzerainty_state = state.id;
	}
	
	public void SetAlliance(State state, boolean alliance) {
		data.states.get(state.id).alliance = alliance;
	}

	private State state;
	private DiplomacyData data;
}
