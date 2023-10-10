package ac.ui.swing.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import ac.ui.swing.util.ShapeDrawer;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;
import ac.util.GeometryUtil;

public class BadgeElement extends VisualElement {

	public BadgeElement(Rectangle area, int font_size) {
		super(area);
		center = GeometryUtil.GetCenter(area);
		radius = Math.min(area.height, area.width) / 2;
		this.font_size = font_size;
	}
	
	public void Resize(Rectangle area) {
		center = GeometryUtil.GetCenter(area);
		radius = Math.min(area.height, area.width) / 2;
	}
	
	public BadgeElement SetText(String text) {
		this.text = text;
		return this;
	}
	
	public BadgeElement SetForegroundColor(Color color) {
		foreground = color;
		return this;
	}
	
	public BadgeElement SetBackgroundColor(Color color) {
		background = color;
		return this;
	}

	@Override
	public void Draw(Graphics g) {
		ShapeDrawer shape_drawer = new ShapeDrawer(g);
		TextWriter text_writer = new TextWriter(g);
		g.setColor(Color.LIGHT_GRAY);
		shape_drawer.DrawCircle(center.x, center.y, radius);
		g.setColor(background);
		shape_drawer.DrawCircle(center.x, center.y, radius);
		text_writer.SetAlignment(Alignment.CENTER);
		text_writer.SetFontSize(font_size);
		text_writer.SetColor(foreground);
		text_writer.DrawString(center.x, center.y, text);
	}
	
	@Override
	public boolean IsMouseOver(Point p) {
		return GeometryUtil.Distance(p, center) <= radius;
	}

	private Point center;
	private int radius;
	private int font_size;
	private String text;
	private Color foreground = Color.BLACK;
	private Color background = Color.LIGHT_GRAY;
}
