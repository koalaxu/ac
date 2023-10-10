package ac.ui.swing.dialog;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import ac.data.ArmyData;
import ac.data.ArmyData.SoldierType;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.ConstStateData;
import ac.data.constant.Ideologies;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Ideologies.IdeologyType;
import ac.data.constant.Policies;
import ac.data.constant.Policies.Policy;
import ac.data.constant.Texts;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.IdKeyedData;
import ac.engine.data.State;
import ac.engine.data.City.CityType;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;
import ac.ui.swing.GenericPanel;
import ac.ui.swing.dialog.CitySelectionDialog.Ranker;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.ShapeDrawer;
import ac.ui.swing.util.TextWriter;
import ac.util.StringUtil;

public class PolicyDialog extends StateDialog {

	private static final long serialVersionUID = 1L;

	public PolicyDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<State> state_getter, AbilityType type) {
		super(parent, cmp, data, state_getter, Texts.select + Texts.policy, 640, 360, true);
		this.type = type;
		
		ArrayList<Policy> policies = Policies.policies.get(type);
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < policies.size(); ++i) {
			Policy policy = policies.get(i);
			JRadioButton option = new JRadioButton(String.format("%s (%d)", Texts.policies[policy.ordinal()], Policies.GetCost(policy)));
			option.setBounds(5, 35 + i * 25, 145, 20);
			option.addActionListener(new ChangePolicy());
			add(option);
			group.add(option);
			policy_options.add(option);
		}
		
		current_param_panel = new NonParamPanel(cmp, data);
		add(current_param_panel);
		
		auto_renew.setBounds(160, 335, 200, 20);
		auto_renew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				data.GetPlayer().SetPolicyAutoRenew(type, auto_renew.isSelected());
			}
		});
		add(auto_renew);
		
		InitDone();
		Refresh();
	}

	@Override
	protected void Refresh() {
		current_param_panel.Refresh();
		this.EnableConfirmButton(ValidatePolicy());
		auto_renew.setSelected(data.GetPlayer().DoesPolicyAutoRenew(type));
		ArrayList<Policy> policies = Policies.policies.get(type);
		for (int i = 0; i < policies.size(); ++i) { 
			policy_options.get(i).setEnabled(cmp.utils.state_util.IsPolicyAvailable(state, policies.get(i)));
		}
	}

	@Override
	protected void Confirm() {
		Policy policy = GetPolicy();
		if (policy == null) return;
		Action action = new Action(ActionType.ADOPT_POLICY);
		action.object = state;
		action.quantity = policy.ordinal();
		action.object2 = current_param_panel.GetObject1();
		action.object3 = current_param_panel.GetObject2();
		action.quantity2 = current_param_panel.GetContextQuantity();
		cmp.action_consumer.accept(action);
		data.GetPlayer().SetPolicyAutoRenew(type, auto_renew.isSelected());
	}
	
	private Policy GetPolicy() {
		ArrayList<Policy> policies = Policies.policies.get(type);
		for (int i = 0; i < policies.size(); ++i) { 
			if (policy_options.get(i).isSelected()) {
				return policies.get(i);
			}
		}
		return null;
	}
	
	private boolean ValidatePolicy() {
		Policy policy = GetPolicy();
		if (policy == null) return false;
		if (policy == Policy.ESTABLISH_COUNTY) {
			if (current_param_panel.GetObject1() == null || ((City)current_param_panel.GetObject1()).GetType() == CityType.CAPITAL) return false;
			if (!cmp.utils.state_util.IsNewCountyAllowed(state)) return false;
		} else if (policy == Policy.ESTABLISH_JIMI_COUNTY) {
			if (current_param_panel.GetObject1() == null || ((City)current_param_panel.GetObject1()).GetType() != CityType.NONE) return false;
		} else if (policy == Policy.CHANGE_CAPITAL) {
			if (current_param_panel.GetObject1() == null || ((City)current_param_panel.GetObject1()).GetType() == CityType.CAPITAL) return false;
		} else if (Policies.GetType(policy) == AbilityType.DIPLOMACY) {
			if (current_param_panel.GetObject1() == null) return false;
			if (policy == Policy.DECREASE_RELATIONSHIP && (
					current_param_panel.GetObject2() == null || current_param_panel.GetObject1() == current_param_panel.GetObject2())) return false;
			if (!cmp.utils.diplomacy_util.ValidateDiplomacyPolicy(policy, state, current_param_panel.GetObject1())) return false;
		} else if (policy == Policy.ADOPT_IDEOLOGY) {
			if (current_param_panel.GetContextQuantity() < 0) return false;
		} else if (policy == Policy.MIGRATE) {
			if (!cmp.utils.pop_util.IsCityMigrationAffordable((City)current_param_panel.GetObject1(), (City)current_param_panel.GetObject2(),
					current_param_panel.GetContextQuantity())) return false;
		}
		return true;
	}
	
	private class ChangePolicy implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (GetPolicy()) {
			case CONVERT_RECRUITMENT_SOLDIERS_TO_CONSCRIPTION:
			case CONVERT_RECRUITMENT_SOLDIERS_TO_FUBING:
			case CONVERT_CONSCRIPTION_SOLDIERS_TO_FUBING:
			case CONVERT_CONSCRIPTION_SOLDIERS_TO_RECRUITMENT:
				SetPolicyParamPanel(new ArmyPanel(cmp, data));
				return;
			case CEASE_ALLIANCE:
			case CEASE_ALLY:
			case CEASE_OPEN_BORDER:
			case CEASE_SUZERAINTY:
			case CEASE_VASSAL:
			case DENOUNCE:
			case INCREASE_RELATIONSHIP:
			case PROPOSE_ALLIANCE:
			case PROPOSE_ALLY:
			case PROPOSE_OPEN_BORDER:
			case PROPOSE_SUZERAINTY:
			case PROPOSE_VASSAL:
				SetPolicyParamPanel(new StatePanel(cmp, data));
				return;
			case DECREASE_RELATIONSHIP:
				SetPolicyParamPanel(new TwoStatePanel(cmp, data));
				return;
			case INCREASE_HAPPINESS:
				SetPolicyParamPanel(new CityPanel(cmp, data, Ranker.HAPPINESS));
				return;
			case SUPPRESS_REVOLT:
				SetPolicyParamPanel(new CityPanel(cmp, data, Ranker.REVOLT));
				return;
			case CONVERT_FOREIGNERS:
				SetPolicyParamPanel(new CityPanel(cmp, data, Ranker.FOREIGNER));
				return;
			case ESTABLISH_COUNTY:
			case CHANGE_CAPITAL:
				SetPolicyParamPanel(new CityPanel(cmp, data, Ranker.POPULATION));
				return;
			case ESTABLISH_JIMI_COUNTY:
				SetPolicyParamPanel(new CityPanel(cmp, data, Ranker.FOREIGNER));
				return; 
			case ADOPT_IDEOLOGY:
				SetPolicyParamPanel(new IdeologyPanel(cmp, data));
				return;
			case MIGRATE:
				SetPolicyParamPanel(new MigrationPanel(cmp, data));
				return;
			case MILITARY_TRAINING:
			case INCREASE_STABILITY:
			case INCREASE_PRESTIGE:
			case NONE:
			default:
				break;
			}
			SetPolicyParamPanel(new NonParamPanel(cmp, data));
		}
	}
	
	private void SetPolicyParamPanel(PolicyParamPanel new_panel) {
		remove(current_param_panel);
		add(new_panel);
		current_param_panel = new_panel;
		Refresh();
		revalidate();
		repaint();
	}
	
	private abstract class PolicyParamPanel extends GenericPanel {
		private static final long serialVersionUID = 1L;
		protected PolicyParamPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			setLayout(null);
			setBounds(160, 30, 420, 295);
		}
		
		protected abstract void Refresh();
		protected IdKeyedData GetObject1() {  return null;  }
		protected IdKeyedData GetObject2() {  return null;  }
		protected long GetContextQuantity() {  return -1;  }
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			ShapeDrawer drawer = new ShapeDrawer(g);
			drawer.DrawField(new Rectangle(0, 5, 400, 290), null);
		}
	}
	
	private class NonParamPanel extends PolicyParamPanel {
		private static final long serialVersionUID = 1L;
		protected NonParamPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
		}
		@Override
		protected void Refresh() {
		}
	}
	
	private class ArmyPanel extends PolicyParamPanel {
		private static final long serialVersionUID = 1L;
		protected ArmyPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			ButtonGroup group0 = new ButtonGroup();
			for (int i = 0; i < ArmyData.kMaxSoldierTypes; ++i) {
				JLabel label = new JLabel(Texts.soldierType[i]);
				label.setBounds(150 + i * 80, 10, 80, 20);
				add(label);
			}
			for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
				armies[i] = new JRadioButton();
				armies[i].setBounds(10, 40 + i * 30, 120, 20);
				add(armies[i]);
				group0.add(armies[i]);
				
				for (int j = 0; j < ArmyData.kMaxSoldierTypes; ++j) {
					army_soldiers[i][j] = new JLabel();
					army_soldiers[i][j].setBounds(150 + j * 80, 40 + i * 30, 80, 20);
					add(army_soldiers[i][j]);
				}
			}
			armies[0].setSelected(true);
		}
		@Override
		protected void Refresh() {
			ArrayList<Army> army_list = state.GetMilitary().GetArmies();
			for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
				if (i < army_list.size()) {
					Army army = army_list.get(i);
					armies[i].setText(army_list.get(i).GetName());
					armies[i].setEnabled(true);
					for (int j = 0; j < ArmyData.kMaxSoldierTypes; ++j) {
						army_soldiers[i][j].setText(StringUtil.LongNumber(army.GetTypedSoldier(SoldierType.values()[i])));
					}
				} else {
					armies[i].setText("");
					armies[i].setEnabled(false);
					if (armies[i].isSelected()) armies[0].setSelected(true);
					for (int j = 0; j < ArmyData.kMaxSoldierTypes; ++j) {
						army_soldiers[i][j].setText("");
					}
				}
			}
		}
		protected IdKeyedData GetObject1() {
			for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
				if (armies[i].isSelected()) return state.GetMilitary().GetArmies().get(i);
			}
			return null;
		}
		
		private JRadioButton[] armies = new JRadioButton[ConstStateData.kMaxArmies];
		private JLabel[][] army_soldiers = new JLabel[ConstStateData.kMaxArmies][ArmyData.kMaxSoldierTypes];
	}
	
	private class CityPanel extends PolicyParamPanel {
		private static final long serialVersionUID = 1L;
		protected CityPanel(Components cmp, DataAccessor data, Ranker ranker) {
			super(cmp, data);
			this.ranker = ranker;

			value.SetUseHandCursor(true);
			AddVisualElement(value);
			AddVisualElement(attribute);
		}

		@Override
		protected void Refresh() {
			if (city == null || city.GetOwner() != state) {
				city = state.GetCapital();
			}
			value.SetText(city.GetName());
			value.SetClickCallback(CreateCitySelector(null, ranker, output -> city = output));
			attribute.SetNumber((long)CitySelectionDialog.GetRankerValue(city, ranker));
		}
		
		protected IdKeyedData GetObject1() {
			return city;
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			TextWriter writer = new TextWriter(g);
			writer.SetFontSize(14);
			writer.DrawString(30, 15, Texts.city + ":");
			writer.DrawString(30, 45, CitySelectionDialog.GetRankerColumn(ranker) + ":");
		}
		
		private City city = null;
		private Ranker ranker;
		private TextElement value = new TextElement(new Rectangle(120, 15, 100, 18));
		private TextElement attribute = new TextElement(new Rectangle(120, 45, 100, 18));
	}
	
	private class StatePanel extends PolicyParamPanel {
		private static final long serialVersionUID = 1L;
		protected StatePanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			value.SetUseHandCursor(true);
			value.SetClickCallback(CreateStateSelector(output -> target = output));
			AddVisualElement(value);
			AddVisualElement(attitude);
			AddVisualElement(relationship);
		}

		@Override
		protected void Refresh() {
			if (target != null && !target.Playable()) {
				target = null;
			}
			if (target != null) {
				value.SetText(target.GetName());
				attitude.SetNumber(state.GetDiplomacy().GetAttitude(target));
				relationship.SetText(GetRelationship(target));
				if (cmp.utils.diplomacy_util.AreAlliance(state, target)) {
					relationship.SetText(Texts.alliance);
				} else if (state.GetDiplomacy().GetSuzerainty() == target) {
					relationship.SetText(Texts.suzerainty);
				} else if (target.GetDiplomacy().GetSuzerainty() == state) {
					relationship.SetText(Texts.vassal);
				} else if (cmp.utils.diplomacy_util.AreAlly(state, target)) {
					relationship.SetText(Texts.ally);
				} else {
					relationship.SetText(Texts.none);
				}
			} else {
				value.SetText("");
				attitude.SetText("");
				relationship.SetText("");
			}
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			TextWriter writer = new TextWriter(g);
			writer.SetFontSize(14);
			writer.DrawString(30, 15, Texts.state + ":");
			writer.DrawString(30, 45, Texts.attitude + ":");
			writer.DrawString(30, 75, Texts.relationship + ":");
		}
		
		protected IdKeyedData GetObject1() {
			return target;
		}
		
		protected String GetRelationship(State target) {
			if (cmp.utils.diplomacy_util.AreAlliance(state, target)) {
				return Texts.alliance;
			} else if (state.GetDiplomacy().GetSuzerainty() == target) {
				return Texts.suzerainty;
			} else if (target.GetDiplomacy().GetSuzerainty() == state) {
				return Texts.vassal;
			} else if (cmp.utils.diplomacy_util.AreAlly(state, target)) {
				return Texts.ally;
			}
			return Texts.none;
		}
		
		private State target = null;
		private TextElement value = new TextElement(new Rectangle(120, 15, 100, 18));
		private TextElement attitude = new TextElement(new Rectangle(120, 45, 100, 18));
		private TextElement relationship = new TextElement(new Rectangle(120, 75, 100, 18));
	}
	
	private class TwoStatePanel extends StatePanel {
		private static final long serialVersionUID = 1L;
		protected TwoStatePanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			value.SetUseHandCursor(true);
			value.SetClickCallback(CreateStateSelector(output -> target = output));
			AddVisualElement(value);
			AddVisualElement(attitude);
			AddVisualElement(relationship);
		}

		@Override
		protected void Refresh() {
			super.Refresh();
			if (target != null && !target.Playable()) {
				target = null;
			}
			if (target != null) {
				value.SetText(target.GetName());
				attitude.SetNumber(state.GetDiplomacy().GetAttitude(target));
				relationship.SetText(GetRelationship(target));
				if (cmp.utils.diplomacy_util.AreAlliance(state, target)) {
					relationship.SetText(Texts.alliance);
				} else if (state.GetDiplomacy().GetSuzerainty() == target) {
					relationship.SetText(Texts.suzerainty);
				} else if (target.GetDiplomacy().GetSuzerainty() == state) {
					relationship.SetText(Texts.vassal);
				} else if (cmp.utils.diplomacy_util.AreAlly(state, target)) {
					relationship.SetText(Texts.ally);
				} else {
					relationship.SetText(Texts.none);
				}
			} else {
				value.SetText("");
				attitude.SetText("");
				relationship.SetText("");
			}
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			TextWriter writer = new TextWriter(g);
			writer.SetFontSize(14);
			writer.DrawString(30, 110, Texts.state + ":");
			writer.DrawString(30, 140, Texts.attitude + ":");
			writer.DrawString(30, 170, Texts.relationship + ":");
		}
		
		protected IdKeyedData GetObject2() {
			return target;
		}
		
		private State target = null;
		private TextElement value = new TextElement(new Rectangle(120, 110, 100, 18));
		private TextElement attitude = new TextElement(new Rectangle(120, 140, 100, 18));
		private TextElement relationship = new TextElement(new Rectangle(120, 170, 100, 18));
	}
	
	private class IdeologyPanel extends PolicyParamPanel {
		private static final long serialVersionUID = 1L;
		protected IdeologyPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			ButtonGroup group = new ButtonGroup();
			for (int i = 0; i < Ideology.values().length; ++i) {
				options.add(new JRadioButton(Texts.ideologies[i]));
				options.get(i).setToolTipText(StringUtil.ConvertToHTML(Texts.ideologyDescription[i]));
				group.add(options.get(i));
				if (i == 0) continue;
				add(options.get(i));
				options.get(i).addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						EnableConfirmButton(ValidatePolicy());
					}
				});
			}
			for (IdeologyType type : IdeologyType.values()) {
				TreeSet<Ideology> list = Ideologies.typed_ideologies.get(type);
				int i = 0;
				for (Ideology ideology : list) {
					if (ideology == Ideology.NONE) continue;
					JRadioButton option = options.get(ideology.ordinal());
					option.setBounds(type.ordinal() * 140 + 10, i * 30 + 10, 100, 20);
					i++;
					
				}
			}
		}
		@Override
		protected void Refresh() {
			for (int i = 0; i < Ideology.values().length; ++i) {
				options.get(i).setEnabled(cmp.utils.state_util.IsIdeologyAvailable(state, Ideology.values()[i]));
			}
		}
		
		@Override
		protected long GetContextQuantity() {
			for (int i = 1; i < options.size(); ++i) {
				if (options.get(i).isSelected()) return i;
			}
			return -1;
		}
		
		private ArrayList<JRadioButton> options = new ArrayList<JRadioButton>();
	}
	
	private class MigrationPanel extends PolicyParamPanel {
		private static final long serialVersionUID = 1L;
		protected MigrationPanel(Components cmp, DataAccessor data) {
			super(cmp, data);

			from_value.SetUseHandCursor(true);
			AddVisualElement(from_value);
			AddVisualElement(from_attribute);
			to_value.SetUseHandCursor(true);
			AddVisualElement(to_value);
			AddVisualElement(to_attribute);
			migrated.SetUseHandCursor(true);
			AddVisualElement(migrated);
		}

		@Override
		protected void Refresh() {
			if (from != null) {
				from_value.SetText(from.GetName());
				from_attribute.SetNumber(from.GetTotalPopulation());
			} else {
				from_value.SetText("");
				from_attribute.SetText("");
			}
			if (to != null) {
				to_value.SetText(to.GetName());
				to_attribute.SetNumber(to.GetTotalPopulation());
			} else {
				to_value.SetText("");
				to_attribute.SetText("");
			}
			from_value.SetClickCallback(CreateCitySelector(null, Ranker.POPULATION, output -> from = output));
			to_value.SetClickCallback(CreateCitySelector(city -> city != from && city.GetOwner() == state && city.GetTransportation(from) != Integer.MAX_VALUE,
					Ranker.POPULATION, output -> to = output));
			migrated.SetNumber(quantity);
			migrated.SetClickCallback(() -> new NumberDialog(policy_dialog, cmp, data, () -> state, Texts.select + Texts.population + Texts.quantity, Texts.population, 0,
					() -> (int)Math.min(data.GetParam().max_migration_quantity, from.GetTotalPopulation() - data.GetParam().city_min_population), output -> quantity = output));
		}
		
		protected IdKeyedData GetObject1() {
			return from;
		}
		
		protected IdKeyedData GetObject2() {
			return to;
		}
		
		protected long GetContextQuantity() {
			return quantity;
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			TextWriter writer = new TextWriter(g);
			writer.SetFontSize(14);
			writer.DrawString(30, 15, Texts.fromCity + ":");
			writer.DrawString(30, 45, Texts.population + ":");
			writer.DrawString(30, 80, Texts.toCity + ":");
			writer.DrawString(30, 110, Texts.population + ":");
			writer.DrawString(30, 145, Texts.quantity + ":");
		}
		
		private City from = null;
		private City to = null;
		private long quantity;
		private TextElement from_value = new TextElement(new Rectangle(120, 15, 100, 18));
		private TextElement from_attribute = new TextElement(new Rectangle(120, 45, 100, 18));
		private TextElement to_value = new TextElement(new Rectangle(120, 80, 100, 18));
		private TextElement to_attribute = new TextElement(new Rectangle(120, 110, 100, 18));
		private TextElement migrated = new TextElement(new Rectangle(120, 145, 100, 18));
	}
	
	private Runnable CreateCitySelector(Predicate<City> filter, Ranker ranker, Consumer<City> func) {
		return () -> {
			new CitySelectionDialog(this, cmp, data, () -> state, filter, ranker, func);
		};
	}
	
	private Runnable CreateStateSelector(Consumer<State> func) {
		return () -> {
			new StateSelectionDialog(this, cmp, data, () -> state, func);
		};
	}

	private AbilityType type;
	private ArrayList<JRadioButton> policy_options = new ArrayList<JRadioButton>();
	private PolicyParamPanel current_param_panel;
	private JCheckBox auto_renew = new JCheckBox(Texts.autoRenew);
	private PolicyDialog policy_dialog = this;
}
