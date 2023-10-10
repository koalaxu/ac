package ac.ui.swing.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import ac.data.constant.Colors;
import ac.ui.swing.util.TextWriter.Alignment;

public class PieDrawer extends BaseDrawer {

	public PieDrawer(Graphics g) {
		super(g);
		text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		text_writer.SetAlignment(Alignment.CENTER);
	}
	
	public void Reset(Graphics g) {
		super.Reset(g);
		text_writer.Reset(g);
	}
		
	public void SetColors(Color[] colors) {
		this.colors = colors;
	}
	
	public void SetWriteText(boolean write_label) {
		this.write_label = write_label;
	}
	
	public void SetFontSize(int size) {
		text_writer.SetFontSize(size);
	}
	
	public void DrawBar(Rectangle rect, double max, double[] values, String[] labels) {
		SaveOldColor();
		int begin = 0;
		int radius = rect.width / 2;
		if (max <= 0) {
			max = 0;
			for (int i = 0; i < values.length; ++i)  max += values[i];
		}
		for (int i = 0; i < values.length && begin < 360; ++i) {
			g.setColor(colors[i % colors.length]);
			int arc = (int) (values[i] * 360 / max);
			if (begin + arc > 360 || i == values.length - 1) arc = 360 - begin;
			g.fillArc(rect.x, rect.y, rect.width, rect.height, begin, arc);
			double angle = Math.PI * (begin + arc / 2) / 180;
			begin += arc;
			if (values[i] > 0.001) {
				g.setColor(Color.BLACK);
				int x = rect.x + radius + (int)(Math.cos(angle) * radius * 0.75);
				int y = rect.y + radius + (int)(- Math.sin(angle) * radius * 0.75);
				text_writer.DrawString(x, y, TextFormat(max, values[i], labels != null ? labels[i] : null));
			}
		}
		RestoreOldColor();
	}
	
	private String TextFormat(double max, double value, String label) {
		if (write_label && label != null) return label;
		return String.format(max == 100 ? "%.0f%%" : "%.1f%%", value * 100 / max);
	}
	
	private Color[] colors = {  Colors.LIGHT_BLUE, Colors.LIGHT_RED, Colors.LIGHTER_YELLOW, Colors.LIGHT_GREEN  };
	private boolean write_label = true;
	private TextWriter text_writer;	

}
