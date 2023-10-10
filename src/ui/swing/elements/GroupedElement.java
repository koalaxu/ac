package ac.ui.swing.elements;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import ac.ui.swing.elements.BasePanel.CursorType;

public class GroupedElement extends VisualElement {

	public GroupedElement() {
		super(null);
	}

	@Override
	public void Draw(Graphics g) {
		for (VisualElement element : elements) {
			if (element.GetVisibility()) element.Draw(g);
		}
	}
	
	@Override
	public boolean IsMouseOver(Point p) {
		for (VisualElement element : elements) {
			if (element.GetVisibility() && element.IsMouseOver(p)) return true;
		}
		return false;
	}

	@Override
	protected void OnClick(Point p) {
		for (VisualElement element : elements) {
			if (element.GetVisibility() && element.IsMouseOver(p)) element.OnClick(p);
		}
	}
	
	@Override
	public String GetTooltipText(Point p) {
		for (VisualElement element : elements) {
			if (element.GetVisibility() && element.IsMouseOver(p)) return element.GetTooltipText(p);
		}
		return null;
	}
	
	@Override
	public CursorType GetCursorType(Point p) {
		for (VisualElement element : elements) {
			if (element.GetVisibility() && element.IsMouseOver(p)) return element.GetCursorType(p);
		}
		return CursorType.DEFAULT;
	}
	
	public void AddVisualElement(VisualElement element) {
		elements.add(element);
	}
	
	private ArrayList<VisualElement> elements = new ArrayList<VisualElement>();
}
