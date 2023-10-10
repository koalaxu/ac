package ac.ui.swing.elements;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import ac.data.constant.Colors;
import ac.ui.swing.util.ShapeDrawer;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;
import ac.util.StringUtil;

public class TextElement extends VisualElement {
	public TextElement(Rectangle area) {
		super(area);
		text_writer.SetFontSize(area.height * 3 / 4);
	}
	
	public TextElement SetText(String str) {
		text = str;
		return this;
	}
	
	public TextElement SetNumber(long number) {
		text = String.format("%,d", number);
		return this;
	}
	
	public TextElement SetPercentage(double rate, boolean signed, boolean colored) {
		text = StringUtil.AccuratePercentage(rate, signed);
		if (colored) {
			text_writer.SetColor(rate > 0 ? Colors.DARK_GREEN : (rate < 0 ? Colors.DARK_RED : Color.BLACK));
		}
		return this;
	}
	
	public TextElement SetFontSize(int size) {
		text_writer.SetFontSize(size);
		return this;
	}
	
	public TextElement SetBold(boolean bold) {
		text_writer.SetBold(bold);
		return this;
	}
	
	public TextElement SetTextColor(Color color) {
		text_writer.SetColor(color);
		return this;
	}
	
	public TextElement SetBackgroundColor(Color color) {
		background_color = color;
		return this;
	}
	
	public TextElement SetAlignment(Alignment alignment) {
		text_writer.SetAlignment(alignment);
		return this;
	}
	
	public TextElement SetHasFrame(boolean has_frame) {
		this.has_frame = has_frame;
		return this;
	}

	@Override
	public void Draw(Graphics g) {
		ShapeDrawer shape_drawer = new ShapeDrawer(g);
		if (background_color != null) {
			g.setColor(background_color);
		}
		if (has_frame) {
			shape_drawer.DrawField(area, background_color);
		} else if (background_color != null) {
			g.fillRect(area.x, area.y, area.width, area.height);
		}
		text_writer.Reset(g);
		switch (text_writer.GetAlignment()) {
		case CENTER:
			text_writer.DrawString(area.x + area.width / 2, area.y + area.height / 2, text);
			break;
		case CENTER_TOP:
			text_writer.DrawString(area.x + area.width / 2, area.y, text);
			break;
		case LEFT:
			text_writer.DrawString(area.x + xOffset, area.y, text);
			break;
		case RIGHT:
			text_writer.DrawString(area.x + area.width - xOffset, area.y, text);
			break;
		case LEFT_MIDDLE:
			text_writer.DrawString(area.x + xOffset, area.y + area.height / 2, text);
			break;
		case RIGHT_MIDDLE:
			text_writer.DrawString(area.x + area.width - xOffset, area.y + area.height / 2, text);
			break;
		default:
			break;
		}
		
	}
	
	private TextWriter text_writer = new TextWriter(null);
	private String text;
	private Color background_color;
	private static int xOffset = 2;
	private boolean has_frame = true;
}
