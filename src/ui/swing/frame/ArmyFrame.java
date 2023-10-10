package ac.ui.swing.frame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JTabbedPane;

import ac.data.ArmyData;
import ac.data.ArmyData.SoldierType;
import ac.data.constant.Colors;
import ac.data.constant.Texts;
import ac.data.constant.Tile.Terrain;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.Person;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;
import ac.ui.swing.GenericPanel;
import ac.ui.swing.dialog.CitySelectionDialog;
import ac.ui.swing.dialog.CitySelectionDialog.Ranker;
import ac.ui.swing.dialog.PersonSelectionDialog;
import ac.ui.swing.elements.BarElement;
import ac.ui.swing.elements.PieElement;
import ac.ui.swing.elements.RatioBarElement;
import ac.ui.swing.elements.ScrollListComponent;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.util.StringUtil;

public class ArmyFrame extends GenericFrame {

	private static final long serialVersionUID = 1L;

	public ArmyFrame(Components cmp, DataAccessor data) {
		super(cmp, data, 450, 310);
		setAlwaysOnTop(true);
		setLayout(null);
		setVisible(false);
		
		training.SetUseSpectrum(true).SetMax(100);
		morale.SetUseSpectrum(true).SetMax(100);
		top_panel = new ArmyPanel(cmp, data);
		top_panel.setBounds(0, 0, 450, 110);
		add(top_panel);
		tabbed_panel.setBounds(0, 115, 450, 170);
		add(tabbed_panel);
		
		unit_panel = new UnitPanel(cmp, data);
		soldier_panel = new SoldierPanel(cmp, data);
		logistic_panel = new LogisticPanel(cmp, data);
		march_panel = new MarchPanel(cmp, data);
	}
	
	public void Show(Army army) {
		this.army = army;
		top_panel.SetVisibilityForPlayerElement(army.GetState() == data.GetPlayer().GetState() && !army.IsGarrison());
		tabbed_panel.removeAll();
		tabbed_panel.add(Texts.unit, unit_panel);
		if (!army.IsGarrison()) {
			tabbed_panel.add(Texts.soldierSource, soldier_panel);
			tabbed_panel.add(Texts.logistic, logistic_panel);
			tabbed_panel.add(Texts.march + Texts.speed, march_panel);
		}
		
		Reset();
		repaint();
		setVisible(true);
		cmp.side_panel.ShowArmy(army);
	}
	
