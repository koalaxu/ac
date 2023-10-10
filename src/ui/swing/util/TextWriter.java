package ac.ui.swing.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.font.LineMetrics;

import ac.util.StringUtil;

public class TextWriter extends BaseDrawer {

	public TextWriter(Graphics g) {
		super(g);
	}

	public TextWriter SetFontSize(int font_size) {
		this.font_size = font_size;
		return this;
	}
	public TextWriter SetBold(boolean bold) {
		this.bold = bold;
		return this;
	}
	public TextWriter SetAlignment(Alignment alignment) {
		align = alignment;
		return this;
	}
	public TextWriter SetColor(Color color) {
		this.color = color;
		return this;
	}
	
	public Alignment GetAlignment() {
		return align;
	}
	public void DrawString(int x, int y, String str) {
		if (str == null) return;
		SaveOldColor();
		if (font_size > 0 || bold) {
			g.setFont(new Font(g.getFont().getName(), bold ? Font.BOLD : 0, font_size));
		}
		g.setColor(color);
		DrawStringInternal(x, y, str);
		RestoreOldColor();
	}
	public void DrawRatio(int x, int y, double ratio) {
		DrawString(x, y, String.format("%.2f%%", ratio * 100));
	}
	public void DrawPercentage(int x, int y, double percentage) {
		DrawString(x, y, String.format("%.1f%%", percentage * 100));
	}
	public void DrawNumber(int x, int y, int number) {
		DrawString(x, y, String.format("%d", number));
	}	
	public void DrawLongNumber(int x, int y, long number) {
		DrawString(x, y, StringUtil.LongNumber(number));
	}
	public void DrawMultiLine(int x, int y, int line_height, int max_width, String str) {
		SaveOldColor();
		if (font_size > 0 || bold) {
			g.setFont(new Font(g.getFont().getName(), bold ? Font.BOLD : 0, font_size));
		}
		g.setColor(color);
		FontMetrics m = g.getFontMetrics();
		int begin_index = 0, end_index = 0;
		while (end_index < str.length()) {
			int width = 0;
			int next_width = 0;
			char c;
			do {
				c = str.charAt(end_index++);
				width += m.charWidth(c);
				next_width = width + (end_index < str.length() ? m.charWidth(str.charAt(end_index)) : 0);
			} while (next_width < max_width && end_index < str.length() && c != '\n');
			DrawStringInternal(x, y, str.substring(begin_index, end_index));
			begin_index = end_index;
			y += line_height;
		}
		
		RestoreOldColor();		
	}
	
	private void DrawStringInternal(int x, int y, String str) {
		LineMetrics m = g.getFontMetrics().getLineMetrics(str, g);
		float font_height = m.getAscent();
		if (align == Alignment.CENTER) {
			x -= g.getFontMetrics().stringWidth(str) / 2;
			y += font_height / 2;
		} else if (align == Alignment.LEFT) {
			y += font_height;
		} else if (align == Alignment.LEFT_MIDDLE) {
			y += font_height / 2;
		} else if (align == Alignment.RIGHT_MIDDLE) {
			x -= g.getFontMetrics().stringWidth(str);
			y += font_height / 2;
		} else if (align == Alignment.CENTER_TOP) {
			x -= g.getFontMetrics().stringWidth(str) / 2;
			y += font_height;
		} else {
			x -= g.getFontMetrics().stringWidth(str);
			y += font_height;
		}
		g.drawString(str, x, y);
	}
	
	public enum Alignment {
		LEFT,
		CENTER,
		RIGHT,
		LEFT_MIDDLE,
		RIGHT_MIDDLE,
		CENTER_TOP,
	}
	private Alignment align = Alignment.LEFT;
	private int font_size = 0;
	private boolean bold = false;
	private Color color = Color.BLACK;
}
