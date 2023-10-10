package ac.ui.swing.panel;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import ac.data.base.Resource;
import ac.data.base.Resource.ResourceType;
import ac.data.constant.Colors;
import ac.data.constant.Texts;
import ac.data.constant.Technology;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.dialog.BudgetAllocateDialog;
import ac.ui.swing.dialog.PercentageDialog;
import ac.ui.swing.elements.TableElement;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.util.StringUtil;

public class StateResourcePanel extends TypedDataPanel<State> {
	private static final long serialVersionUID = 1L;
	public StateResourcePanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		food_tax = new TextElement(new Rectangle(60, 10, 50, 18));
		food_income = new TextElement(new Rectangle(260, 10, 100, 18));
		tech_budget = new TextElement(new Rectangle(60, 230, 50, 18));
		food_budget = new TextElement(new Rectangle(30, 260, 100, 18));
		horse_budget = new TextElement(new Rectangle(185, 260, 100, 18));
		iron_budget = new TextElement(new Rectangle(340, 260, 100, 18));
		table = new TableElement(new Rectangle(5, 40, 110, 30), 6, 4);
		for (int i = 0; i < Resource.kMaxTypes; ++i) {
			table.CreateCell(i + 1, 0).SetText(Texts.resourcesIcon[i]);
			table.CreateCell(i + 1, 1).SetFontSize(12);
			table.CreateCell(i + 1, 2).SetFontSize(12).SetTextColor(Colors.DARK_GREEN);
			table.CreateCell(i + 1, 3).SetFontSize(12).SetTextColor(Colors.DARK_RED);
		}
		table.CreateCell(0, 1).SetText(Texts.currentOwned);
		table.CreateCell(0, 2).SetText(Texts.estimatedIncome);
		table.CreateCell(0, 3).SetText(Texts.estimatedExpense);
		
