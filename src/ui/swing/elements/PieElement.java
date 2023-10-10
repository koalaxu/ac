package ac.ui.swing.elements;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import ac.ui.swing.util.PieDrawer;
import ac.util.GeometryUtil;

public class PieElement extends VisualElement {

	public PieElement(Rectangle area) {
		super(area);
		center = GeometryUtil.GetCenter(area);
	}
	
	public PieElement SetMax(double max) {
		this.max = max;
		return this;
	}
	
	public PieElement SetValues(double[] values) {
		this.values = values;
		ComputeAngles();
		return this;
	}
	
	public PieElement SetLabels(String[] labels) {
		this.labels = labels;
		return this;
	}
	
	public PieElement SetShowLabels(boolean show_label) {
		pie_drawer.SetWriteText(show_label);
		return this;
	}
	
	public PieElement SetFontSize(int size) {
		pie_drawer.SetFontSize(size);
		return this;
	}
	
	public PieElement SetTooltipTextFormat(String[] tooltip_text_formats) {
		this.tooltip_text_formats = tooltip_text_formats;
		return this;
	}

	@Override
	public void Draw(Graphics g) {
		pie_drawer.Reset(g);
		pie_drawer.DrawBar(area, max, values, labels);
	}
	
	@Override
	public boolean IsMouseOver(Point p) {
		return GeometryUtil.Distance(p, center) < area.width / 2;
	}
	
	@Override
	public String GetTooltipText(Point p) {
		if (tooltip_text_formats == null && labels == null) return null;
		int x = p.x - center.x;
		int y = center.y - p.y;
		if (x == 0 && y == 0) return null;
		double angle = Math.atan((double)y / x);
		if (x < 0) {
			angle = Math.PI + angle;
		} else if (x == 0) {
			angle = Math.PI * (y > 0 ?  0.5 : 1.5);
		} else if (angle < 0) {
			angle += Math.PI * 2;
		}
		for (int i = 0; i < angles.length; ++i) {
			if (angle < angles[i]) {
				if (tooltip_text_formats != null) {
					return String.format(tooltip_text_formats[i], values[i] * 100 / max);
				}
				return labels[i] + String.format("=%.1f%%", values[i] * 100 / max);
			}
		}
		return null;
	}	
	
	private void ComputeAngles() {
		angles = new double[values.length];
		double begin = 0;
		for (int i = 0; i < values.length; ++i) {
			begin += 2.0 * Math.PI * values[i] / max;
			angles[i] = begin;
		}
	}

	private PieDrawer pie_drawer = new PieDrawer(null);
	private double max = 1.0;
	private double[] values;
	private String[] labels;
	private String[] tooltip_text_formats;
	private Point center;
	private double[] angles;
}
