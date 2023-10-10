package ac.ui.swing.elements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ac.data.constant.Colors;
import ac.ui.swing.util.ShapeDrawer;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;

public class ScrollListComponent extends JScrollPane {
	private static final long serialVersionUID = 1L;
	public ScrollListComponent(int[] width, int height) {
		row_height = height;
		SetColumnWidth(width);
		this.setViewportView(table_panel);
		this.setColumnHeaderView(column_panel);
		this.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner_panel);
		this.setRowHeaderView(row_panel);
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		for (int i = 0; i < width.length; ++i) {
			values.add(new ArrayList<String>());
			buttons.add(new ArrayList<JButton>());
		}
		
		table_panel.setLayout(null);
		table_panel.Resize(0);
		column_panel.Resize();
		corner_panel.Resize();
		row_panel.Resize(0);
	}
	
	private void SetColumnWidth(int[] width) {
		row_header_width = kWidthOffset + width[0];
		column_positions = new int[width.length];
		int x = kWidthOffset;
		for (int i = 1; i < width.length; ++i) {
			column_positions[i - 1] = x;
			x += width[i];
		}
		column_positions[width.length - 1] = x;
		table_width = x;
	}
	
	public void SetColumnHeaders(String[] headers) {
		column_headers = headers;
	}
	
	public void Resize(int num_rows) {
		data_lock.lock();
		for (int i = 0; i < values.size(); ++i) {
			for (int j = buttons.get(i).size() - 1; j >= num_rows; --j) {
				JButton button = buttons.get(i).get(j);
				if (button != null) button.setVisible(false);
			}
			for (int j = 0; j < num_rows - this.num_rows; ++j) {
				values.get(i).add(null);
				buttons.get(i).add(null);
			}
		}
		if (this.num_rows != num_rows) {
			row_panel.Resize(num_rows);
			table_panel.Resize(num_rows);
			this.num_rows = num_rows;
		}
		data_lock.unlock();
	}
	
	public void SetValue(int row, int column, String value) {
		if (column == 0) {
			row_panel.SetValue(row, value);
			return;
		}
		values.get(column).set(row, value);
	}
	
	public void SetCallback(int row, Runnable callback) {
		row_panel.SetCallback(row, callback);
	}
	
	public void SetButton(int row, int col, String text, Runnable callback) {
		JButton button = buttons.get(col).get(row);
		if (button == null) {
			button = new JButton();
			button.setBounds(column_positions[col - 1] + 2, row * row_height + 2, column_positions[col] - column_positions[col - 1] - 4, row_height - 4);
			buttons.get(col).set(row, button);
			table_panel.add(button);
		}
		button.setText(text);
		for (ActionListener listener : button.getActionListeners()) {
			button.removeActionListener(listener);
		}
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (callback != null) callback.run();
			}
		});
		button.setVisible(true);
	}
	
	public void RemoveButton(int row, int col) {
		JButton button = buttons.get(col).get(row);
		if (button != null) {
			button.setVisible(false);
		}
	}
	
	private class TablePanel extends BasePanel {
		private static final long serialVersionUID = 1L;
		
		public void Resize(int num_rows) {
			setPreferredSize(new Dimension(table_width, num_rows * row_height));
			revalidate();
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			data_lock.lock();
			if (values != null) {		
				ShapeDrawer shape_drawer = new ShapeDrawer(g);
				shape_drawer.DrawStripeTable(0, 0, table_width, row_height, num_rows);
				TextWriter text_writer = new TextWriter(g);
				text_writer.SetFontSize(12);
				text_writer.SetAlignment(Alignment.LEFT_MIDDLE);
				for (int i = 1; i < column_headers.length; ++i) {
					for (int j = 0; j < num_rows; ++j) {
						text_writer.DrawString(column_positions[i - 1], row_height / 2 + j * row_height, values.get(i).get(j));
					}
				}
			}
			data_lock.unlock();
		}
	}
	
	private class ColumnPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public void Resize() {
			setPreferredSize(new Dimension(table_width, row_height));
			revalidate();
		}
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, table_width, row_height);
			
			TextWriter text_writer = new TextWriter(g);
			text_writer.SetFontSize(12);
			text_writer.SetColor(Color.WHITE);
			text_writer.SetAlignment(Alignment.LEFT_MIDDLE);
			for (int i = 1; i < column_headers.length; ++i) {
				text_writer.DrawString(column_positions[i - 1], row_height / 2, column_headers[i]);
			}
		}
	}
	
	private class CornerPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public void Resize() {
			setPreferredSize(new Dimension(row_header_width, row_height));
			revalidate();
		}
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, row_header_width, row_height);
			g.setColor(Color.WHITE);
			g.drawLine(row_header_width, 0, row_header_width, row_height);
			
			TextWriter text_writer = new TextWriter(g);
			text_writer.SetFontSize(12).SetColor(Color.WHITE);
			text_writer.SetAlignment(Alignment.LEFT_MIDDLE);
			text_writer.DrawString(kWidthOffset, row_height / 2, column_headers[0]);
		}
	}
	
	private class RowPanel extends BasePanel {
		private static final long serialVersionUID = 1L;
		public void Resize(int new_num_rows) {
			setPreferredSize(new Dimension(row_header_width, row_height * new_num_rows));
			for (int i = num_rows - 1; i >= new_num_rows; --i) {
				rows.get(i).SetText("").SetUseHandCursor(false);
				rows.get(i).SetClickCallback(null);
				rows.get(i).SetVisibility(false);
			}
			int i = num_rows;
			for (int j = 0; j < new_num_rows - num_rows; ++j, ++i) {
				if (i < max_rows) {
					rows.get(i).SetVisibility(true);
					continue;
				}
				TextElement text = new TextElement(new Rectangle(kWidthOffset, i * row_height, row_header_width, row_height));
				text.SetAlignment(Alignment.LEFT_MIDDLE).SetFontSize(12).SetHasFrame(false)
				.SetBackgroundColor((i % 2 == 0) ? Colors.LIGHTEST_GREY : Color.LIGHT_GRAY);	
				rows.add(text);
				AddVisualElement(text);
				max_rows++;
			}
			revalidate();
		}
		public void SetValue(int row, String value) {
			rows.get(row).SetText(value);
		}
		public void SetCallback(int row, Runnable callback) {
			rows.get(row).SetUseHandCursor(callback != null);
			rows.get(row).SetClickCallback(callback);
		}
		public void paintComponent(Graphics g)  {
			data_lock.lock();
			super.paintComponent(g);
			if (values != null) {	
//				ShapeDrawer shape_drawer = new ShapeDrawer(g);
//				shape_drawer.DrawStripeTable(0, 0, row_header_width, row_height, num_rows);
				g.setColor(Color.BLACK);
				g.drawLine(row_header_width, 0, row_header_width, row_height * num_rows);
			}
			
			data_lock.unlock();
		}
		
		private ArrayList<TextElement> rows = new ArrayList<TextElement>();
		private int max_rows = 0;
	}
	
	private ColumnPanel column_panel = new ColumnPanel();
	private RowPanel  row_panel = new RowPanel();
	private CornerPanel corner_panel = new CornerPanel();
	private TablePanel table_panel = new TablePanel();
	private int[] column_positions;
	private String[] column_headers;
	private int row_header_width;
	private ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<JButton>> buttons = new ArrayList<ArrayList<JButton>>();
	
	private int table_width = 480;
	private int row_height = 20;
	private static final int kWidthOffset = 5;
	
	private int num_rows = 0;
	private Lock data_lock = new ReentrantLock();
	
}
