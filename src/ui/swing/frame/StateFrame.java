package ac.ui.swing.frame;

import ac.data.constant.Texts;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.TabbedFrame;
import ac.ui.swing.panel.StateCityPanel;
import ac.ui.swing.panel.StateDiplomacyPanel;
import ac.ui.swing.panel.StateIdeologyPanel;
import ac.ui.swing.panel.StateInfoPanel;
import ac.ui.swing.panel.StateMilitaryPanel;
import ac.ui.swing.panel.StatePersonPanel;
import ac.ui.swing.panel.StateResourcePanel;
import ac.ui.swing.panel.StateTechPanel;
import ac.ui.swing.panel.StateTopBanner;

public class StateFrame extends TabbedFrame<State> {
	private static final long serialVersionUID = 1L;
	public StateFrame(Components cmp, DataAccessor data) {
		super(cmp, data, new StateTopBanner(cmp, data));

		resource = new StateResourcePanel(cmp, data);
		tech = new StateTechPanel(cmp, data);
		ideology = new StateIdeologyPanel(cmp, data);
		info = new StateInfoPanel(cmp, data);
		city = new StateCityPanel(cmp, data);
		person = new StatePersonPanel(cmp, data);
		military = new StateMilitaryPanel(cmp, data);
		diplomacy = new StateDiplomacyPanel(cmp, data);
	}
	
	@Override
	protected void ShowInternal(State t) {
		setTitle(t.GetName());
		ClearTabs();
		if (t.Playable()) {
			AddTab(Texts.resource, resource);
			AddTab(Texts.city, city);
			AddTab(Texts.person, person);
			AddTab(Texts.military, military);
			AddTab(Texts.technology, tech);
			AddTab(Texts.ideology, ideology);
			AddTab(Texts.diplomat, diplomacy);
		}
		AddTab(Texts.information, info);
	}
	
	@Override
	protected boolean IsPlayerElementVisible(State t) {
		return t == data.GetPlayer().GetState();
	}

	private StateResourcePanel resource;
	private StateCityPanel city;
	private StatePersonPanel person;
	private StateMilitaryPanel military;
	private StateTechPanel tech;
	private StateIdeologyPanel ideology;
	private StateDiplomacyPanel diplomacy;
	private StateInfoPanel info;
}
