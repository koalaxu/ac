package ac.tools;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ac.data.GameData;
import ac.tools.FieldParser.FieldInfo;
import ac.tools.FieldParser.FieldLocator;
import ac.util.StringUtil;

public class DiplomacyEditPanel extends GenericEditPanel {
	private static final long serialVersionUID = 1L;
	protected DiplomacyEditPanel(GameData data) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		super(null);
		this.data = data;
		setLayout(null);
		for (int i = 0; i < fields.length; ++i) {
			editors.add(new ArrayList<FieldEditor<?>>());
		}
		scroll_pane = new JScrollPane();
		scroll_pane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel() {private static final long serialVersionUID = 1L; {
			setLayout(null);
			setPreferredSize(new Dimension(kFieldWidth, 15));
			revalidate();
		}});
		scroll_pane.setBounds(5, 5, 1700, DataEditorFrame.kHeight - 160);
		scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Init();
		add(scroll_pane);
		
		drop_box = new JComboBox<String>(fields);
        drop_box.setBounds(110, DataEditorFrame.kHeight - 150, 150, 20);
        drop_box.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Refresh();
			}
        });
        add(drop_box);  
        
        Refresh();
        revalidate();
	}
	
	private void Init() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		index_map.clear();
		ClearFieldEditors();
		for (int i = 0; i < data.states.size(); ++i) {
			if (!data.states.get(i).playable) continue;
			index_map.add(i);
		}
		scroll_pane.setRowHeaderView(new JPanel() {private static final long serialVersionUID = 1L; {
			setLayout(null);
			for (int i = 0; i < index_map.size(); ++i) {
				int index = index_map.get(i);
				JLabel label = new JLabel(GetStateName(index));
				label.setBounds(0, 20 * i + 5, kFieldWidth, 15);
				add(label);
			}
			setPreferredSize(new Dimension(kFieldWidth, 20 * index_map.size() + 10));
		}});
		scroll_pane.setColumnHeaderView(new JPanel() {private static final long serialVersionUID = 1L; {
			setLayout(null);
			for (int i = 0; i < index_map.size(); ++i) {
				int index = index_map.get(i);
				JLabel label = new JLabel(GetStateName(index));
				label.setBounds(kCellWidth * i + 5, 0, kFieldWidth, 15);
				add(label);
			}
			setPreferredSize(new Dimension(kCellWidth * index_map.size() + 10, 15));
		}});
		scroll_pane.setViewportView(new JPanel() {private static final long serialVersionUID = 1L; {
			setLayout(null);
			for (int i = 0; i < index_map.size(); ++i) {
				int index_i = index_map.get(i);
				for (int j = 0; j < index_map.size(); ++j) {
					int index_j = index_map.get(j);
					for (int k = 0; k < fields.length; ++k) {
						FieldInfo info = FieldParser.Parse(fields[k]);
						//FieldLocator locator = FieldParser.LocateField(data.states.get(index_i).diplomacy.states[index_j], info);
						FieldLocator locator = FieldParser.LocateField(data.states.get(index_i).diplomacy.states.get(index_j), info);
						FieldEditor<?> editor = DataEditorFrame.CreateFieldEditor(locator, data, -1);
						editors.get(k).add(editor);
						AddFieldEditor(editor, kCellWidth * i + 5, 20 * j + 5, kFieldWidth, 18, this);
					}
				}
			}
			setPreferredSize(new Dimension(kCellWidth * index_map.size() + 10, 20 * index_map.size() + 10));
			revalidate();
		}});
	}
	
	private void Refresh() {
		for (int i = 0; i < fields.length; ++i) {
			for (FieldEditor<?> editor : editors.get(i)) {
				editor.GetComponent().setVisible(i == drop_box.getSelectedIndex());
			}
		}
	}
	
	private String GetStateName(int index) {
		return StringUtil.IfNull(GameData.const_data.states.get(index).alias, GameData.const_data.states.get(index).name);
	}
	
	private int kFieldWidth = 60;
	private int kCellWidth = kFieldWidth + 5;
	
	private GameData data;
	private ArrayList<Integer> index_map = new ArrayList<Integer>();
	private JScrollPane scroll_pane;
	private JComboBox<String> drop_box;
	private static String[] fields = { "attitude", "ally", "open_border", "alliance" };
	private ArrayList<ArrayList<FieldEditor<?>>> editors = new ArrayList<ArrayList<FieldEditor<?>>>();
}
