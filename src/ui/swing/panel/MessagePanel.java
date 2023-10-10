package ac.ui.swing.panel;

import java.util.LinkedList;

import ac.data.base.Date;
import ac.data.base.Pair;
import ac.data.constant.Texts;
import ac.engine.data.Data;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.ScrollListComponent;

public class MessagePanel extends TypedDataPanel<Data> {
	private static final long serialVersionUID = 1L;
	public MessagePanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		message_list = new ScrollListComponent(kColumnWidth, 20);
		message_list.SetColumnHeaders(kColumnNames);
		
		message_list.setBounds(5, 5, 450, 260);
		add(message_list);
	}

	@Override
	public void Reset(Data input) {
		LinkedList<Pair<Date, String>> messages = data.GetMessages().GetCachedMessages();
		message_list.Resize(messages.size());
		int index = messages.size();
		for (Pair<Date, String> message : messages) {
			index--;
			message_list.SetValue(index, 0, message.first.ShortString());
			message_list.SetValue(index, 1, message.second);
		}
	}

	private ScrollListComponent message_list;
	
	private static final int[] kColumnWidth = { 110, 800};
	private static final String[] kColumnNames = { Texts.time, Texts.content};

}
