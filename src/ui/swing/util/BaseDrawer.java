package ac.ui.swing.util;

import java.awt.Color;
import java.awt.Graphics;

public class BaseDrawer {
	public BaseDrawer(Graphics g) {
		this.g = g;
	}
	
	public void Reset(Graphics g) {
		this.g = g;
	}
	
	protected void SaveOldColor() {
		old_color = g.getColor();
	}
	
	protected void RestoreOldColor() {
		g.setColor(old_color);
	}
	
	protected Graphics g;
	private Color old_color;
}
