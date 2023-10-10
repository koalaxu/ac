package ac.ui.swing.panel;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

import ac.data.CityData.Profession;
import ac.data.constant.Texts;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.City;
import ac.engine.data.CityPopulation;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.dialog.NumberDialog;
import ac.ui.swing.dialog.ProfessionAllocateDialog;
import ac.ui.swing.elements.PieElement;
import ac.ui.swing.elements.RatioBarElement;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;
import ac.util.StringUtil;

public class CityOverviewPanel extends TypedDataPanel<City> {

	private static final long serialVersionUID = 1L;
	public CityOverviewPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		pop = new TextElement(new Rectangle(60, 10, 100, 18));
		pop_growth = new TextElement(new Rectangle(170, 10, 80, 18));
		food = new TextElement(new Rectangle(60, 35, 100, 18));
		food_consumption = new TextElement(new Rectangle(170, 35, 80, 18));
		labor_current = new TextElement(new Rectangle(60, 60, 80, 18));
		labor = new TextElement(new Rectangle(150, 60, 100, 18));
		military_service_current = new TextElement(new Rectangle(60, 85, 80, 18));
		military_service = new TextElement(new Rectangle(150, 85, 100, 18));
		happiness = new RatioBarElement(new Rectangle(375, 10, 80, 15));
		happiness.SetMax(100).SetUseSpectrum(true);
		stability_cost = new TextElement(new Rectangle(375, 35, 80, 18));
		stability_cost.SetAlignment(Alignment.RIGHT);
		pop_decrease = new TextElement(new Rectangle(375, 60, 80, 18));
		tax_efficiency = new TextElement(new Rectangle(375, 85, 80, 18));
		profession = new PieElement(new Rectangle(80, 130, 150, 150));
		profession.SetMax(1.0).SetLabels(Texts.professions).SetFontSize(11);
		races = new PieElement(new Rectangle(305, 130, 150, 150));
		races.SetMax(1.0).SetFontSize(11);
		AddVisualElement(pop);
		AddVisualElement(pop_growth);
		AddVisualElement(food);
		AddVisualElement(food_consumption);
		AddVisualElement(labor);
		AddVisualElement(labor_current);
		AddVisualElement(military_service);
		AddVisualElement(military_service_current);
		AddVisualElement(happiness);
		AddVisualElement(stability_cost);
		AddVisualElement(pop_decrease);
		AddVisualElement(tax_efficiency);
		AddVisualElement(profession);
		AddVisualElement(races);
		