	private void Reset() {
		setTitle(army.GetName());
		double[] units = new double[Unit.kMaxUnitType];
		int total = 0;
		int max_total = 0;
		for (UnitType type : UnitType.values()) {
			TreeMap<Unit, Long> unit_quantities = army.GetUnitQuantities(type);
			if (unit_quantities == null || unit_quantities.isEmpty()) {
				unit_dists[type.ordinal()].SetVisibility(false);
				continue;
			}
			int[] unit_counts = new int[10];
			int idx = 0;
			int type_total = 0;
			String tooltip_text = "";
			for (Entry<Unit, Long> kv : unit_quantities.entrySet()) {
				units[type.ordinal()] += kv.getValue();
				unit_counts[idx] = Integer.valueOf(String.valueOf(kv.getValue()));
				type_total += unit_counts[idx++];
				if (kv.getValue() > 0) {
					tooltip_text += (tooltip_text == "" ? "" : " / ") + kv.getKey().name;
				}
			}
			unit_dists[type.ordinal()].SetValues(unit_counts).SetTooltipText(tooltip_text);
			unit_dists[type.ordinal()].SetVisibility(true);
			total += type_total;
			max_total = Math.max(type_total, max_total);
		}
		for (BarElement unit_dist : unit_dists) {
			unit_dist.SetMax(max_total);
		}
		if (total > 0) {
			unit_types.SetMax(total).SetValues(units);
			unit_types.SetVisibility(true);
		} else {
			unit_types.SetVisibility(false);
		}
		soldier_number.SetNumber(total);
		training.SetValue((int) (army.GetTrainingLevel() * 100));
		morale.SetValue((int) (army.GetMorale() * 100));
		Person general_person = army.GetGeneral();
		if (general_person != null) {
			general.SetText(general_person.GetName()).SetClickCallback(() -> cmp.person.Show(general_person));
			general.SetUseHandCursor(true);
		} else {
			general.SetText(Texts.none).SetClickCallback(null);
			general.SetUseHandCursor(false);
		}
		if (!army.IsGarrison()) {
			for (int i = 0; i < Unit.kMaxUnitType; ++i) {
				for (int j = 0; j < ArmyData.kMaxSoldierTypes; ++j) {
					typed_soldiers[i][j].SetNumber(army.GetTypedSoldier(UnitType.values()[i], SoldierType.values()[j]));
					// typed_max_soldiers[i][j].SetText("/ " + StringUtil.LongNumber(army.GetMaxSoldierDist()[i][j]));
				}
			}
		}
		state.SetText(army.GetState().GetName());
		state.SetClickCallback(() -> cmp.state.Show(army.GetState()));
		
		base_city.SetText(army.GetBaseCity().GetName());
		base_city.SetClickCallback(() -> cmp.city.Show(army.GetBaseCity()));
		
		if (army.GetStatus() == Army.Status.IDLE) {
			target.SetText(Texts.baseCity);
		} else if (army.GetStatus() == Army.Status.RETREAT) {
			target.SetText(Texts.retreat).SetClickCallback(null);
			target.SetUseHandCursor(false);
		} else {
			Army target_army = army.GetTarget();
			if (target_army != null) {
				target.SetText(target_army.GetName()).SetClickCallback(() -> cmp.army.Show(target_army));
				target.SetUseHandCursor(true);
			} else {
				target.SetText("").SetClickCallback(null);
				target.SetUseHandCursor(false);
			}
		}
		
		current_pos.SetText(cmp.utils.army_util.GetPositionCity(army).GetName());
		current_pos.SetClickCallback(() -> cmp.battle_field.Show(army.GetPosition()));
		
		City log_city = army.GetLogistcalCity();
		if (log_city != null) {
			logistical_city.SetText(log_city.GetName());
			logistical_city.SetClickCallback(() -> cmp.city.Show(log_city));
			logistical_city.SetUseHandCursor(true);
			logistic_cost.SetNumber(army.GetLogisticCost());
		} else {
			logistical_city.SetText("");
			logistical_city.SetClickCallback(null);
			logistical_city.SetUseHandCursor(false);
			logistic_cost.SetText("");
		}
		food_consumption.SetNumber(cmp.utils.army_util.GetDailyFoodConsumption(army));
		
		if (log_city != null) {
			city_list.Resize(army.GetTotalSupportingCities());
			long total_labor = 0;
			for (int index = 0; index < army.GetTotalSupportingCities(); ++index) {
				City city = army.GetSupportingCity(index);
				long labor = army.GetCitySupportingLabor(city);
				city_list.SetValue(index, 0, city.GetName());
				city_list.SetValue(index, 1, StringUtil.LongNumber(labor));
				city_list.SetCallback(index, () -> cmp.city.Show(city));
				total_labor += labor;
			}
			double eff = (double)cmp.utils.army_util.GetEffectiveSoldierBySupply(
					army, army.GetLogisticCost(), total_labor) / army.GetTotalSoldier();
			efficiency.SetPercentage(eff, false, false);
		} else {
			city_list.Resize(0);
			efficiency.SetText("100%");
		}
	}

	@Override
	protected void Refresh() {
		Reset();
		repaint();
	}
	
	private class ArmyPanel extends GenericPanel {
		private static final long serialVersionUID = 1L;

		protected ArmyPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			this.setLayout(null);
			unit_types.SetLabels(Texts.unitType);
			AddVisualElement(unit_types);
			
			state.SetUseHandCursor(true);
			base_city.SetUseHandCursor(true);
			current_pos.SetUseHandCursor(true);
			
