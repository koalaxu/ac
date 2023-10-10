package ac.tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ac.data.ArmyData;
import ac.data.GameData;
import ac.data.constant.ConstStateData;
import ac.tools.FieldParser.FieldInfo;
import ac.tools.FieldParser.FieldLocator;
import ac.util.StringUtil;

public class ArmyEditPanel extends GenericEditPanel {
	private static final long serialVersionUID = 1L;
	protected ArmyEditPanel(GameData data) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		super(null);
		this.data = data;
		setLayout(null);
//		for (int i = 0; i < fields.length; ++i) {
//			editors.add(new ArrayList<FieldEditor<?>>());
//		}
		scroll_pane = new JScrollPane();
		scroll_pane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel() {private static final long serialVersionUID = 1L; {
			setLayout(null);
			setPreferredSize(new Dimension(60, 15));
			revalidate();
		}});
		scroll_pane.setColumnHeaderView(new JPanel() {private static final long serialVersionUID = 1L; {
			setLayout(null);
			for (int i = 0; i < fields.length; ++i) {
				JLabel label = new JLabel(field_to_names.getOrDefault(fields[i], fields[i]));
				label.setBounds(kCellWidth * i + 5, 0, kFieldWidth, 15);
				add(label);
			}
			setPreferredSize(new Dimension(kCellWidth * index_map.size() + 10, 15));
		}});
		scroll_pane.setRowHeaderView(new JPanel() {private static final long serialVersionUID = 1L; {
			setLayout(null);
			for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
				JLabel label = new JLabel(String.format("Army %d", i));
				label.setBounds(0, 20 * i + 5, 60, 15);
				add(label);
			}
			setPreferredSize(new Dimension(kFieldWidth, 20 * index_map.size() + 10));
		}});
		scroll_pane.setBounds(5, 5, 1700, DataEditorFrame.kHeight - 160);
		scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroll_pane); 
        
		drop_box = new JComboBox<String>();
        drop_box.setBounds(110, DataEditorFrame.kHeight - 150, 120, 20);
        drop_box.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				try {
					Refresh();
				} catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException
						| InvocationTargetException e1) {
					e1.printStackTrace();
				}
			}
        });
        add(drop_box); 
        
        JButton add = new JButton("Add");
        add.setBounds(250, DataEditorFrame.kHeight - 150, 100, 20);
        add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = index_map.get(drop_box.getSelectedIndex());
				for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
					ArmyData army = data.states.get(index).military.armies[i];
					if (army == null) {
						data.states.get(index).military.armies[i] = ArmyData.CreateArmy(GameData.const_data.cities.get(data.states.get(index).capital).coordinate);
						try {
							Refresh();	
						} catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException
								| NoSuchMethodException | InvocationTargetException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return;
					}
				}
			}
        });
        add(add);
        
        Init();
        revalidate();
	}
	
	private void Init() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		index_map.clear();
		drop_box.removeAllItems();
		for (int i = 0; i < data.states.size(); ++i) {
			if (!data.states.get(i).playable) continue;
			index_map.add(i);
			drop_box.addItem(GetStateName(i));
		}
		Refresh();
	}
	
	private void Refresh() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		ClearFieldEditors();
		int index = index_map.get(drop_box.getSelectedIndex());
		
		scroll_pane.setViewportView(new JPanel() {private static final long serialVersionUID = 1L; {
			setLayout(null);
			for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
				ArmyData army = data.states.get(index).military.armies[i];
				if (army == null) continue;
				for (int j = 0; j < fields.length; ++j) {
					FieldInfo info = FieldParser.Parse(fields[j]);
					FieldLocator locator = FieldParser.LocateField(army, info);
					FieldEditor<?> editor = DataEditorFrame.CreateFieldEditor(locator, data, index);
					//editors.get(i).add(editor);
					AddFieldEditor(editor, kCellWidth * j + 5, 20 * i + 5, kFieldWidth, 18, this);
				}
			}
			setPreferredSize(new Dimension(kCellWidth * fields.length + 10, 20 * ConstStateData.kMaxArmies + 10));
			revalidate();
		}});
	}
	
	private String GetStateName(int index) {
		return StringUtil.IfNull(GameData.const_data.states.get(index).alias, GameData.const_data.states.get(index).name);
	}
	
	private int kFieldWidth = 80;
	private int kCellWidth = kFieldWidth + 5;
	
	private GameData data;
	private ArrayList<Integer> index_map = new ArrayList<Integer>();
	private JScrollPane scroll_pane;
	private JComboBox<String> drop_box;
	private static String[] fields = { "typed_soldier_quantities[0][0]", "typed_soldier_quantities[0][1]", "typed_soldier_quantities[0][2]",
			"typed_soldier_quantities[1][0]", "typed_soldier_quantities[1][1]", "typed_soldier_quantities[1][2]",
			"typed_soldier_quantities[2][0]", "typed_soldier_quantities[2][1]", "typed_soldier_quantities[2][2]",
			"typed_soldier_quantities[3][0]", "typed_soldier_quantities[3][1]", "typed_soldier_quantities[3][2]",
			"training", "morale", "base_city" };
	private static HashMap<String, String> field_to_names = new HashMap<String, String>() {private static final long serialVersionUID = 1L; {
		put("typed_soldier_quantities[0][0]", "Melee Con"); put("typed_soldier_quantities[0][1]", "Melee Fub"); put("typed_soldier_quantities[0][2]", "Melee Recr");
		put("typed_soldier_quantities[1][0]", "Arch Con"); put("typed_soldier_quantities[1][1]", "Arch Fub"); put("typed_soldier_quantities[1][2]", "Arch Recr");
		put("typed_soldier_quantities[2][0]", "Mount Con"); put("typed_soldier_quantities[2][1]", "Mount Fub"); put("typed_soldier_quantities[2][2]", "Mount Recr");
		put("typed_soldier_quantities[3][0]", "Siege Con"); put("typed_soldier_quantities[3][1]", "Siege Fub"); put("typed_soldier_quantities[3][2]", "Siege Recr");
	}};
	// //private ArrayList<ArrayList<FieldEditor<?>>> editors = new ArrayList<ArrayList<FieldEditor<?>>>();
}
