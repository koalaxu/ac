package ac.ui.swing.panel;

import java.awt.Graphics;
import ac.data.base.Pair;
import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.CityDescription;
import ac.engine.data.CityNaturalInfo;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.util.TextWriter;
import ac.util.StringUtil;

public class CityInfoPanel extends TypedDataPanel<City> {
	private static final long serialVersionUID = 1L;
	public CityInfoPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
	}
	
	public void Reset(City city) {
		this.city = city;
	}

	public void paintComponent(Graphics g)  {
		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		CityDescription desc = city.GetDescription();
		CityNaturalInfo info = city.GetNaturalInfo();
		text_writer.DrawString(5, 10, Texts.hanName + ": " + desc.GetHanName());
		text_writer.DrawString(5, 30, Texts.hanCountyName + ": " + desc.GetHanCountyName());
		text_writer.DrawString(5, 50, Texts.tangName + ": " + desc.GetTangName());
		text_writer.DrawString(5, 70, Texts.tangCountyName + ": " + desc.GetTangCountyName());
		text_writer.DrawString(5, 90, Texts.currentName + ": " + desc.GetCurrentName());	
		text_writer.DrawString(5, 120, Texts.rain + ": " + StringUtil.nOf(Texts.rainLevel, info.GetRainLevel()));	
		text_writer.DrawString(5, 140, Texts.temperature + ": " + StringUtil.nOf(Texts.temperatureLevel, info.GetTemperatureLevel()));	
		
		text_writer.DrawString(245, 10, Texts.neighborCity + ":");
		int y = 30;
		for (Pair<City, Double> neighbor : desc.GetNeighbors()) {
			text_writer.DrawString(245, y, String.format("%s (%.1f/%d)", neighbor.first.GetName(), neighbor.second,
					city.GetTransportation(neighbor.first)));
			y += 20;
		}
	}
	
	private City city;
}
