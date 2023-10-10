package ac.ui.swing.elements;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import ac.ui.swing.elements.BasePanel.CursorType;
import ac.ui.swing.util.ShapeDrawer;
import ac.ui.swing.util.TextWriter;

public class TableElement extends VisualElement {

	public TableElement(Rectangle area, int rows, int cols) {
		super(new Rectangle(area.x, area.y, area.width * cols, area.height * rows));
		cell_area = area;
		this.rows = rows;
		this.cols = cols;
		elements = new TextElement[rows][cols];
	}
	
	public TextElement CreateCell(int row, int col) {
		TextElement element = new TextElement(new Rectangle(cell_area.x + col * cell_area.width + kXOffset,
				cell_area.y + row * cell_area.height + cell_area.height / 4, 0, 0));
		elements[row][col] = element;
		element.SetFontSize(cell_area.height / 2);
		return element;
	}
	
	public void CleanCell(int row, int col) {
		elements[row][col] = null;
	}
	
	public TextElement GetCell(int row, int col) {
		return elements[row][col];
	}
	
	public TextElement GetCellOrCreate(int row, int col) {
		if (elements[row][col] == null) {
			CreateCell(row, col);
		}
		return elements[row][col];
	}

	@Override
	public void Draw(Graphics g) {
		ShapeDrawer shape_drawer = new ShapeDrawer(g);
		shape_drawer.DrawTable(cell_area, rows, cols);
		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(cell_area.height / 2);
		for (int i = 0; i < rows; ++i) {
			for (int j = 0; j < cols; ++j) {
				if (elements[i][j] == null) continue;
				elements[i][j].Draw(g);
			}
		}
	}
	
	@Override
	protected void OnClick(Point p) {
		TextElement element = GetCellFromPosition(p);
		if (element != null) element.OnClick(p);
	}
	
	@Override
	public String GetTooltipText(Point p) {
		TextElement element = GetCellFromPosition(p);
		if (element != null) return element.GetTooltipText(p);
		return null;
	}
	
	@Override
	public CursorType GetCursorType(Point p) {
		TextElement element = GetCellFromPosition(p);
		if (element != null) return element.GetCursorType(p);
		return CursorType.DEFAULT;
	}
	
	private TextElement GetCellFromPosition(Point p) {
		int x = p.x - area.x;
		int y = p.y - area.y;
		int row = y / cell_area.height;
		int col = x / cell_area.width;
		if (row < 0 || col < 0 || row >= rows || col >= cols) return null;
		return elements[row][col];
	}

	private Rectangle cell_area;
	private int rows;
	private int cols;
	private TextElement[][] elements;
	private final int kXOffset = 5;
}
