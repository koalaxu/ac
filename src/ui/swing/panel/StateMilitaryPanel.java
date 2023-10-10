package ac.ui.swing.panel;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import ac.data.ArmyData;
import ac.data.ArmyData.SoldierType;
import ac.data.base.Date;
import ac.data.base.Resource;
import ac.data.constant.ConstStateData;
import ac.data.constant.Texts;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.Garrison;
import ac.engine.data.State;
import ac.engine.data.StateMilitary;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.RatioBarElement;
import ac.ui.swing.elements.TableElement;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.util.StringUtil;

public class StateMilitaryPanel extends TypedDataPanel<State> {

	private static final long serialVersionUID = 1L;

	public StateMilitaryPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		for (int i = 0; i < Unit.kMaxUnitType; ++i) {
			available_units[i] = new TextElement(new Rectangle(85 + i * 95, 10, 80, 15));
			AddVisualElement(available_units[i]);
		}
		total = new RatioBarElement(new Rectangle(60, 35, 260, 15));
		maintenance_cost = new TextElement(new Rectangle(390, 35, 60, 15));
		reinforce_cost = new TextElement(new Rectangle(60, 60, 175, 15));
		reinforce_cost_monthly = new TextElement(new Rectangle(250, 60, 175, 15));
		AddVisualElement(total);
		AddVisualElement(maintenance_cost);
		AddVisualElement(reinforce_cost);
		AddVisualElement(reinforce_cost_monthly);
		for (int i = 0; i < ArmyData.kMaxSoldierTypes; i++) {
			typed_soldiers[i] = new TextElement(new Rectangle(60 + 155 * i, 85, 80, 15));
			AddVisualElement(typed_soldiers[i]);
		}
		typed_soldiers[0].SetTooltipText(Texts.upperbound + ": " + Texts.none);
		
		table = new TableElement(new Rectangle(5, 110, 90, 24), ConstStateData.kMaxArmies + 1, 5);
		table.CreateCell(0, 1).SetText(Texts.soldierNumber);
		table.CreateCell(0, 2).SetText(Texts.baseCity);
		table.CreateCell(0, 3).SetText(Texts.target);
		table.CreateCell(0, 4).SetText(Texts.estimatedArriveTime);
		AddVisualElement(table);
	}

	@Override
	public void Reset(State state) {
		StateMilitary military = state.GetMilitary();
		for (int i = 0; i < Unit.kMaxUnitType; ++i) {
			UnitType type = UnitType.values()[i];
			Unit most_advanced_unit = state.GetTechnology().GetMostAdvancedUnit(type);
			available_units[i].SetText(most_advanced_unit.name);
			String tooltip_text = "<html><table>";
			tooltip_text += String.format("<tr><th>-------</th><th>%s</th><th>%s</th><th>%s</th><th>%s</th><th>%s</th><th>%s</th></tr>",
					Texts.attackIcon, Texts.defendIcon, Texts.hammerIcon, Texts.goldIcon, Texts.horseIcon, Texts.ironIcon);
			ArrayList<Unit> units = data.GetConstData().typed_units.get(i);
			for (Unit unit : units) {
				tooltip_text += String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
						unit.name, unit.attack, unit.defend, unit.cost.hammer, unit.cost.gold, unit.cost.horse, unit.cost.iron);
				if (unit == most_advanced_unit) break;
			}
			tooltip_text += "</table><html>";
			available_units[i].SetTooltipText(tooltip_text);
		};
		long max_garrison = 0L;
		long total_garrison = 0L;
		for (City city : state.GetOwnedCities()) {
			Garrison garrison = city.GetMilitary().GetGarrison();
			max_garrison += garrison.GetMaxSoldier();
			total_garrison += garrison.GetTotalSoldier();
		}
		total.SetMax(max_garrison).SetValue(total_garrison);
