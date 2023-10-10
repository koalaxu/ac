package ac.ui.swing.panel;

import java.awt.Graphics;
import ac.data.base.Date;
import ac.data.constant.Texts;
import ac.engine.data.DataAccessor;
import ac.engine.data.Monarch;
import ac.engine.data.State;
import ac.engine.data.StateDescription;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.ScrollListComponent;
import ac.ui.swing.util.TextWriter;

public class StateInfoPanel extends TypedDataPanel<State> {
	private static final long serialVersionUID = 1L;
	public StateInfoPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		person_list = new ScrollListComponent(kColumnWidth, 20);
		person_list.SetColumnHeaders(kColumnNames);
		
		person_list.setBounds(155, 30, 300, 250);
		add(person_list);
	}
	
	public void Reset(State state) {
		this.state = state;

		person_list.Resize(state.GetHistoricalMonarchs().size());
		int index = 0;
		for (Monarch person : state.GetHistoricalMonarchs()) {
			person_list.SetValue(index, 0, person.GetName());
			person_list.SetValue(index, 1, person.GetFamilyName());
			person_list.SetValue(index, 2, person.GetGivenName());
			person_list.SetValue(index, 3, person.GetPosthumousName());
			person_list.SetValue(index, 4, Date.YearString(person.HistoricalDeathYear()));
			index++;
		}
	}

	public void paintComponent(Graphics g)  {
		super.paintComponent(g);
		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		StateDescription desc = state.GetDescription();
		text_writer.DrawString(5, 10, Texts.officialName + ": " + desc.OfficialName());
		text_writer.DrawString(5, 30, Texts.alias + ": " + desc.Alias());
		text_writer.DrawString(5, 50, Texts.nobility + ": " + desc.Nobility());
		text_writer.DrawString(5, 70, Texts.familyName + ": " + desc.FamilyName());
		text_writer.DrawString(5, 90, Texts.race + ": " + desc.Race());		
		text_writer.DrawString(155, 10, Texts.historicalMonarchs);	
	}
	
	private State state;
	
	private ScrollListComponent person_list;
	
	private static final int[] kColumnWidth = { 60, 50, 50, 50, 50 };
	private static final String[] kColumnNames = { Texts.name, Texts.familyName, Texts.givenName, Texts.posthumousName, Texts.dead };
}
