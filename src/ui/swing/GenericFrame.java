package ac.ui.swing;

import java.awt.GraphicsEnvironment;
import java.awt.Point;

import javax.swing.JFrame;

import ac.engine.data.DataAccessor;

public abstract class GenericFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	public GenericFrame(Components cmp, DataAccessor data, int width, int height) {
		this.cmp = cmp;
		this.data = data;
        setSize(width, height);
        setResizable(false);
        
        Point point = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        setLocation(point.x - width / 2, point.y - height / 2);
	}
	
	public void Repaint() {
		if (isVisible()) {
			Refresh();
			repaint();
		}
	}
	protected abstract void Refresh();
	
	protected Components cmp;
	protected DataAccessor data;
}
