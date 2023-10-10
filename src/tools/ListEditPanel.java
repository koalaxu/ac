package ac.tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ac.data.GameData;
import ac.data.StateData.DiplomacyData.StateRelationship;
import ac.tools.FieldParser.FieldInfo;
import ac.tools.FieldParser.FieldLocator;

class ListEditPanel<T> extends  GenericEditPanel {
		private static final long serialVersionUID = 1L;
		protected ListEditPanel(String[] fields, ArrayList<T> list, Function<Integer, String> row_header_desc,
				Function<Integer, String> name_func, HashMap<String, String> field_to_names,
				GameData data, Class<T> clazz, Runnable callback) throws
		NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
			super(callback);
			this.fields = fields;
			this.list = list;
			this.row_header_desc = row_header_desc;
			this.name_func = name_func;
			this.data = data;
			setLayout(null);
			scroll_pane = new JScrollPane();
			scroll_pane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel() {private static final long serialVersionUID = 1L; {
				setLayout(null);
				JLabel label = new JLabel("Name");
				label.setBounds(0, 0, kFieldWidth, 15);
				label.addMouseListener(new RankListener(-1));
				add(label);
				setPreferredSize(new Dimension(kFieldWidth, 15));
				revalidate();
			}});
			scroll_pane.setColumnHeaderView(new JPanel() {private static final long serialVersionUID = 1L; {
				setLayout(null);
				for (int i = 0; i < fields.length; ++i) {
					JLabel label = new JLabel(field_to_names.getOrDefault(fields[i], fields[i]));
					label.setToolTipText(label.getText());
					label.setBounds(kCellWidth * i + 5, 0, kFieldWidth, 15);
					label.addMouseListener(new RankListener(i));
					add(label);
				}
				setPreferredSize(new Dimension(kCellWidth * fields.length + 10, 15));
				revalidate();
			}});
			Reload();
			scroll_pane.setBounds(5, 5, 1700, DataEditorFrame.kHeight - 160);
			scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scroll_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			add(scroll_pane);
			
			if (clazz != null) {
		        JButton add = new JButton("Add");
		        add.setBounds(110, DataEditorFrame.kHeight - 150, 100, 20);
		        add.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							list.add(clazz.getConstructor().newInstance());
							Reload();
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException |
								SecurityException | NoSuchFieldException | ClassNotFoundException e1) {
							e1.printStackTrace();
						}
					}
		        });
		        add(add);
			}
		}
		
		private class RankListener implements MouseListener {
			protected RankListener(int index) {
				this.index = index;
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					UpdateList(index);
				} catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException
						| NoSuchMethodException | InvocationTargetException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
			private int index;
		}
		
		protected class ColumnComparator implements Comparator<Integer> {
			public ColumnComparator(int column, int prev_column) {
				index = column;
				prev_index = prev_column;
			}
			@Override
			public int compare(Integer o1, Integer o2) {
				int cmp = compare(o1, o2, index);
				if (cmp == 0 && prev_index >= 0) {
					return compare(o1, o2, prev_index);
				}
				return cmp;
			}
			
			private int compare(Integer o1, Integer o2, int col_index) {
				FieldEditor<?> editor1 = editors.get(col_index).get(o1);
				FieldEditor<?> editor2 = editors.get(col_index).get(o2);
				int cmp = 0;
				try {
					cmp = (int) (editor1.ConvertToNumber() - editor2.ConvertToNumber());
				} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				if (cmp == 0) {
					try {
						cmp = editor1.Convert().toString().compareTo(editor2.Convert().toString());
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
				}
				return cmp;
			}
			
			private int index;
			private int prev_index;
		}
		
		private void Reload() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
			ClearFieldEditors();
			editors.clear();
			scroll_pane.setViewportView(new JPanel() {private static final long serialVersionUID = 1L; {
				setLayout(null);
				for (int i = 0; i < fields.length; ++i) {
					editors.add(new ArrayList<FieldEditor<?>>());
					for (int j = 0; j < list.size(); ++j) {
						FieldInfo info = FieldParser.Parse(fields[i]);
						FieldLocator locator = FieldParser.LocateField(list.get(j), info);
						FieldEditor<?> editor = DataEditorFrame.CreateFieldEditor(locator, data, j);
						editors.get(i).add(editor);
						AddFieldEditor(editor, kCellWidth * i + 5, 20 * j + 5, kFieldWidth, 18, this);
					}
				}
				setPreferredSize(new Dimension(kCellWidth * fields.length + 10, 20 * list.size() + 10));
				revalidate();
			}});
			ascending = false;
			UpdateList(-1);
			// Update diplomacy matrix
        	for (int i = 0; i < data.states.size(); ++i) {
        		while (data.states.get(i).diplomacy.states.size() < data.states.size()) {
        			data.states.get(i).diplomacy.states.add( new StateRelationship());
        		}
        	}
		}
		
		protected void UpdateList(int rank_by) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
			ArrayList<Integer> indices = new ArrayList<Integer>();
			for (int i = 0; i < list.size(); ++i) {
				indices.add(i);
			}
			ascending = (rank_by != this.rank_by) || !ascending;
			this.previous_rank_by = this.rank_by;
			this.rank_by = rank_by;
			if (this.rank_by == -1) {
				indices.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());
			} else {
				Comparator<Integer> cmp = new ColumnComparator(this.rank_by, this.previous_rank_by);
				indices.sort(ascending ? cmp : cmp.reversed());
			}

			for (int i = 0; i < list.size(); ++i) {
				int index = indices.get(i);
				for (int j = 0; j < fields.length; ++j) {
					editors.get(j).get(index).GetComponent().setBounds(kCellWidth * j + 5, 20 * i + 5, kFieldWidth, 18);
				}
			}
			scroll_pane.setRowHeaderView(new JPanel() {private static final long serialVersionUID = 1L; {
				setLayout(null);
				for (int i = 0; i < list.size(); ++i) {
					int index = indices.get(i);
					JLabel label = new JLabel(name_func.apply(index));
					label.setBounds(0, 20 * i + 5, 60, 15);
					if (row_header_desc != null) {
						label.setToolTipText(row_header_desc.apply(index));
					} else {
						label.setToolTipText(null);
					}
					add(label);
				}
				setPreferredSize(new Dimension(60, 20 * list.size() + 10));
				revalidate();
			}});
		}
		
		private int kFieldWidth = 80;
		private int kCellWidth = kFieldWidth + 5;
		
		private GameData data;
		private String[] fields;
		private ArrayList<T> list;
		private Function<Integer, String> row_header_desc;
		private Function<Integer, String> name_func;
		private JScrollPane scroll_pane;
		private int rank_by = -1;
		private int previous_rank_by = -1;
		private boolean ascending = true;
		private ArrayList<ArrayList<FieldEditor<?>>> editors = new ArrayList<ArrayList<FieldEditor<?>>>();
	}