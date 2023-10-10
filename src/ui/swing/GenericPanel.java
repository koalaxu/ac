package ac.ui.swing;

import java.util.ArrayList;

import javax.swing.JComponent;

import ac.engine.data.DataAccessor;
import ac.ui.swing.elements.BasePanel;
import ac.ui.swing.elements.VisualElement;

public class GenericPanel extends BasePanel {
	private static final long serialVersionUID = 1L;

	protected GenericPanel(Components cmp, DataAccessor data) {
		super();
		this.cmp = cmp;
		this.data = data;
		setBounds(0, 0, 450, 280);
	}
	
	protected void AddPlayerElement(JComponent element) {
		player_elements.add(element);
		add(element);
	}
	
	protected void AddPlayerElement(VisualElement element) {
		player_ve.add(element);
		AddVisualElement(element);
	}
	
	public void SetVisibilityForPlayerElement(boolean visible){
		for (JComponent element : player_elements) {
			element.setVisible(visible);
		}
		for (VisualElement ve : player_ve) {
			ve.SetVisibility(visible);
		}
	}
	
	protected Components cmp;
	protected DataAccessor data;
	protected ArrayList<JComponent> player_elements = new ArrayList<JComponent>();
	protected ArrayList<VisualElement> player_ve = new ArrayList<VisualElement>();
}
