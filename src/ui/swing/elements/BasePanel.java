package ac.ui.swing.elements;

import java.awt.Cursor;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;

public class BasePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	protected BasePanel() {
		this.addMouseListener(mouse_listener);
		this.addMouseMotionListener(mouse_listener);
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);
		for (VisualElement e : ve) {
			if (e.GetVisibility()) e.Draw(g);
		}
	}
	
	protected void AddVisualElement(VisualElement e) {
		ve.add(e);
	}
	
	protected void RemoveVisualElement(VisualElement e) {
		ve.remove(e);
	}
	
	protected void ClearVisualElement() {
		ve.clear();
	}
	
	public enum CursorType {
		DEFAULT,
		HAND,
		TARGET,
	}
	
	public void ChangeCursor(CursorType cursor) {
		switch(cursor) {
		case HAND:
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return;
		case TARGET:
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			return;
		default:
			break;
		}
		setCursor(Cursor.getDefaultCursor());
	}
	
	protected Components cmp;
	protected DataAccessor data;
	protected Collection<VisualElement> ve = new ArrayList<VisualElement>();
	private BaseMouseListener mouse_listener = new BaseMouseListener(this, ve);
}