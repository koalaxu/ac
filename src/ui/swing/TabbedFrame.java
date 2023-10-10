package ac.ui.swing;

import java.util.ArrayList;

import javax.swing.JTabbedPane;

import ac.data.base.Pair;
import ac.engine.data.Data;
import ac.engine.data.DataAccessor;

public abstract class TabbedFrame<T extends Data> extends GenericFrame {
	private static final long serialVersionUID = 1L;
	public TabbedFrame(Components cmp, DataAccessor data, TypedDataPanel<T> banner) {
		super(cmp, data, 480, 400);
		setAlwaysOnTop(true);
		setLayout(null);
		setVisible(false);
		
		this.banner = banner;
		if (banner != null) {
			banner.setBounds(0, 0, 480, 30);
			add(banner);
		}
		
		tabbed_panel.setBounds(0, 30, 480, 330);
		add(tabbed_panel);
	}

	public void Show(T t) {
		typed_data = t;
		ShowInternal(t);
		ResetTypedDataPanel(banner, t);
		for (Pair<String, TypedDataPanel<T>> tab : tabs) {
			ResetTypedDataPanel(tab.second, t);
		}
		repaint();
		setVisible(true);		
	}
	
	private void ResetTypedDataPanel(TypedDataPanel<T> panel, T t) {
		if (panel == null) return;
		panel.Reset(t);
		panel.SetVisibilityForPlayerElement(IsPlayerElementVisible(t));
		panel.value = t;
	}
	
	protected abstract void ShowInternal(T t);
	
	protected abstract boolean IsPlayerElementVisible(T t);
	
	protected void ClearTabs() {
		tabbed_panel.removeAll();
		tabs.clear();
	}
	
	protected void AddTab(String name, TypedDataPanel<T> panel) {
		tabbed_panel.addTab(name, panel);
		tabs.add(new Pair<String, TypedDataPanel<T>>(name, panel));
	}
	
	@Override
	protected void Refresh() {
		if (banner != null) banner.Reset(typed_data);
		int tab_index = tabbed_panel.getSelectedIndex();
		if (tab_index >= 0 && tab_index < tabs.size() && tabs.get(tab_index).second != null) {
			tabs.get(tab_index).second.Reset(typed_data);
		}
		repaint();
	}

	protected T typed_data;
	private JTabbedPane tabbed_panel = new JTabbedPane();
	private ArrayList<Pair<String, TypedDataPanel<T>>> tabs = new ArrayList<Pair<String, TypedDataPanel<T>>>();
	private TypedDataPanel<T> banner;
}
