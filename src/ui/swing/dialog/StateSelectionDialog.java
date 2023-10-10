package ac.ui.swing.dialog;

import java.util.function.Consumer;
import java.util.function.Supplier;

import ac.data.constant.Texts;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.GenericDialog;
import ac.ui.swing.elements.ScrollListComponent;
import ac.util.StringUtil;

public class StateSelectionDialog extends StateDialog {
	private static final long serialVersionUID = 1L;
	public StateSelectionDialog(GenericDialog parent, Components cmp, DataAccessor data, Supplier<State> state_getter, Consumer<State> func) {
		super(parent, cmp, data, state_getter, Texts.choose + Texts.city, 500, 620, false);
		this.func = func;
		
		state_list = new ScrollListComponent(kColumnWidth, 20);
		state_list.SetColumnHeaders(kColumnNames);
		
		state_list.setBounds(10, 40, 415, 550);
		add(state_list);
		
		InitDone();
		Refresh();
	}

	@Override
	protected void Refresh() {
		state_list.Resize(data.GetAllPlayableStates().Size() - 1);
		int index = 0;
		for (State state : data.GetAllPlayableStates()) {
			if (state == this.state) continue;
			state_list.SetValue(index, 0, state.GetName());
			state_list.SetValue(index, 1, StringUtil.Number(this.state.GetDiplomacy().GetAttitude(state)));
			state_list.SetCallback(index, () -> cmp.state.Show(state));
			if (cmp.utils.diplomacy_util.AtWar(state, this.state)) {
				state_list.SetValue(index, 2, Texts.atWar);
			} else if (cmp.utils.diplomacy_util.AreAlliance(state, this.state)) {
				state_list.SetValue(index, 2, Texts.alliance);
			} else if (state.GetDiplomacy().GetSuzerainty() == this.state) {
				state_list.SetValue(index, 2, Texts.vassal);
			} else if (this.state.GetDiplomacy().GetSuzerainty() == state) {
				state_list.SetValue(index, 2, Texts.suzerainty);
			} else if (cmp.utils.diplomacy_util.AreAlly(state, this.state)) {
				state_list.SetValue(index, 2, Texts.ally);
			} else if (cmp.utils.diplomacy_util.AtEmbargo(state, this.state)) {
				state_list.SetValue(index, 2, Texts.atEmbargo);
			} else {
				state_list.SetValue(index, 2, "");
			}
			state_list.SetValue(index, 3, cmp.utils.diplomacy_util.BorderOpened(state, this.state) ? Texts.yes : "");
			state_list.SetValue(index, 4, cmp.utils.diplomacy_util.BorderOpened(this.state, state) ? Texts.yes : "");
			state_list.SetCallback(index, () -> {
				func.accept(state);
				CloseDialog();
			});
			index++;
		}
	}

	@Override
	protected void Confirm() {
	}

	private Consumer<State> func;
	private ScrollListComponent state_list;
	
	private static final int[] kColumnWidth = { 45, 60, 80, 100, 100};
	private static final String[] kColumnNames = { Texts.state, Texts.attitude, Texts.relationship,
			Texts.openBorder + "(" + Texts.obtain + ")", Texts.openBorder + "(" + Texts.provide + ")" };
}
