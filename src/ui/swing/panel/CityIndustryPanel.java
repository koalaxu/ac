package ac.ui.swing.panel;

import java.awt.Rectangle;

import ac.data.CityData.Profession;
import ac.data.base.Resource;
import ac.data.base.Resource.ResourceType;
import ac.data.constant.Improvement;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.util.CityUtil.IndustryPopulationDistribution;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.TableElement;

public class CityIndustryPanel extends TypedDataPanel<City> {

	private static final long serialVersionUID = 1L;

	public CityIndustryPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		table = new TableElement(new Rectangle(5, 5, 64, 25), 11, 7);
		AddVisualElement(table);
		
		for (int i = 0; i < Improvement.kIndustryImprovements.length; ++i)  {
			table.CreateCell(i + 1, 0).SetText(Texts.industryImprovements[i]);
		}
		table.CreateCell(8, 0).SetText(Texts.workshop);
		table.CreateCell(9, 0).SetText(Texts.sum);
		table.CreateCell(10, 0).SetText(Texts.estimatedTaxIncome);
		table.CreateCell(0, 1).SetText(Texts.count);
		for (int j = 0; j < Resource.kMaxTypes; ++j) {
			table.CreateCell(0, j + 2).SetText(Texts.resourcesIcon[j]);
		}
	}
	
	public void Reset(City city) {
		long worker = cmp.utils.city_util.GetProfessionalPopulation(city, Profession.WORKER);
		table.GetCellOrCreate(9, 1).SetNumber(worker);
		IndustryPopulationDistribution dist = cmp.utils.city_util.GetIndustryPopulationDistribution(city);
		table.GetCellOrCreate(8, 1).SetNumber(dist.workshop_worker).SetTooltipText(
				String.format(Texts.tooltipWorkshopPopulation, data.GetParam().population_per_industry_improvement));
		Resource<Long> total_yield = new Resource<Long>(0L);
		GenerateYieldCell(7, ImprovementType.WORKSHOP, dist.workshop_worker, total_yield, city);
		
		for (int i = 0; i < Improvement.kIndustryImprovements.length; ++i) {
			ImprovementType improvement_type = Improvement.kIndustryImprovements[i];
			int improvements = city.GetImprovements().GetCount(improvement_type);
			table.GetCellOrCreate(i + 1, 1).SetText(String.format("%2d * %3d%%", improvements,
					(int)(dist.worker_per_improvment / data.GetParam().population_per_industry_improvement * 100)));
			if (dist.worker_per_improvment > 0) {
				table.GetCell(i + 1, 1).SetTooltipText(
					String.format(Texts.tooltipIndustryImprovementPopulation, improvements, (int)dist.worker_per_improvment));
			}
			GenerateYieldCell(i, improvement_type, (long) (improvements * dist.worker_per_improvment), total_yield, city);
		}
		total_yield.Iterate((yield, index) -> {
			table.GetCellOrCreate(9, 2 + index).SetNumber(yield);
			table.GetCellOrCreate(10, 2 + index).SetNumber((long) (yield * cmp.utils.prod_util.GetTaxEfficiency(city)));
		});
	}
	
	private void GenerateYieldCell(int i, ImprovementType improvement_type, long workers, Resource<Long> total_yield, City city) {
		Resource<Double> unit_yield = data.GetParam().GetImprovementYield(improvement_type);
		String[] yield_tooltip_text = { "" };
		boolean[] resources_to_draw = new boolean[ResourceType.values().length];
		unit_yield.Iterate((yield, index) -> {
			if (yield > 0) {
				resources_to_draw[index] = true;
				yield_tooltip_text[0] += String.format(Texts.tooltipIndustryYieldResource, Texts.resourcesIcon[index], yield);
			}			
		});
		String tooltip_text = String.format(Texts.tooltipIndustryYield, data.GetParam().population_per_industry_improvement,
				i < Texts.industryImprovements.length ? Texts.industryImprovements[i] : Texts.workshop, yield_tooltip_text[0]);
		Resource<Long> yield = cmp.utils.prod_util.GetProduction(city.GetOwner(), improvement_type, workers);
		yield.Iterate((y, index) -> {
			if (resources_to_draw[index]) {
				table.CreateCell(i + 1, 2 + index).SetNumber(y).SetTooltipText(tooltip_text);
			}
		});
		Resource.AddResource(total_yield, yield);
	}

	private TableElement table;
}
