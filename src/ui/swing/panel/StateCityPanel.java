package ac.ui.swing.panel;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import ac.data.CityData.Profession;
import ac.data.base.Resource;
import ac.data.constant.Texts;
import ac.engine.ai.Ranker;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.ScrollListComponent;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.util.StringUtil;

public class StateCityPanel extends TypedDataPanel<State> {
	private static final long serialVersionUID = 1L;
	public StateCityPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		city_list = new ScrollListComponent(kColumnWidth, 20);
		city_list.SetColumnHeaders(kColumnNames);
		
		city_list.setBounds(5, 5, 450, 255);
		add(city_list);
		
		AddVisualElement(max_county);
	}
	
	public void Reset(State state) {
		city_list.Resize(state.GetOwnedCities().size());
		ArrayList<City> city_view = new ArrayList<City>(state.GetOwnedCities());
		city_view.sort(Ranker.city_populations);
		
		int index = 0;
		for (City city : city_view) {
			city_list.SetCallback(index, () -> cmp.city.Show(city));
			city_list.SetValue(index, 0, city.GetName());
			city_list.SetValue(index, 1, Texts.cityTypeSymbol[city.GetType().ordinal()]);
			city_list.SetValue(index, 2, StringUtil.LongNumber(city.Get().population));
			long food_consumed = cmp.utils.pop_util.FoodConsumption(city.Get().population, city.Get().remaining_food,
					cmp.utils.pop_util.RemainingMonthBeforeHarvest(data.GetDate().GetMonth() - 1));
			double pop_growth_rate = cmp.utils.pop_util.GrowthRate(state, city.Get().population, food_consumed, city.GetNaturalInfo().GetTemperatureLevel());		
			city_list.SetValue(index, 3, StringUtil.AccuratePercentage(pop_growth_rate, true));
			city_list.SetValue(index, 4, StringUtil.Percentage(city.Get().happiness));
			city_list.SetValue(index, 5, StringUtil.Percentage((double)city.Get().riot / 10000));
			
			long food = cmp.utils.city_util.GetFoodYield(city);
			double tax_efficiency = cmp.utils.prod_util.GetTaxEfficiency(city);
			city_list.SetValue(index, 6, StringUtil.LongNumber((long) (food * city.GetOwner().GetEconomic().GetFoodTax() * tax_efficiency)));
			Resource<Long> total_yields = new Resource<Long>(0L);
			Resource<Long> city_yields = cmp.utils.city_util.GetIndustryProduction(city);
			Resource.AddResource(total_yields, city_yields, tax_efficiency);
			int[] idx = { index };
			total_yields.Iterate((yield, i) -> {
				city_list.SetValue(idx[0], 7 + i, StringUtil.LongNumber(yield));
			});
//			for (int i = 0; i < Resource.kMaxTypes; ++i) {
//				city_list.SetValue(index, 5 + i, StringUtil.LongNumber(total_yields.Get(ResourceType.values()[i])));
//			}
			long merchant_num = data.GetUtils().city_util.GetProfessionalPopulation(city, Profession.MERCHANT);
			double unit_income = data.GetUtils().prod_util.GetIncomePerMerchant(city, city.GetPopulation().GetProfessionRatio(Profession.MERCHANT));
			city_list.SetValue(index, 12, StringUtil.LongNumber((long) (merchant_num * unit_income * tax_efficiency)));
			city_list.SetValue(index, 13, StringUtil.LongNumber(cmp.utils.city_util.GetBuildingMaintenanceCost(city)
					+ cmp.utils.city_util.GetStabilityCost(city)));
			index++;
		}
		max_county.SetNumber(cmp.utils.state_util.GetAllowedCounties(state));
	}

	public void paintComponent(Graphics g)  {
		super.paintComponent(g);

		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		text_writer.DrawString(5, 265, Texts.max + Texts.county + Texts.count);
	}
	
	private ScrollListComponent city_list;
	
	private static final int[] kColumnWidth = { 45, 20, 80, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60 };
	private static final String[] kColumnNames = { Texts.city, "", Texts.totalPopulation, Texts.growth, Texts.happiness, Texts.riotLevel,
			Texts.foodIcon + "(" + Texts.agriculture + ")",
			Texts.resourcesIcon[0], Texts.resourcesIcon[1], Texts.resourcesIcon[2], Texts.resourcesIcon[3], Texts.resourcesIcon[4], 
			Texts.goldIcon + "(" + Texts.commerce + ")", Texts.goldIcon + "(" + Texts.expense + ")"};
	
	private TextElement max_county = new TextElement(new Rectangle(100, 265, 50, 15));
}
