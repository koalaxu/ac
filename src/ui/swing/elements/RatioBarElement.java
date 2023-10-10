package ac.ui.swing.elements;

import java.awt.Graphics;
import java.awt.Rectangle;

import ac.ui.swing.util.BarDrawer;

public class RatioBarElement extends VisualElement {

	public RatioBarElement(Rectangle area) {
		super(area);
	}
	
	public RatioBarElement SetValue(long value) {
		this.value = value;
		return this;
	}
	
	public RatioBarElement SetMax(long max) {
		this.max = max;
		return this;
	}
	
	public RatioBarElement SetAddition(Integer add) {
		addition = add;
		return this;
	}
	
	public RatioBarElement SetUseSpectrum(boolean use_spectrum) {
		if (use_spectrum) {
			bar_drawer.SetColors(BarDrawer.kThresholdSpectrum);
		} else {
			bar_drawer.ResetColors();
		}
		bar_drawer.SetUseSpectrum(use_spectrum);
		return this;
	}

	@Override
	public void Draw(Graphics g) {
		if (area == null) return;
		bar_drawer.Reset(g);
		if (addition == null) {
			bar_drawer.DrawRatio(area, value, max);
		} else {
			bar_drawer.DrawRatio(area, value, max, " + " + addition);
		}
	}

	private BarDrawer bar_drawer = new BarDrawer(null);
	private long value;
	private long max;
	private Integer addition;
}
