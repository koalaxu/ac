package ac.ui.swing.panel;

import ac.data.constant.Texts;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.Data;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.ScrollListComponent;
import ac.util.StringUtil;

public class StatePanel extends TypedDataPanel<Data> {
	private static final long serialVersionUID = 1L;
	public StatePanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		state_list = new ScrollListComponent(kColumnWidth, 20);
		state_list.SetColumnHeaders(kColumnNames);
		
		state_list.setBounds(5, 5, 450, 260);
		add(state_list);
	}

	@Override
	public void Reset(Data input) {
		state_list.Resize(data.GetAllPlayableStates().Size());
		int index = 0;
		for (State state : data.GetAllPlayableStates()) {
			state_list.SetValue(index, 0, state.GetName());
			state_list.SetValue(index, 1, StringUtil.LongNumber(cmp.utils.state_util.GetTotalPopulation(state)));
			long total_soldier = 0L;
			for (Army army : state.GetMilitary().GetArmies()) {
				total_soldier += army.GetTotalSoldier();
			}
			state_list.SetValue(index, 2, StringUtil.LongNumber(total_soldier));
			total_soldier = 0L;
			for (City city : state.GetOwnedCities()) {
				total_soldier += city.GetMilitary().GetGarrison().GetTotalSoldier();
			}
			state_list.SetValue(index, 3, StringUtil.LongNumber(total_soldier));
			state_list.SetCallback(index, () -> cmp.state.Show(state));
			index++;
		}
	}

	private ScrollListComponent state_list;
	
	private static final int[] kColumnWidth = { 45, 100, 100, 100};
	private static final String[] kColumnNames = { Texts.state, Texts.population, Texts.soldierNumber, Texts.garrison};
}
