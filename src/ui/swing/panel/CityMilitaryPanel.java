package ac.ui.swing.panel;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.JButton;

import java.util.TreeMap;

import ac.data.constant.ConstStateData;
import ac.data.constant.Texts;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.CityMilitary;
import ac.engine.data.DataAccessor;
import ac.engine.data.Garrison;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.dialog.PercentageDialog;
import ac.ui.swing.elements.GroupedElement;
import ac.ui.swing.elements.RatioBarElement;
import ac.ui.swing.elements.TableElement;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.util.StringUtil;

public class CityMilitaryPanel extends TypedDataPanel<City> {

	private static final long serialVersionUID = 1L;

	public CityMilitaryPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		garrison = new RatioBarElement(new Rectangle(60, 10, 160, 15)).SetUseSpectrum(true);
		garrison.SetUseHandCursor(true);
		defence_buffer = new TextElement(new Rectangle(290, 10, 50, 15));
		training = new RatioBarElement(new Rectangle(395, 10, 60, 15)).SetUseSpectrum(true).SetMax(100);
		
		playable_info = new GroupedElement();
		militia = new RatioBarElement(new Rectangle(60, 35, 100, 15)).SetUseSpectrum(false).SetMax(100);
		maintenance_cost = new TextElement(new Rectangle(230, 35, 60, 15));
		advanced_unit_ratio = new TextElement(new Rectangle(395, 35, 40, 15));
		reinforce_cost = new TextElement(new Rectangle(60, 60, 175, 15));
		reinforce_cost_monthly = new TextElement(new Rectangle(250, 60, 175, 15));
		table = new TableElement(new Rectangle(5, 90, 100, 24), ConstStateData.kMaxArmies + 2, 3);
		AddVisualElement(garrison);
		AddVisualElement(defence_buffer);
		AddVisualElement(training);
		AddVisualElement(playable_info);
	
		playable_info.AddVisualElement(militia);
		playable_info.AddVisualElement(maintenance_cost);
		playable_info.AddVisualElement(advanced_unit_ratio);
		playable_info.AddVisualElement(reinforce_cost);
		playable_info.AddVisualElement(reinforce_cost_monthly);
		playable_info.AddVisualElement(table);
		
