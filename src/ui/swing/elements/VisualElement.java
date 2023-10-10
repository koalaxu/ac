package ac.ui.swing.elements;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import ac.ui.swing.elements.BasePanel.CursorType;
import ac.util.GeometryUtil;

public abstract class VisualElement {
	public VisualElement(Rectangle area) {
		this.area = area;
	}
	
	public void Resize(Rectangle area) {
		this.area = area;
	}
	public abstract void Draw(Graphics g);
	
	public boolean IsMouseOver(Point p) {
		return area != null && GeometryUtil.CheckRectangle(p, area);
	}
	
	public void SetTooltipText(String text) {
		tooltip_text = text;
	}
	public String GetTooltipText(Point p) {
		return tooltip_text;
	}
	
	public CursorType GetCursorType(Point p) {
		return cursor_type;
	}
	
	public void SetUseHandCursor(boolean hand) {
		cursor_type = hand ? CursorType.HAND : CursorType.DEFAULT;
	}
	
	public void SetUseTargetCursor(boolean target) {
		cursor_type = target ? CursorType.TARGET : CursorType.DEFAULT;
	}
	
	protected void OnClick(Point p) {
		if (click_callback != null) click_callback.run();
	}
	
	protected void OnHover(Point p) {
		if (hover_callback != null) hover_callback.run();
	}
	
	public void SetClickCallback(Runnable callback) {
		click_callback = callback;
	}
	
	public void SetVisibility(boolean visible) {
		this.visible = visible;
	}
	
	public boolean GetVisibility() {
		return visible;
	}
	
	protected Rectangle area;
	private String tooltip_text;
	private CursorType cursor_type = CursorType.DEFAULT;
	private Runnable click_callback;
	private Runnable hover_callback;
	private boolean visible = true;
}
