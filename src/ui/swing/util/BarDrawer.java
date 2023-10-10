package ac.ui.swing.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import ac.data.constant.Colors;
import ac.ui.swing.util.TextWriter.Alignment;

public class BarDrawer extends BaseDrawer {
	public BarDrawer(Graphics g) {
		super(g);
		text_writer = new TextWriter(g);
		text_writer.SetAlignment(Alignment.CENTER);
	}
	
	public void Reset(Graphics g) {
		super.Reset(g);
		text_writer.Reset(g);
	}
		
	public void SetColors(Color[] colors) {
		this.colors = colors;
	}
	
	public void ResetColors() {
		colors = new Color[]{  Colors.LIGHT_BLUE  };
	}
	
	public void SetUseSpectrum(boolean use_spectrum) {
		this.use_spectrum = use_spectrum;
	}
	
	public void SetWriteText(boolean write_text) {
		this.write_text = write_text;
	}
	
	public void SetShowRemaining(boolean show_remaining) {
		this.show_remaining = show_remaining;
	}
		
	public void DrawBar(Rectangle rect, int max, int... values) {
		SaveOldColor();
		int x = rect.x;
		for (int i = 0; i < values.length; ++i) {
			int value = values[i];
			if (use_spectrum) {
				g.setColor(colors[Math.min(colors.length - 1, Math.max(0, (value - 1) * colors.length / max ))]);
			} else {
				g.setColor(colors[i % colors.length]);
			}
			int actual_width = (int) (rect.width * value / max);
			g.fillRect(x, rect.y, actual_width, rect.height);
			if (write_text && value > 0) {
				g.setColor(Color.BLACK);
				text_writer.SetFontSize(rect.height * 3 / 4);
				text_writer.DrawString(x + actual_width / 2, rect.y + rect.height / 2, String.valueOf(values[i]));
			}
			x += actual_width;
		}
		g.setColor(Color.BLACK);
		g.drawRect(rect.x, rect.y, show_remaining ? rect.width : x - rect.x, rect.height);
		RestoreOldColor();
	}
	
	public void DrawRatio(Rectangle rect, long num, long max) {
		DrawRatio(rect, num, max, "");
	}
	
	public void DrawRatio(Rectangle rect, long num, long max, String suffix_of_num ) {
		//num = Math.min(num, max);
		DrawBarWithText(rect, (float)num / max, num + suffix_of_num + "/" + max);
	}
		
	public void DrawBarWithText(Rectangle rect, float ratio, String text) {
		SaveOldColor();
		if (use_spectrum) {
			g.setColor(colors[Math.max(0, (int) (Math.round(Math.min(1.0, ratio) * colors.length) - 1))]);
		} else {
			g.setColor(Colors.LIGHT_BLUE);
		}
		g.fillRect(rect.x, rect.y, (int) (rect.width * ratio), rect.height);
		g.setColor(Color.BLACK);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		if (write_text) {
			text_writer.SetFontSize(rect.height * 3 / 4);
			text_writer.DrawString(rect.x + rect.width / 2, rect.y + rect.height / 2, text);
		}
		RestoreOldColor();
	}
		
	private Color[] colors = {  Colors.LIGHT_BLUE  };
	private boolean write_text = true;
	private boolean show_remaining = true;
	private boolean use_spectrum = false;
	private TextWriter text_writer;
	
	public static Color[] kThresholdSpectrum = { new Color(255, 127, 127), new Color (241, 191, 63), new Color (225, 225, 127),
			new Color (191, 223, 153), new Color (127, 192, 127) };
}
