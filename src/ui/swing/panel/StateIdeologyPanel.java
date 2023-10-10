package ac.ui.swing.panel;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import ac.data.constant.Ideologies;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Ideologies.IdeologyType;
import ac.data.constant.Texts;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;
import ac.util.StringUtil;

public class StateIdeologyPanel extends TypedDataPanel<State> {

	private static final long serialVersionUID = 1L;

	public StateIdeologyPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		for (int i = 0; i < Ideologies.kMaxIdeologyTypes; ++i) {
			ideologies[i] = new TextElement(new Rectangle(25 + 150 * i, 50, 100, 18));
			ideologies[i].SetAlignment(Alignment.CENTER);
			AddVisualElement(ideologies[i]);
			
			descriptions[i] = new JLabel();
			descriptions[i].setHorizontalAlignment(SwingConstants.CENTER);
			descriptions[i].setVerticalAlignment(SwingConstants.TOP);
			descriptions[i].setBounds(15 + 150 * i, 75, 120, 200);
			add(descriptions[i]);
		}	
	}

	@Override
	public void Reset(State state) {
		for (int i = 0; i < Ideologies.kMaxIdeologyTypes; ++i) {
			Ideology ideology = state.GetPolicy().GetIdeology(IdeologyType.values()[i]);
			ideologies[i].SetText(Texts.ideologies[ideology.ordinal()]);
			if (ideology != Ideology.NONE) {
				descriptions[i].setText(StringUtil.ConvertToHTML(Texts.ideologyDescription[ideology.ordinal()]));
			} else {
				descriptions[i].setText("");
			}
		}
		
	}
	
	public void paintComponent(Graphics g)  {		
		super.paintComponent(g);
		// if (state == null) return;
		
		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(20);
//		shape_drawer.DrawLine(150, 0, 150, this.getHeight());
//		shape_drawer.DrawLine(300, 0, 150, this.getHeight());
		text_writer.SetAlignment(Alignment.CENTER).SetFontSize(16).SetBold(true);
		for (int i = 0; i < Ideologies.kMaxIdeologyTypes; ++i) {
			text_writer.DrawString(150 * i + 75, 20, Texts.ideologyTypes[i]);
		}
	}
	
	private TextElement[] ideologies = new TextElement[Ideologies.kMaxIdeologyTypes];
	private JLabel[] descriptions = new JLabel[Ideologies.kMaxIdeologyTypes];
}