		table.CreateCell(0, 1).SetText(Texts.soldierType[0]);
		table.CreateCell(0, 2).SetText(Texts.logisticLabor);
		table.CreateCell(ConstStateData.kMaxArmies + 1, 0).SetText(Texts.sum);
		table.CreateCell(ConstStateData.kMaxArmies + 1, 1).SetNumber(0L);
		table.CreateCell(ConstStateData.kMaxArmies + 1, 2).SetNumber(0L);
		
		
		advanced_unit_ratio_change.setBounds(440, 35, 15, 15);
		advanced_unit_ratio_change.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PercentageDialog.CreatePercentageDialogForCity(cmp.city, cmp, data, () -> GetData(), Texts.change + Texts.advancedUnitRatio, Texts.advancedUnitRatio,
						() -> GetData().GetMilitary().GetAdvancedUnitPercent(), pct -> {
					Action action = new Action(ActionType.CHANGE_ADVANCED_UNIT_PERCENTAGE);
					action.object = GetData();
					action.quantity = pct;
					cmp.action_consumer.accept(action);
					cmp.Repaint();
				});
			}
		});
		AddPlayerElement(advanced_unit_ratio_change);
	}

	@Override
	public void Reset(City city) {
		this.city = city;
		
		CityMilitary city_military = city.GetMilitary();
		Garrison military = city_military.GetGarrison();
		garrison.SetMax((int) military.GetMaxSoldier());
		garrison.SetValue((int) military.GetTotalSoldier());
		garrison.SetTooltipText(GetTooltipText(military));
		garrison.SetClickCallback(() -> {cmp.army.Show(military);});
		defence_buffer.SetText(StringUtil.Percentage(cmp.utils.army_util.GetCityDefenceBuffer(city)));
		training.SetValue((int) (military.GetTrainingLevel() * 100));
		
		if (!city.GetOwner().Playable()) {
			playable_info.SetVisibility(false);
			return;
		}
		playable_info.SetVisibility(true);
		Unit advanced = city.GetOwner().GetTechnology().GetMostAdvancedUnit(UnitType.ARCHERY);
		Unit second_advanced = city.GetOwner().GetTechnology().GetSecondAdvancedUnit(UnitType.ARCHERY);
		if (advanced != second_advanced) {
			advanced_unit_ratio.SetTooltipText(second_advanced.name + ": " + StringUtil.Percentage(100 - city_military.GetAdvancedUnitPercent())
				+ " / " + advanced.name + ": " + StringUtil.Percentage(city_military.GetAdvancedUnitPercent()));
		}
		defence_buffer.SetText(StringUtil.Percentage(cmp.utils.army_util.GetCityDefenceBuffer(city)));
		
		militia.SetValue((int) (cmp.utils.city_util.GetMilitiaRatio(city) * 100));
		maintenance_cost.SetNumber(cmp.utils.army_util.GetMaintenanceCost(military));
		advanced_unit_ratio.SetText(StringUtil.Percentage(city_military.GetAdvancedUnitPercent()));
		reinforce_cost.SetText(cmp.utils.army_util.GetReinforceCost(city, Long.MAX_VALUE).toSimpleString());
		reinforce_cost_monthly.SetText(cmp.utils.army_util.GetReinforceCost(city,
				cmp.utils.city_util.GetMonthlyReinforcement(city)).toSimpleString());
		
		long total_conscription_soldiers = 0L;
		long total_labor = 0L;
		ArrayList<Army> armies = city.GetOwner().GetMilitary().GetArmies();
 		for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
 			Army army = i < armies.size() ? armies.get(i) : null;
 			if (army != null) {
				table.GetCellOrCreate(i + 1, 0).SetText(army.GetName()).SetClickCallback(() -> cmp.army.Show(army));
				table.GetCell(i + 1, 0).SetUseHandCursor(true);
				long soldier = army.GetCityConscription(city);
				table.GetCellOrCreate(i + 1, 1).SetText(StringUtil.LongNumber(soldier));
				total_conscription_soldiers += soldier;
				long labor = army.GetCitySupportingLabor(city);
				table.GetCellOrCreate(i + 1, 2).SetText(StringUtil.LongNumber(labor));
				total_labor += labor;
 			} else {
 				table.GetCellOrCreate(i + 1, 0).SetText("").SetUseHandCursor(false);
 				table.GetCellOrCreate(i + 1, 1).SetText("");
 				table.GetCellOrCreate(i + 1, 2).SetText("");
 			}
		}
		table.GetCell(ConstStateData.kMaxArmies + 1, 1).SetNumber(total_conscription_soldiers);
		table.GetCell(ConstStateData.kMaxArmies + 1, 2).SetNumber(total_labor);
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);
		
		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		text_writer.DrawString(5, 10, Texts.garrison + Texts.soldierNumber);
		text_writer.DrawString(235, 10, Texts.defenceBuffer);
		text_writer.DrawString(350, 10, Texts.trainingLevel);
		if (!city.GetOwner().Playable()) return;
		text_writer.DrawString(5, 35, Texts.militiaRatio);
		text_writer.DrawString(175, 35, Texts.maintenanceCost);
		text_writer.DrawString(315, 35, Texts.advancedUnitRatio);
		text_writer.DrawString(5, 60, Texts.reinforce + Texts.requirement);
		text_writer.DrawString(430, 60, "/ "+ Texts.month);
	}
	
	private String GetTooltipText(Garrison military) {
		String tooltip_text = "";
		TreeMap<Unit, Long> unit_quantities = military.GetUnitQuantities();
		for (Entry<Unit, Long> kv : unit_quantities.entrySet()) {
			tooltip_text += (tooltip_text == "" ? "" : "<br>") + kv.getKey().name + Texts.soldier + ": " + StringUtil.LongNumber(kv.getValue());
		}
		return "<html>" + tooltip_text + "</html>";
	}
	
	private City city;
	
	private RatioBarElement garrison;
	private TextElement defence_buffer;
	private RatioBarElement training;
	
	private GroupedElement playable_info;
	private RatioBarElement militia;
	private TextElement maintenance_cost;
	private TextElement advanced_unit_ratio;
	private TextElement reinforce_cost;
	private TextElement reinforce_cost_monthly;
	
	private TableElement table;
	
	// Player Element
	private JButton advanced_unit_ratio_change = new JButton(Texts.settingIcon);
}