			AddVisualElement(soldier_number);
			AddVisualElement(training);
			AddVisualElement(morale);
			AddVisualElement(general);
			AddVisualElement(state);
			AddVisualElement(base_city);
			AddVisualElement(target);
			AddVisualElement(current_pos);
			
			
			assign_general.setBounds(150, 85, 15, 15);
			assign_general.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new PersonSelectionDialog(cmp.army, cmp, data, () -> GetArmy().GetState(), true, true, PersonSelectionDialog.AssignArmyGeneral(() -> GetArmy()));
				}
			});
			rebase.setBounds(305, 35, 15, 15);
			rebase.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new CitySelectionDialog(cmp.army, cmp, data, () -> GetArmy().GetState(), null, Ranker.POPULATION, city -> {
						Action action = new Action(ActionType.REBASE_ARMY);
						action.object = GetArmy();
						action.object2 = city;
						cmp.action_consumer.accept(action);
					});
				}
			});
			reset_target.setBounds(305, 60, 15, 15);
			reset_target.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Action action = new Action(ActionType.RESET_TARGET);
					action.object = GetArmy();
					cmp.action_consumer.accept(action);
				}
			});
			AddPlayerElement(assign_general);
			AddPlayerElement(rebase);
			AddPlayerElement(reset_target);
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);

			TextWriter text_writer = new TextWriter(g);
			text_writer.SetFontSize(12);
