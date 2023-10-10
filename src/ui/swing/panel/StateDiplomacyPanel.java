package ac.ui.swing.panel;

import ac.data.constant.Texts;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.ScrollListComponent;
import ac.util.StringUtil;

public class StateDiplomacyPanel extends TypedDataPanel<State> {

	private static final long serialVersionUID = 1L;

	public StateDiplomacyPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		state_list = new ScrollListComponent(kColumnWidth, 20);
		state_list.SetColumnHeaders(kColumnNames);
		
		state_list.setBounds(5, 5, 450, 260);
		add(state_list);
	}

	@Override
	public void Reset(State input) {
		state_list.Resize(data.GetAllPlayableStates().Size() - 1);
		int index = 0;
		for (State state : data.GetAllPlayableStates()) {
			if (state == input) continue;
			state_list.SetValue(index, 0, state.GetName());
			state_list.SetValue(index, 1, StringUtil.Number(input.GetDiplomacy().GetAttitude(state)));
			state_list.SetCallback(index, () -> cmp.state.Show(state));
			if (cmp.utils.diplomacy_util.AtWar(state, input)) {
				state_list.SetValue(index, 2, Texts.atWar);
			} else if (cmp.utils.diplomacy_util.AreAlliance(state, input)) {
				state_list.SetValue(index, 2, Texts.alliance);
			} else if (state.GetDiplomacy().GetSuzerainty() == input) {
				state_list.SetValue(index, 2, Texts.vassal);
			} else if (input.GetDiplomacy().GetSuzerainty() == state) {
				state_list.SetValue(index, 2, Texts.suzerainty);
			} else if (cmp.utils.diplomacy_util.AreAlly(state, input)) {
				state_list.SetValue(index, 2, Texts.ally);
			} else if (cmp.utils.diplomacy_util.AtEmbargo(state, input)) {
				state_list.SetValue(index, 2, Texts.atEmbargo);
			} else {
				state_list.SetValue(index, 2, "");
			}
			state_list.SetValue(index, 3, cmp.utils.diplomacy_util.BorderOpened(state, input) ? Texts.yes : "");
			state_list.SetValue(index, 4, cmp.utils.diplomacy_util.BorderOpened(input, state) ? Texts.yes : "");
			index++;
		}
	}

	private ScrollListComponent state_list;
	
	private static final int[] kColumnWidth = { 45, 60, 80, 100, 100};
	private static final String[] kColumnNames = { Texts.state, Texts.attitude, Texts.relationship,
			Texts.openBorder + "(" + Texts.obtain + ")", Texts.openBorder + "(" + Texts.provide + ")" };
}
