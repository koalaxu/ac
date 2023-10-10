package ac.ui.swing.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import ac.data.constant.Colors;

public class ShapeDrawer extends BaseDrawer {
	public ShapeDrawer(Graphics g) {
		super(g);
	}
	public void DrawField(Rectangle rect, Color background_color) {
		SaveOldColor();
		if (background_color != null) {
			g.setColor(background_color);
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
		}
		g.setColor(Color.DARK_GRAY);
		g.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y);
		g.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height);
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height);
		g.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
		RestoreOldColor();
	}
	
	public void DrawTable(Rectangle rect, int rows, int cols) {
		SaveOldColor();
		g.setColor(Color.GRAY);
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++) {
				g.drawRect(rect.x + j * rect.width, rect.y + i * rect.height, rect.width, rect.height);
			}
		RestoreOldColor();
	}
	
	public void DrawLine(int x, int y, int width, int height) {
		SaveOldColor();
		g.setColor(Color.GRAY);
		g.drawLine(x, y, x + width, y + height);
		RestoreOldColor();
	}	
	
	public void DrawStripeTable(int x, int y, int width, int height, int rows) {
		SaveOldColor();
		for (int i = 0; i < rows; i++) {
			g.setColor((i % 2 == 0) ? Colors.LIGHTEST_GREY : Color.LIGHT_GRAY);
			g.fillRect(x, y + i * height, x + width, height);
		}
		RestoreOldColor();
	}	
	
	public void DrawArrow(int x1, int y1, int x2, int y2, double length) {
		double ratio = length / Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		DrawArrow(x1, y1, x1 + (int)Math.round((x2 - x1) * ratio), y1 + (int)Math.round((y2 - y1) * ratio));
	}
	
	public void DrawArrow(int x1, int y1, int x2, int y2) {
		g.drawLine(x1, y1, x2, y2);
		double alpha = Math.atan((double)(y1 - y2) / (x2 - x1));
		if (x1 > x2) alpha = Math.PI + alpha;
		final double beta = Math.PI / 6;
		int length = arrow_length;
		double angle1 = -alpha + Math.PI - beta;
		double angle2 = -alpha + Math.PI + beta;
		g.drawLine(x2, y2, x2 + (int)(Math.cos(angle1) * length), y2 + (int)(Math.sin(angle1) * length));
		g.drawLine(x2, y2, x2 + (int)(Math.cos(angle2) * length), y2 + (int)(Math.sin(angle2) * length));
	}
	
	public void DrawCircle(int x, int y, int r) {
		g.fillOval(x - r, y - r, 2 * r, 2 * r);
	}
	
	public void SetArrowLength(int length) {
		arrow_length = length;
	}
	
	private int arrow_length = 10;
}
