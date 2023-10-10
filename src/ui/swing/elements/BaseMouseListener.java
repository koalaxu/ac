package ac.ui.swing.elements;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;

import ac.ui.swing.elements.BasePanel.CursorType;

public class BaseMouseListener implements MouseListener, MouseMotionListener {
	public BaseMouseListener(BasePanel component, Collection<VisualElement> ves) {
		this.component = component;
		this.ves = ves;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		for (VisualElement ve : ves) {
			if (ve.GetVisibility() && ve.IsMouseOver(e.getPoint())) {
				ve.OnClick(e.getPoint());
				return;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		for (VisualElement ve : ves) {
			if (ve.GetVisibility() && ve.IsMouseOver(e.getPoint())) {
				component.setToolTipText(ve.GetTooltipText(e.getPoint()));
				component.ChangeCursor(ve.GetCursorType(e.getPoint()));
				ve.OnHover(e.getPoint());
				return;
			}
		}
		component.ChangeCursor(CursorType.DEFAULT);
		component.setToolTipText(null);
	}

	private BasePanel component;
	private Collection<VisualElement> ves = new ArrayList<VisualElement>();
}
