package ac.engine.data;

import ac.data.StateData.EconomicData;
import ac.data.base.Resource.ResourceType;

public class StateEconomic extends Data {

	protected StateEconomic(DataAccessor accessor, EconomicData data) {
		super(accessor);
		this.data = data;
	}
	
	public double GetFoodTax() {
		return (double)data.food_tax_pct / 100;
	}
	
	public int GetFoodTaxPercentage() {
		return data.food_tax_pct;
	}
	
	public int GetTechBudgetPercentage() {
		return data.research_budget_pct;
	}
	
	public double GetTechBudgetRatio() {
		return (double)data.research_budget_pct / 100;
	}
	
	public double GetResourceImportBudgetRatio(ResourceType type) {
		int pct = GetResourcePurchaseBudgetPercentage(type);
		if (pct > 0) {
			return (double)pct / 100;
		}
		return 0.0;
	}
	
	public long GetResourceExportQuantity(ResourceType type) {
		int pct = GetResourcePurchaseBudgetPercentage(type);
		if (pct > 0) return 0;
		return -pct;
	}
	
	
	public long GetTechBudget() {
		return (long) (GetTechBudgetRatio() * net_income);
	}
	
	public long GetResourceImportBudget(ResourceType type) {
		return (long) (GetResourceImportBudgetRatio(type) * net_income);
	}
	
	public void SetNetIncome(long net_income) {
		this.net_income = net_income;
	}
	
	public void SetFoodTaxPercentage(int pct) {
		data.food_tax_pct = pct;
	}
	
	public void SetTechBudgetPercentage(int pct) {
		data.research_budget_pct = pct;
	}
	
	public void SetResourcePurchaseBudgetPercentage(ResourceType type, int pct) {
		switch (type) {
		case FOOD:
			data.food_budget_pct = pct;
			return;
		case HORSE:
			data.horse_budget_pct = pct;
			return;
		case IRON:
			data.iron_budget_pct = pct;
			return;
		case GOLD:
		case HAMMER:
		}
	}
	
	
	public int GetResourcePurchaseBudgetPercentage(ResourceType type) {
		switch (type) {
		case FOOD:
			return data.food_budget_pct;
		case HORSE:
			return data.horse_budget_pct;
		case IRON:
			return data.iron_budget_pct;
		case GOLD:
		case HAMMER:
		}
		return 0;
	}
	
	
	private EconomicData data;
	private long net_income;
}