		profession_allocate.setBounds(5, 130, 15, 15);
		profession_allocate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ProfessionAllocateDialog(cmp.city, cmp, data, () -> GetData());
			}
		});
		appease_subjecs.setBounds(280, 35, 30, 15);
		appease_subjecs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new NumberDialog(cmp.city, cmp, data, () -> GetData(), Texts.appeaseSubjects, Texts.food, 0, () -> GetData().GetOwner().GetResource().food.intValue(),
						number -> {
					Action action = new Action(ActionType.APPEASE_SUBJECT_WITH_FOOD);
					action.object = GetData();
					action.quantity = (long)number;
					cmp.action_consumer.accept(action);
				});
			}
		});
		AddPlayerElement(profession_allocate);
		AddPlayerElement(appease_subjecs);
	}
	
	public void Reset(City city) {
		pop.SetNumber(city.Get().population);
		food.SetNumber(city.Get().remaining_food);
		labor.SetNumber(city.Get().labor / 30);
		labor_current.SetNumber(cmp.utils.pop_util.GetTotalLabor(city));
		military_service.SetNumber(city.Get().military_service / 30);
		military_service_current.SetNumber(cmp.utils.pop_util.GetTotalMilitaryInService(city));
		if (city.GetOwner().Playable()) {
			long food_consumed = cmp.utils.pop_util.FoodConsumption(city.Get().population, city.Get().remaining_food,
					cmp.utils.pop_util.RemainingMonthBeforeHarvest(data.GetDate().GetMonth()));
			double growth_rate = cmp.utils.pop_util.GrowthRate(city.GetOwner(), city.Get().population, food_consumed, city.GetNaturalInfo().GetTemperatureLevel());
			pop_growth.SetPercentage(growth_rate, true, true);
			food_consumption.SetNumber(-food_consumed);
			pop_decrease.SetPercentage(-cmp.utils.pop_util.DecreaseRate(city.GetPopulation().GetHappiness()), false, true);
		} else {
			pop_growth.SetText("");
			food_consumption.SetText("");
			pop_decrease.SetText("");
		}
		happiness.SetValue(city.Get().happiness);
		stability_cost.SetNumber(cmp.utils.city_util.GetStabilityCost(city));
		tax_efficiency.SetPercentage(cmp.utils.prod_util.GetTaxEfficiency(city), false, false);
		State owner = city.GetOwner();
		tax_efficiency.SetTooltipText(StringUtil.ConvertToHTML(
				Texts.base + Texts.point + ": " + data.GetParam().tax_efficiency_base,
				"+ " + String.format("%.1f", cmp.utils.prod_util.GetTaxEfficiencyBoost(owner)) + " (" + Texts.from + Texts.technology + ")",
				"* " + String.format("%.1f", cmp.utils.prod_util.GetTaxEfficiencyFromType(city)) + " (" + Texts.from + Texts.cityType + ")",
				"* " + String.format("%.1f", cmp.utils.prod_util.GetTaxEfficiencyFromStability(owner)) + " (" + Texts.from + Texts.stability + ")",
				"* " + String.format("%.1f", cmp.utils.prod_util.GetTaxEfficiencyFromStabilityFromRaces(city)) + " (" + Texts.from + Texts.race + ")",
				"* " + String.format("%.1f", cmp.utils.prod_util.GetTaxEfficiencyFromStabilityFromGovernor(city)) + " (" + Texts.from + Texts.governor + ")",
				"* " + String.format("%.1f", cmp.utils.prod_util.GetTaxEfficiencyFromStabilityFromIdeology(owner)) + " (" + Texts.from + Texts.ideology + ")"));
		
		CityPopulation city_pop = city.GetPopulation();
		double[] profession_ratio = new double[Profession.values().length];
		String[] tooltip_text_formats = new String[profession_ratio.length];
		for (int i = 0; i < profession_ratio.length; ++i) {
			profession_ratio[i] = city_pop.GetProfessionRatio(Profession.values()[i]);
			tooltip_text_formats[i] = Texts.professions[i] + "=%.1f%% (" + city_pop.GetProfessionTargetPct(Profession.values()[i]) + "%%)";
		}
		profession.SetValues(profession_ratio).SetTooltipTextFormat(tooltip_text_formats);
		
		int num_races = city_pop.NumRaces();
		double[] race_pct = new double[num_races];
		String[] race_names = new String[num_races];
		for (int i = 0; i < num_races; ++i) {
			race_pct[i] = city_pop.GetPopRatio(i);
			race_names[i] = city_pop.GetRace(i).GetName();
		}
		races.SetValues(race_pct).SetLabels(race_names);
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);

		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		text_writer.DrawString(5, 10, Texts.registeredPopulation);
		text_writer.DrawString(5, 35, Texts.remainingFood);
		text_writer.DrawString(5, 60, Texts.labor);
		text_writer.DrawString(5, 85, Texts.militaryService);
		text_writer.DrawString(255, 10, "/" + Texts.month);
		text_writer.DrawString(255, 35, "/" + Texts.month);
		text_writer.DrawString(255, 60, "(" + Texts.peopleMonth + ")");
		text_writer.DrawString(255, 85, "(" + Texts.peopleMonth + ")");
		
		
		text_writer.DrawString(25, 130, Texts.professionDistribution);
		text_writer.DrawString(250, 130, Texts.raceDistribution);
		
		text_writer.DrawString(320, 10, Texts.happiness);
		text_writer.DrawString(320, 35, Texts.stabilityCost);
		text_writer.DrawString(320, 60, Texts.populationDecrease);
		text_writer.DrawString(320, 85, Texts.taxEfficiency);
	}

	private TextElement pop;
	private TextElement pop_growth;
	private TextElement food;
	private TextElement food_consumption;
	private TextElement labor;
	private TextElement labor_current;
	private TextElement military_service;
	private TextElement military_service_current;
	private RatioBarElement happiness;
	private TextElement pop_decrease;
	private TextElement stability_cost;
	private TextElement tax_efficiency;
	private PieElement profession;
	private PieElement races;
	
	// Player Element
	private JButton profession_allocate = new JButton(Texts.settingIcon);
	private JButton appease_subjecs = new JButton(Texts.appeaseSubjects);
}
