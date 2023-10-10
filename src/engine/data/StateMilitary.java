package ac.engine.data;

import java.util.ArrayList;

import ac.data.ArmyData;
import ac.data.StateData.MilitaryData;

public class StateMilitary extends Data {

	protected StateMilitary(DataAccessor accessor, State state, MilitaryData data) {
		super(accessor);
		
		this.data = data;
		this.state = state;
		for (int i = 0; i < data.armies.length; ++i) {
			ArmyData army_data = data.armies[i];
			if (army_data == null) break;
			Army army = accessor.CreateArmy(state, i, army_data);
			armies.add(army);
		}
	}
	
	public ArrayList<Army> GetArmies() {
		return armies;
	}
	
	public Army AddArmy() {
		for (int i = 0; i < data.armies.length; ++i) {
			if (data.armies[i] != null) continue;
			data.armies[i] = ArmyData.CreateArmy(state.GetCapital().GetCoordinate());
			Army army = accessor.CreateArmy(state, i, data.armies[i]);
			armies.add(army);
			return army;
		}
		return null;
	}
	
	protected Army GetArmy(int index) {
		if (index < 0 || index >= armies.size()) return null;
		return armies.get(index);
	}

	private State state;
	private MilitaryData data;
	private ArrayList<Army> armies = new ArrayList<Army>();
}