//		Resource<Long> total_yields = cmp.utils.state_util.GetMonthlyProduce(state);
//		Resource<Double> military_budget = state.GetEconomic().GetMilitaryBudgetRatio();
//		total_yields.Assign((y, e) -> (long) (y - e), total_yields, type -> GetExpense(state, type));
//		Resource.Product(total_yields, total_yields, military_budget);
		long maintanence = 0L;
		String maintanence_tooltip_text = "<html>" + Texts.garrison + Texts.sum + ": ";
		String reinforce_tooltip_text = "<html>" + Texts.garrison + Texts.sum + ": ";
		String reinforce_monthly_tooltip_text = "<html>" + Texts.garrison + Texts.sum + ": ";
		Resource<Long> reinforce_requirement = new Resource<Long>(0L);
		Resource<Long> reinforce_requirement_monthly = new Resource<Long>(0L);
		for (City city : state.GetOwnedCities()) {
			Garrison garrison = city.GetMilitary().GetGarrison();
			maintanence += cmp.utils.army_util.GetMaintenanceCost(garrison);
			Resource.AddResource(reinforce_requirement, cmp.utils.army_util.GetReinforceCost(city, Long.MAX_VALUE));
			Resource.AddResource(reinforce_requirement_monthly, cmp.utils.army_util.GetReinforceCost(city,
					cmp.utils.city_util.GetMonthlyReinforcement(city)));
		}
		maintanence_tooltip_text += StringUtil.LongNumber(maintanence);
		reinforce_tooltip_text += reinforce_requirement.toSimpleString();
		reinforce_monthly_tooltip_text += reinforce_requirement_monthly.toSimpleString();
		for (Army army : state.GetMilitary().GetArmies()) {
			long army_maintanence_cost = cmp.utils.army_util.GetMaintenanceCost(army);
//			Resource<Long> army_reinforce_cost = cmp.utils.army_util.GetReinforceCost(army, most_advanced_units, Long.MAX_VALUE);
//			Resource<Long> army_reinforce_cost_monthly = cmp.utils.army_util.GetReinforceCost(army, most_advanced_units,
//					cmp.utils.state_util.GetMonthlyReinforcement(state));
			maintanence += army_maintanence_cost;
			maintanence_tooltip_text += "<br>" + army.GetName() + ": " + army_maintanence_cost;
//			reinforce_tooltip_text += "<br>" + army.GetName() + ": " + army_reinforce_cost.toSimpleString();
//			reinforce_monthly_tooltip_text += "<br>" + army.GetName() + ": " + army_reinforce_cost_monthly.toSimpleString();
//			Resource.AddResource(reinforce_requirement, army_reinforce_cost);
//			Resource.AddResource(reinforce_requirement_monthly, army_reinforce_cost_monthly);
		}
		maintenance_cost.SetNumber(maintanence).SetTooltipText(maintanence_tooltip_text + "</html>");
		reinforce_cost.SetText(reinforce_requirement.toSimpleString()).SetTooltipText(reinforce_tooltip_text + "</html>");
		reinforce_cost_monthly.SetText(reinforce_requirement_monthly.toSimpleString()).SetTooltipText(reinforce_monthly_tooltip_text + "</html>");
		
		ArrayList<Army> armies = military.GetArmies();
		for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
			TextElement name_cell = table.GetCellOrCreate(i + 1, 0);
			TextElement solider_cell = table.GetCellOrCreate(i + 1, 1);
			TextElement city_cell = table.GetCellOrCreate(i + 1, 2);
			TextElement target_cell = table.GetCellOrCreate(i + 1, 3);
			TextElement arrive_time_cell = table.GetCellOrCreate(i + 1, 4);
			if (i < armies.size()) {
				Army army = armies.get(i);
				City city = army.GetBaseCity();
				name_cell.SetText(army.GetName()).SetClickCallback(() -> cmp.army.Show(army));
				name_cell.SetUseHandCursor(true);
				solider_cell.SetNumber(army.GetTotalSoldier());
				city_cell.SetText(city.GetName()).SetClickCallback(() -> cmp.city.Show(city));
				city_cell.SetUseHandCursor(true);
				if (army.GetStatus() == Army.Status.IDLE) {
					target_cell.SetText(Texts.baseCity);
				} else if (army.GetStatus() == Army.Status.RETREAT) {
					target_cell.SetText(Texts.retreat).SetClickCallback(null);
					target_cell.SetUseHandCursor(false);
				} else {
					Army target = army.GetTarget();
					if (target != null) {
						target_cell.SetText(target.GetName()).SetClickCallback(() -> cmp.army.Show(target));
						target_cell.SetUseHandCursor(true);
					} else {
						target_cell.SetText("").SetClickCallback(null);
						target_cell.SetUseHandCursor(false);
					}
				}
				Date date = army.GetArriveDate();
				//arrive_time_cell.SetText(date != null ? date.MonthDayString() : "");
				arrive_time_cell.SetText(date != null ? date.ShortString() : "");
			} else {
				name_cell.SetText("").SetClickCallback(null);
				name_cell.SetUseHandCursor(false);
				solider_cell.SetText("");
				city_cell.SetText("").SetClickCallback(null);
				city_cell.SetUseHandCursor(false);
				target_cell.SetText("").SetClickCallback(null);
				target_cell.SetUseHandCursor(false);
				arrive_time_cell.SetText("");
			}
		}
		for (int i = 0; i < ArmyData.kMaxSoldierTypes; i++) {
			SoldierType type = SoldierType.values()[i];
			typed_soldiers[i].SetNumber(cmp.utils.state_util.GetTypedSoldiers(state, type));
			if (i > 0) {
				String tooltip_text = "<html>" + Texts.upperbound + ": " + cmp.utils.state_util.GetAllowedTypedSoldiers(state, type);
				if (type == SoldierType.RECRUITMENT) {
					tooltip_text += "<br>" + Texts.monthly + Texts.expense + ": " + cmp.utils.state_util.GetFoodPayment(state) + Texts.foodIcon
							+ " " + cmp.utils.state_util.GetSoldierWageExpense(state) + Texts.goldIcon;
				} else if (type == SoldierType.FUBING) {
					tooltip_text += "<br>" + Texts.canAfford + ": " + cmp.utils.state_util.GetFubingAffordability(state);
				}
				typed_soldiers[i].SetTooltipText(tooltip_text + "</html>");
				
			}
		}
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);
		
		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		text_writer.DrawString(5, 10, Texts.availableUnits);
		text_writer.DrawString(5, 35, Texts.garrison + Texts.soldierNumber);
		text_writer.DrawString(335, 35, Texts.maintenanceCost);
		text_writer.DrawString(5, 60, Texts.reinforce + Texts.requirement);
		text_writer.DrawString(430, 60, "/ "+ Texts.month);
		for (int i = 0; i < ArmyData.kMaxSoldierTypes; i++) {
			text_writer.DrawString(5 + 155 * i, 85, Texts.soldierType[i]);
		}
	}

	private TextElement[] available_units = new TextElement[Unit.kMaxUnitType];
	private RatioBarElement total;
	private TextElement maintenance_cost;
	private TextElement reinforce_cost;
	private TextElement reinforce_cost_monthly;
	private TextElement[] typed_soldiers = new TextElement[ArmyData.kMaxSoldierTypes];
	
	private TableElement table;
}
