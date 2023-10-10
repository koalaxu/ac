package ac.ui.swing.panel;

import java.awt.Graphics;
import java.awt.Rectangle;

import ac.data.constant.Improvement;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Improvement.SpecialImprovementType;
import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.RatioBarElement;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;

public class CityBuildingPanel extends TypedDataPanel<City> {
	private static final long serialVersionUID = 1L;

	public CityBuildingPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		for (int i = 0; i < Improvement.kAuxiliaryImprovements.length; ++i) {
			buildings[i] = new RatioBarElement(new Rectangle(80, 12 + i * 30, 200, 16));
			AddVisualElement(buildings[i]);
		}
		cost = new TextElement(new Rectangle(80, 250, 80, 15));
		AddVisualElement(cost);
	}

	@Override
	public void Reset(City city) {
		this.city = city;
		for (int i = 0; i < Improvement.kAuxiliaryImprovements.length; ++i) {
			buildings[i].SetValue(city.GetImprovements().GetCount(Improvement.kAuxiliaryImprovements[i]));
			buildings[i].SetMax(cmp.utils.city_util.GetMaxImprovement(city, Improvement.kAuxiliaryImprovements[i]));
		}
		cost.SetNumber(cmp.utils.city_util.GetBuildingMaintenanceCost(city));
	}

	public void paintComponent(Graphics g)  {
		super.paintComponent(g);

		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		for (int i = 0; i < Improvement.kAuxiliaryImprovements.length; ++i) {
			ImprovementType type = Improvement.kAuxiliaryImprovements[i];
			text_writer.DrawString(10, 10 + i * 30, Texts.improvements[type.ordinal()]);
			if (type == ImprovementType.SPECIAL) {
				SpecialImprovementType special_type = city == null ? SpecialImprovementType.NONE : city.GetImprovements().GetSpecialImprovement();
				text_writer.DrawString(10, 30 + i * 30, "(" + Texts.specialImprovements[special_type.ordinal()] + ")");
			}
		}
		text_writer.DrawString(10, 250, Texts.maintenanceCost); 
	}
	
	private RatioBarElement[] buildings = new RatioBarElement[Improvement.kAuxiliaryImprovements.length];
	private TextElement cost;
	private City city;
}
