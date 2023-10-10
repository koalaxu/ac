package ac.ui.swing.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import ac.data.CityData.Profession;
import ac.data.constant.Colors;
import ac.data.constant.Improvement;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.CityImprovements;
import ac.engine.data.CityNaturalInfo;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.BarElement;
import ac.ui.swing.elements.RatioBarElement;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.util.StringUtil;

public class CityAgriculturePanel extends TypedDataPanel<City> {
	private static final long serialVersionUID = 1L;
	public CityAgriculturePanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		farms = new BarElement(new Rectangle(45, 10, 400, 15), 70, farm_colors);
		farms.SetShowRemaining(false);
		oxed_farms = new RatioBarElement(new Rectangle(45, 35, 400, 15));
		ironed_farms = new RatioBarElement(new Rectangle(45, 60, 400, 15));
		climate = new TextElement(new Rectangle(45, 100, 290, 15));
		base_yield = new TextElement(new Rectangle(200, 125, 135, 15));
		peasant = new BarElement(rectPeasant, data.GetParam().population_per_farm * 3, peasant_colors);
		tech_multiplier = new TextElement(new Rectangle(85, 175, 115, 15));
		governor_multiplier = new TextElement(new Rectangle(310, 175, 115, 15));
		labor_multiplier = new TextElement(new Rectangle(85, 200, 115, 15));
		disaster_multiplier = new TextElement(new Rectangle(310, 200, 115, 15));
		estimated_yield = new TextElement(new Rectangle(85, 225, 115, 15));
		estimated_income = new TextElement(new Rectangle(310, 225, 115, 15));
		AddVisualElement(farms);
		AddVisualElement(oxed_farms);
		AddVisualElement(ironed_farms);
		AddVisualElement(climate);
		AddVisualElement(base_yield);
		AddVisualElement(peasant);
		AddVisualElement(tech_multiplier);
		AddVisualElement(governor_multiplier);
		AddVisualElement(labor_multiplier);
		AddVisualElement(disaster_multiplier);
		AddVisualElement(estimated_yield);
		AddVisualElement(estimated_income);
		oxed_farms.SetTooltipText(Texts.yieldBonus + ": " + StringUtil.Percentage(data.GetParam().ox_bonus));
		ironed_farms.SetTooltipText(Texts.yieldBonus + ": " + StringUtil.Percentage(data.GetParam().iron_bonus));
		
		
		auto_appease.setBounds(5, 250, 100, 20);
		auto_appease.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				data.GetPlayer().SetAutoAppease(GetData(), data.GetPlayer().HasAutoAppease(GetData()));
				cmp.city.Repaint();
			}
		});
		AddPlayerElement(auto_appease);
	}
	
	public void Reset(City city) {
		CityImprovements impr = city.GetImprovements();
		int farm = impr.GetCount(ImprovementType.FARM);
		int aqeducted_farm = impr.GetCount(ImprovementType.AQEDUCTED_FARM);
		int irrigated_farm = impr.GetCount(ImprovementType.IRRIGATED_FARM);
		int total_farm = farm + aqeducted_farm + irrigated_farm;
		int oxed_farm = impr.GetCount(ImprovementType.OXED_FARM);
		int ironed_farm = impr.GetCount(ImprovementType.IRONED_FARM);
		farms.SetValues(farm, aqeducted_farm, irrigated_farm)
			.SetTooltipText(GetFarmTooltipText(farm, aqeducted_farm, irrigated_farm));
		oxed_farms.Resize(new Rectangle(45, 35, 400 * total_farm / 70, 15));
		oxed_farms.SetValue(oxed_farm).SetMax(total_farm);
		ironed_farms.Resize(new Rectangle(45, 60, 400 * total_farm / 70, 15));
		ironed_farms.SetValue(ironed_farm).SetMax(total_farm);
		
		CityNaturalInfo natural_info = city.GetNaturalInfo();
		int rain_level = natural_info.GetRainLevelOrDefault();
		int heat_level = natural_info.GetTemperatureLevelOrDefault();
		boolean estimated = !natural_info.HasFinalState();
		climate.SetText(StringUtil.nOf(Texts.rainLevel, rain_level) + " / " + StringUtil.nOf(Texts.temperatureLevel, heat_level) +
				(estimated ? " (" + Texts.estimated + ") ": "") + (natural_info.GetFloodSeverity() > 0 ? "  [" + Texts.flood + "]" : "") +
				(natural_info.GetLocustSeverity() > 0 ? "  [" + Texts.locust + "]" : ""));
		
		String base_yield_str = "";
		for (int i = 0; i < 3; ++i) {
			if (i > 0) base_yield_str += " / ";
			base_yield_str += StringUtil.Decimal3Digit(
					cmp.utils.prod_util.GetNatureBase(Improvement.kAgricultureImprovements[i], rain_level, heat_level));
		}
		base_yield.SetText(base_yield_str);
		
		long total_peasant = cmp.utils.city_util.GetProfessionalPopulation(city, Profession.PEASANT);
		int available_peasant = (int)(total_peasant / total_farm);
		int pop_per_farm = data.GetParam().population_per_farm;
		peasant.SetValues(Math.min(pop_per_farm, available_peasant), Math.min(pop_per_farm, available_peasant - pop_per_farm),
				Math.min(pop_per_farm, available_peasant - pop_per_farm * 2));
		peasant.SetTooltipText(GetPeasantTooltipText(available_peasant, pop_per_farm));
		
		tech_multiplier.SetText(StringUtil.Percentage(cmp.utils.prod_util.GetAgricultureTechMultiplier(city.GetOwner())))
			.SetTooltipText(cmp.utils.prod_util.GetAgricultureTechMultiplierTooltip(city.GetOwner()));
		governor_multiplier.SetText(StringUtil.Percentage(cmp.utils.prod_util.GetAgricultureGovernorMultiplier(city)));
		labor_multiplier.SetText(StringUtil.Percentage(1.0 - cmp.utils.prod_util.GetAgricultureLaborMultiplier(city)));
		disaster_multiplier.SetText(StringUtil.Percentage(1.0 - cmp.utils.prod_util.GetAgricultureDisasterMultiplier(city)));
		long food = cmp.utils.prod_util.GetFoodYield(city, total_peasant, rain_level, heat_level);
		estimated_yield.SetNumber(food);
		estimated_income.SetNumber((long) (food * city.GetOwner().GetEconomic().GetFoodTax() * cmp.utils.prod_util.GetTaxEfficiency(city)));
		
		if (city.GetOwner() == data.GetPlayer().GetState()) {
			auto_appease.setText((data.GetPlayer().HasAutoAppease(city) ? Texts.yes : Texts.no) + " " + Texts.auto + Texts.appeaseSubjects + " ");
		}
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);

		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		text_writer.DrawString(5, 10, Texts.farm);
		text_writer.DrawString(5, 35, Texts.oxedFarm);
		text_writer.DrawString(5, 60, Texts.ironedFarm);
		
		text_writer.DrawString(5, 100, Texts.climate);
		text_writer.DrawString(5, 125, String.format("%s/%s (%s/%s/%s)", Texts.baseAgricultureYield, Texts.farm,
				Texts.normalFarm, Texts.aqeductedFarm, Texts.irrigatedFarm));
		text_writer.DrawString(5, 150, String.format("%s/%s", Texts.professionPeasant, Texts.farm));
		text_writer.DrawString(5, 175, Texts.technologyBoost);
		text_writer.DrawString(230, 175, Texts.governorBoost);
		text_writer.DrawString(5, 200, Texts.laborPenalty);
		text_writer.DrawString(230, 200, Texts.disasterPenalty);
		text_writer.DrawString(5, 225, Texts.estimatedYield);
		text_writer.DrawString(230, 225, Texts.estimatedTaxIncome);
		
	}
	
	private String GetFarmTooltipText(int farm, int aqeducted_farm, int irrigated_farm) {
		ArrayList<String> tips = new ArrayList<String>();
		if (farm - aqeducted_farm > 0) tips.add(Texts.normalFarm);
		if (aqeducted_farm > 0) tips.add(Texts.aqeductedFarm);
		if (irrigated_farm > 0) tips.add(Texts.irrigatedFarm);
		return Texts.farmIrrigation + " ( " + String.join(" / ", tips) + " ) ";
	}
	
	private String GetPeasantTooltipText(int available_peasant, int pop_per_farm) {
		String result = "<html>";
		for (int i = 0; i < 3; ++i) {
			result += String.format(Texts.tooltipPeasantProductivity, (i + 1) * pop_per_farm, data.GetParam().peasant_productivity[i]) + "<br>";
			if (available_peasant < (i + 1) * pop_per_farm) break;
		}
		return result + "</html>";
	}
	
	private BarElement farms;
	private RatioBarElement oxed_farms;
	private RatioBarElement ironed_farms;
	private TextElement climate;
	private TextElement base_yield;
	private TextElement tech_multiplier;
	private TextElement governor_multiplier;
	private TextElement disaster_multiplier;
	private TextElement labor_multiplier;
	private TextElement estimated_yield;
	private TextElement estimated_income;
	private Rectangle rectPeasant = new Rectangle(85, 150, 360, 15);
	private BarElement peasant;
	
	private static final Color[] farm_colors = { Color.ORANGE, Colors.DARK_YELLOW, new Color (127, 192, 127)};
	private static final Color[] peasant_colors = { new Color (127, 192, 127), Colors.DARK_YELLOW, Color.ORANGE };
	
	// PlayerElement
	private JButton auto_appease = new JButton(Texts.auto + Texts.appeaseSubjects);
}
