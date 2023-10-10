package ac.ui.swing.frame;

import ac.data.constant.Texts;
import ac.engine.data.Data;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.TabbedFrame;
import ac.ui.swing.panel.MarketPanel;
import ac.ui.swing.panel.MessagePanel;
import ac.ui.swing.panel.PersonPanel;
import ac.ui.swing.panel.StatePanel;

public class OverviewFrame extends TabbedFrame<Data> {
	private static final long serialVersionUID = 1L;
	public OverviewFrame(Components cmp, DataAccessor data) {
		super(cmp, data, null);
		
		AddTab(Texts.state, new StatePanel(cmp, data));
		AddTab(Texts.person, new PersonPanel(cmp, data));
		AddTab(Texts.market, new MarketPanel(cmp, data));
		AddTab(Texts.message, new MessagePanel(cmp, data));
	}

	@Override
	protected void ShowInternal(Data t) {
		setTitle(Texts.overivew);
	}

	@Override
	protected boolean IsPlayerElementVisible(Data t) {
		return true;
	}
}
