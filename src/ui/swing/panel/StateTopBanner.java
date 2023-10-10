package ac.ui.swing.panel;

import java.awt.Graphics;
import java.awt.Rectangle;

import ac.data.constant.Texts;
import ac.engine.data.Army;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;

public class StateTopBanner extends TypedDataPanel<State> {

	private static final long serialVersionUID = 1L;

	public StateTopBanner(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		population = new TextElement(new Rectangle(45, 5, 100, 20));
		population.SetFontSize(14).SetAlignment(Alignment.LEFT_MIDDLE);
		soliders = new TextElement(new Rectangle(200, 5, 80, 20));
		soliders.SetFontSize(14).SetAlignment(Alignment.LEFT_MIDDLE);
		prestige = new TextElement(new Rectangle(335, 5, 50, 20));
		prestige.SetFontSize(14).SetAlignment(Alignment.RIGHT_MIDDLE);
		stability = new TextElement(new Rectangle(450, 5, 20, 20));
		stability.SetFontSize(14).SetAlignment(Alignment.CENTER);
		
		AddVisualElement(population);
		AddVisualElement(soliders);
		AddVisualElement(prestige);
		AddVisualElement(stability);
	}

	@Override
	public void Reset(State state) {
		population.SetNumber(cmp.utils.state_util.GetTotalPopulation(state));
		long total_soldier = 0L;
		for (Army army : state.GetMilitary().GetArmies()) {
			total_soldier += army.GetTotalSoldier();
		}
		soliders.SetNumber(total_soldier);
		prestige.SetNumber(state.Get().prestige);
		stability.SetNumber(state.Get().stability);
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);
		
		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(15);
		text_writer.DrawString(5, 7, Texts.population);
		text_writer.DrawString(160, 7, Texts.soldierNumber);
		text_writer.DrawString(295, 7, Texts.prestige);
		text_writer.DrawString(400, 7, Texts.stability);
	}

	private TextElement population;
	private TextElement soliders;
	private TextElement prestige;
	private TextElement stability;
}
