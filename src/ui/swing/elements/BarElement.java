package ac.ui.swing.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import ac.ui.swing.util.BarDrawer;

public class BarElement extends VisualElement {

	public BarElement(Rectangle area, int max, Color[] colors) {
		super(area);
		this.max = max;
		bar_drawer.SetColors(colors);
	}
	
	public BarElement(Rectangle area) {
		super(area);
	}
		
	public BarElement SetValues(int... values) {
		this.values = values;
		return this;
	}
	
	public BarElement SetMax(int max) {
		this.max = max;
		return this;
	}
	
	public BarElement SetShowRemaining(boolean show_remaining) {
		bar_drawer.SetShowRemaining(show_remaining);
		return this;
	}
	
	@Override
	public void Draw(Graphics g) {
		bar_drawer.Reset(g);
		bar_drawer.DrawBar(area, max, values);
	}

	private BarDrawer bar_drawer = new BarDrawer(null);
	private int max;
	private int[] values;
}