//			for (int i = 0; i < Unit.kMaxUnitType; ++i) {
//				text_writer.DrawString(5, 25 * i + 135, Texts.unitType[i]);
//			}
			text_writer.DrawString(5, 10, Texts.soldierNumber);
			text_writer.DrawString(5, 35, Texts.trainingLevel);
			text_writer.DrawString(5, 60, Texts.morale);
			text_writer.DrawString(5, 85, Texts.general);
			
			text_writer.DrawString(185, 10, Texts.state);
			text_writer.DrawString(185, 35, Texts.baseCity);
			text_writer.DrawString(185, 60, Texts.target);
			text_writer.DrawString(185, 85, Texts.position);
		}
	}
	
	private class UnitPanel extends GenericPanel {
		private static final long serialVersionUID = 1L;

		protected UnitPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			for (int i = 0; i < Unit.kMaxUnitType; ++i) {			
				unit_dists[i] = new BarElement(new Rectangle(45, 25 * i + 30, 375, 15), 100, colors);
				unit_dists[i].SetShowRemaining(false);
				AddVisualElement(unit_dists[i]);
			}
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);

			TextWriter text_writer = new TextWriter(g);
			text_writer.SetFontSize(12);
			for (int i = 0; i < Unit.kMaxUnitType; ++i) {
				text_writer.DrawString(5, 25 * i + 30, Texts.unitType[i]);
			}
		}
	}
	
	private class SoldierPanel extends GenericPanel {
		private static final long serialVersionUID = 1L;

		protected SoldierPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			for (int i = 0; i < Unit.kMaxUnitType; ++i) {
				for (int j = 0; j < ArmyData.kMaxSoldierTypes; ++j) {
					typed_soldiers[i][j] = new TextElement(new Rectangle(55 + j * 130, 25 * i + 30, 50, 15));
					typed_soldiers[i][j].SetHasFrame(false).SetFontSize(12);
					AddVisualElement(typed_soldiers[i][j]);
				}
			}
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);

			TextWriter text_writer = new TextWriter(g);
			text_writer.SetFontSize(12);
			for (int i = 0; i < Unit.kMaxUnitType; ++i) {
				text_writer.DrawString(5, 25 * i + 30, Texts.unitType[i]);
			}
			for (int j = 0; j < ArmyData.kMaxSoldierTypes; ++j) {
				text_writer.DrawString(55 + j * 130, 5, Texts.soldierType[j]);
			}
		}
	}
	
	private class LogisticPanel extends GenericPanel {
		private static final long serialVersionUID = 1L;

		protected LogisticPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			city_list = new ScrollListComponent(kColumnWidth, 20);
			city_list.SetColumnHeaders(kColumnNames);
			
			city_list.setBounds(240, 5, 180, 120);
			add(city_list);
			
			AddVisualElement(logistical_city);
			AddVisualElement(logistic_cost);
			AddVisualElement(efficiency);
			AddVisualElement(food_consumption);
			this.setLayout(null);
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			
			TextWriter text_writer = new TextWriter(g);
			text_writer.SetFontSize(12);
			text_writer.DrawString(5, 5, Texts.logistic + Texts.city);
			text_writer.DrawString(5, 35, Texts.logisticCost);
			text_writer.DrawString(5, 65, Texts.battleEfficiency);
			text_writer.DrawString(5,  95, Texts.food + Texts.expense);
		}
		
		private int[] kColumnWidth = { 60, 100};
		private String[] kColumnNames = { Texts.city, Texts.logisticLabor};
	}
	
	private class MarchPanel extends GenericPanel {
		protected MarchPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
		}

		private static final long serialVersionUID = 1L;
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);

			TextWriter text_writer = new TextWriter(g);
			text_writer.SetFontSize(12);
			int i = 0;
			for (Terrain terrain : Terrain.values()) {
				if (terrain == Terrain.HIGH_MOUNTAIN || terrain == Terrain.SEA) continue;
				text_writer.DrawString(5 + i * 50 , 25, Texts.terrains[terrain.ordinal()]);
				if (army.GetTotalSoldier() > 0) {
					text_writer.DrawString(5 + i * 50 , 50, String.valueOf(cmp.utils.army_util.GetTerrainTravelTime(army, terrain)));
				} else {
					text_writer.DrawString(5 + i * 50 , 50, "");
				}
				++i;
			}
		}
	}
	
	private Army GetArmy() {
		return army;
	}

	private GenericPanel top_panel;
	private JTabbedPane tabbed_panel = new JTabbedPane();
	private GenericPanel unit_panel;
	private GenericPanel soldier_panel;
	private GenericPanel logistic_panel;
	private GenericPanel march_panel;
	
	private Army army;
	private PieElement unit_types = new PieElement(new Rectangle(340, 5, 100, 100));
	private BarElement[] unit_dists = new BarElement[Unit.kMaxUnitType];
	private TextElement soldier_number = new TextElement(new Rectangle(45, 10, 100, 15));
	private RatioBarElement training = new RatioBarElement(new Rectangle(45, 35, 100, 15));
	private RatioBarElement morale = new RatioBarElement(new Rectangle(45, 60, 100, 15));
	private TextElement general = new TextElement(new Rectangle(45, 85, 100, 15));
	private TextElement state = new TextElement(new Rectangle(220, 10, 80, 15));
	private TextElement base_city = new TextElement(new Rectangle(220, 35, 80, 15));
	private TextElement target = new TextElement(new Rectangle(220, 60, 80, 15));
	private TextElement current_pos = new TextElement(new Rectangle(220, 85, 80, 15));
	
	private TextElement logistical_city = new TextElement(new Rectangle(75, 5, 90, 15));
	private TextElement logistic_cost = new TextElement(new Rectangle(75, 35, 90, 15));
	private TextElement efficiency = new TextElement(new Rectangle(75, 65, 90, 15));
	private TextElement food_consumption = new TextElement(new Rectangle(75, 95, 90, 15));
	
	private TextElement[][] typed_soldiers = new TextElement[Unit.kMaxUnitType][ArmyData.kMaxSoldierTypes];
	//private TextElement[][] typed_max_soldiers = new TextElement[Unit.kMaxUnitType][ArmyData.kMaxSoldierTypes];
	private ScrollListComponent city_list;
	
	
	// Player Element
	private JButton assign_general = new JButton(Texts.settingIcon);
	private JButton rebase = new JButton(Texts.settingIcon);
	private JButton reset_target = new JButton(Texts.stopIcon);
	
	private static Color[] colors = { Color.LIGHT_GRAY, Colors.LIGHTEST_GREY, Color.WHITE, Colors.LIGHTER_YELLOW, Colors.LIGHTER_GREEN,
			Colors.LIGHT_GREEN, Colors.LIGHT_BLUE, Colors.LIGHTER_BLUE, Color.PINK, Colors.RED_ORANGE };
}