		AddVisualElement(table);
		AddVisualElement(food_tax);
		AddVisualElement(food_income);
		AddVisualElement(tech_budget);
		AddVisualElement(food_budget);
		AddVisualElement(horse_budget);
		AddVisualElement(iron_budget);
		
		
		change_tax_rate.setBounds(120, 10, 18, 18);
		change_tax_rate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PercentageDialog.CreatePercentageDialogForState(cmp.state, cmp, data, () -> GetData(), Texts.changeTaxRate, Texts.foodTax,
						() -> GetData().GetEconomic().GetFoodTaxPercentage(), pct -> {
					Action action = new Action(ActionType.CHANGE_FOOD_TAX);
					action.object = GetData();
					action.quantity = pct;
					cmp.action_consumer.accept(action);
					cmp.Repaint();
				});
			}
		});
		allocate_budget.setBounds(120, 230, 18, 18);
		allocate_budget.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new BudgetAllocateDialog(cmp.state, cmp, data, () -> GetData());
			}
		});
		AddPlayerElement(change_tax_rate);
		AddPlayerElement(allocate_budget);
	}

	public void Reset(State state) {
		Resource<Long> total_yields = cmp.utils.state_util.GetMonthlyProduce(state);

		state.GetResource().Iterate((r, i) -> {
			table.GetCell(i + 1, 1).SetNumber(r);
		});
		total_yields.Iterate((y, i) -> {
			table.GetCell(i + 1, 2).SetNumber(y);
		});
		total_expense.SetAll(0L);
		total_expense.food = cmp.utils.state_util.GetFoodExpense(state);
		total_expense.gold = cmp.utils.state_util.GetExpense(state);
		Resource<Long> reinforce_expense = new Resource<Long>(0L);
		for (City city : state.GetOwnedCities()) {
			Resource.AddResource(reinforce_expense, cmp.utils.army_util.GetReinforceCost(city,
					cmp.utils.city_util.GetMonthlyReinforcement(city)));
		}
		Resource.AddResource(total_expense, reinforce_expense);
		total_expense.food += state.GetEconomic().GetResourceExportQuantity(ResourceType.FOOD);
		total_expense.horse += state.GetEconomic().GetResourceExportQuantity(ResourceType.HORSE);
		total_expense.iron += state.GetEconomic().GetResourceExportQuantity(ResourceType.IRON);
		
		// Tooltips
		String[] tool_tip_texts = new String[Resource.kMaxTypes];
		tool_tip_texts[0] = StringUtil.ConvertToHTML(
				Texts.army + Texts.expense + ": " + StringUtil.LongNumber(cmp.utils.state_util.GetFoodPayment(state)),
				Texts.logisticCost + ": " + StringUtil.LongNumber(cmp.utils.state_util.GetFoodConsumption(state)),
				Texts.reinforce + Texts.expense + ": " + StringUtil.LongNumber(reinforce_expense.food),
				Texts.exports + Texts.expense + ": " + StringUtil.LongNumber(state.GetEconomic().GetResourceExportQuantity(ResourceType.FOOD)));
		long building_maintenance_cost = 0L;
		long city_stability_cost = 0L;
		long army_maintenance_cost = 0L;
		for (City city : state.GetOwnedCities()) {
			building_maintenance_cost += cmp.utils.city_util.GetBuildingMaintenanceCost(city);
			city_stability_cost += cmp.utils.city_util.GetStabilityCost(city);
			army_maintenance_cost += cmp.utils.army_util.GetMaintenanceCost(city.GetMilitary().GetGarrison());
		}
		tool_tip_texts[1] = StringUtil.ConvertToHTML(
				Texts.reinforce + Texts.expense + ": " + StringUtil.LongNumber(reinforce_expense.hammer));
		tool_tip_texts[2] = StringUtil.ConvertToHTML(
				Texts.salary + Texts.expense + ": " + StringUtil.LongNumber(cmp.utils.state_util.GetSalaryExpense(state)),
				Texts.army + Texts.expense + ": " + StringUtil.LongNumber(cmp.utils.state_util.GetSoldierWageExpense(state)),
				Texts.building + Texts.maintenanceCost + ": " + StringUtil.LongNumber(building_maintenance_cost),
				Texts.stabilityCost + ": " + StringUtil.LongNumber(city_stability_cost),
				Texts.garrison + Texts.maintenanceCost + ": " + StringUtil.LongNumber(army_maintenance_cost));
		tool_tip_texts[3] = StringUtil.ConvertToHTML(
				Texts.reinforce + Texts.expense + ": " + StringUtil.LongNumber(reinforce_expense.horse),
				Texts.exports + Texts.expense + ": " + StringUtil.LongNumber(state.GetEconomic().GetResourceExportQuantity(ResourceType.HORSE)));
		tool_tip_texts[4] = StringUtil.ConvertToHTML(
				Texts.reinforce + Texts.expense + ": " + StringUtil.LongNumber(reinforce_expense.iron),
				Texts.exports + Texts.expense + ": " + StringUtil.LongNumber(state.GetEconomic().GetResourceExportQuantity(ResourceType.IRON)));
				
		total_expense.Iterate((e, i) -> {
			table.GetCell(i + 1, 3).SetNumber(-e);
			table.GetCell(i + 1, 3).SetTooltipText(tool_tip_texts[i]);
		});
//		Resource<Integer> military_budget = state.GetEconomic().GetMilitaryBudgetPct();
//		military_budget.Iterate((b, i) -> {
//			table.GetCell(i + 1, 4).SetText(String.format("%d%%", b));
//		});
		food_tax.SetText(String.format("%.0f%%", state.GetEconomic().GetFoodTax() * 100));
//		long total_food = 0;
//		for (City city : state.GetOwnedCities()) {
//			long food = cmp.utils.city_util.GetFoodYield(city);
//			double tax_efficiency = cmp.utils.prod_util.GetTaxEfficiency(city);
//			total_food += food * city.GetOwner().GetEconomic().GetFoodTax() * tax_efficiency;
//		}
		food_income.SetNumber(cmp.utils.state_util.GetEstimatedAnnualFoodIncome(state));
		tech_budget.SetText(String.format("%.0f%%", state.GetEconomic().GetTechBudgetRatio() * 100));
		SetBudget(state, ResourceType.FOOD, food_budget);
		SetBudget(state, ResourceType.HORSE, horse_budget);
		SetBudget(state, ResourceType.IRON, iron_budget);
		
		if (state == data.GetPlayer().GetState()) {
			change_tax_rate.setEnabled(
					cmp.utils.state_util.HasEffect(state, Technology.Effect.UNBLOCK_TAX_RATE_CHANGE) && state.Get().stability > 1);
		}
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);

		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		text_writer.DrawString(5, 10, Texts.foodTax);
		text_writer.DrawString(175, 10, Texts.agriculture + Texts.estimatedTaxIncome);
		text_writer.DrawString(5, 230, Texts.technology + Texts.budget);
		text_writer.SetFontSize(14);
		text_writer.DrawString(5, 260, Texts.foodIcon);
		text_writer.DrawString(160, 260, Texts.horseIcon);
		text_writer.DrawString(315, 260, Texts.ironIcon);
	}
	
//	private long GetExpense(State state, ResourceType type) {
//		if (type == ResourceType.GOLD) {
//			return cmp.utils.state_util.GetExpense(state);
//		}
//		return 0L;
//	}
	
	private void SetBudget(State state, ResourceType type, TextElement text_element) {
		int budget = state.GetEconomic().GetResourcePurchaseBudgetPercentage(type);
		if (budget > 0) {
			text_element.SetText(String.format("%s / %d%%", Texts.imports + Texts.budget, budget));
			return;
		}
		long export_quanity = state.GetEconomic().GetResourceExportQuantity(type);
		if (export_quanity > 0) {
			text_element.SetText(String.format("%s: %,d", Texts.exports, export_quanity));
		} else {
			text_element.SetText("");
		}
	}
	
	private TableElement table;
	private TextElement food_tax;
	private TextElement food_income;
	private TextElement tech_budget;
	private TextElement food_budget;
	private TextElement horse_budget;
	private TextElement iron_budget;
	
	private Resource<Long> total_expense = new Resource<Long>(0L);
	
	// Player Element
	private JButton change_tax_rate = new JButton(Texts.settingIcon);
	private JButton allocate_budget = new JButton(Texts.settingIcon);
}
