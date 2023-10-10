package ac.ui.swing.panel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JScrollPane;

import ac.data.base.Resource;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Improvement;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Improvement.SpecialImprovementType;
import ac.data.constant.Policies.Policy;
import ac.data.constant.Technology;
import ac.data.constant.Technology.TechnologyType;
import ac.data.constant.Texts;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.GenericPanel;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.RatioBarElement;
import ac.ui.swing.util.BarDrawer;
import ac.ui.swing.util.ShapeDrawer;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;

public class StateTechPanel extends TypedDataPanel<State> {
	private static final long serialVersionUID = 1L;
	public StateTechPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		setLayout(null);
		
		for (TechnologyType type : TechnologyType.values()) {
			ArrayList<Technology> techs = data.GetConstData().typed_techs.get(type);
			kMaxNumTechs = Math.max(kMaxNumTechs, techs.size());
		}
		
		tech_panel = new TechPanel(cmp, data);
		tech_panel.setPreferredSize(new Dimension(420, kMaxNumTechs * kHeight + 2 * kOffset));
		tech_panel.revalidate();
		JScrollPane pane = new JScrollPane(tech_panel);
		pane.setBounds(10, 10, 440, 250);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		pane.revalidate();
		pane.repaint();
		
		add(pane);
	}

	public void Reset(State state) {		
		tech_panel.Reset(state);
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);
		tech_panel.repaint();
	}
	
	private String Description(Technology tech) {
		String ret = "";
		if (tech.agriculture_boost > 0) {
			ret += String.format(Texts.technologyAgricultureBoost, (int)(tech.agriculture_boost * 100)) + "\n";
		}
		if (tech.commerce_boost > 0) {
			ret += String.format(Texts.technologyCommerceBoost, (int)(tech.commerce_boost * 100)) + "\n";
		}
		if (tech.tax_boost > 0) {
			ret += String.format(Texts.technologyTaxBoost, (int)(tech.tax_boost * 100)) + "\n";
		}
		if (tech.county_bonus > 0) {
			ret += String.format(Texts.technologyCountyBonus, tech.county_bonus) + "\n";
		}
		if (tech.improvement != null && tech.improvement != ImprovementType.NONE) {
			ret += String.format(Texts.technologyUnblockImprovement, Texts.improvements[tech.improvement.ordinal()]) + "\n";
		}
		switch (tech.effect) {
		case NONE:
			break;
		case UNBLOCK_AQEDUCT_SPECIAL_IMPROVEMENT:
			ArrayList<String> unblocked_improvements = new ArrayList<String>();
			for (SpecialImprovementType type : Improvement.kAqeductSpecialImprovements) {
				unblocked_improvements.add(Texts.specialImprovements[type.ordinal()]);
			}
			ret += String.format(Texts.technologyUnblockImprovement, String.join(",", unblocked_improvements)) + "\n";
			break;
		case UNBLOCK_GREATWALL:
			ret += String.format(Texts.technologyUnblockImprovement, Texts.specialImprovements[SpecialImprovementType.GREATWALL.ordinal()]) + "\n";
			break;
		case UNBLOCK_TAX_RATE_CHANGE:
			ret += String.format(Texts.technologyUnblock, Texts.changeTaxRate) + "\n";
			break;
		case UNBLOCK_GARRISON:
			ret += Texts.technologyUnblockGarrison + "\n";
			break;
		case UNLIMIT_WORKER:
			ret += Texts.technologyUnlimitWorker + "\n";
			break;
		case HALVE_DISTANCE_PENALTY_FOR_COUNTY:
			ret += Texts.technologyHalveDistancePenalty + "\n";
			break;
		case BOOST_SALT_IRON:
			ret += Texts.technologyBoostSaltIron + "\n";
			break;
		case BOOST_HORSE:
			ret += Texts.technologyBoostHorse + "\n";
			break;
		case BOOST_SILK:
			ret += Texts.technologyBoostSilk + "\n";
			break;
		case BOOST_FISH:
			ret += Texts.technologyBoostFish + "\n";
			break;
		case BOOST_MINE:
			ret += Texts.technologyBoostMine + "\n";
			break;
		case BOOST_CHINA:
			ret += Texts.technologyBoostChina + "\n";
			break;
		case REDUCE_BARBARIAN_PROB:
			ret += Texts.technologyReduceBarbarianProb + "\n";
			break;
		case REDUCE_LOCUST_DAMAGE:
			ret += Texts.technologyReduceLocustDamage + "\n";
			break;
		case BOOST_CONSCRIPTION_COMBAT_POWER:
			ret += Texts.technologyBoostConscriptionCombatPower + "\n";
			break;
		case REDUCE_LOGISTIC_LABOR:
			ret += Texts.technologyReduceLogisticLabor + "\n";
			break;
		case BOOST_FOREIGNER_CONVERSION:
			ret += Texts.technologyBoostForeignerConversion + "\n";
			break;
		default:
		}
		if (tech.policy != null && tech.policy != Policy.NONE) {
			ret += String.format(Texts.technologyUnblockPolicy, Texts.policies[tech.policy.ordinal()]) + "\n";
		}
		if (tech.ideology != null && tech.ideology != Ideology.NONE) {
			ret += String.format(Texts.technologyUnblockIdeology, Texts.ideologies[tech.ideology.ordinal()]) + "\n";
		}
		if (tech.unit >= 0) {
			ret += String.format(Texts.technologyUnblockUnit, data.GetConstData().units.get(tech.unit).name);
		}
		if (tech.fubing > 0) {
			ret += String.format(Texts.technologyFubingBoost, tech.fubing * 100);
		}
		if (tech.recruitment > 0) {
			ret += String.format(Texts.technologyRecruitmentBoost, tech.recruitment * 100);
		}
		return ret;
	}

	private class TechPanel extends GenericPanel {
		private static final long serialVersionUID = 1L;
		protected TechPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			setLayout(null);
			
			current_tech_bar = new RatioBarElement(null);
			AddVisualElement(current_tech_bar);
			
			
			for (int i = 0; i < TechnologyType.values().length; ++i) {
				choose_tech_type[i] = new JButton(Texts.yes);
				choose_tech_type[i].setBounds(kOffset + i * kWidth + kFrameWidth / 2 + 30, kOffset + 8, 15, 15);
				choose_tech_type[i].addActionListener(new ChooseTechTypeListener(i));
				AddPlayerElement(choose_tech_type[i]);
			}
		}
		
		private class ChooseTechTypeListener implements ActionListener {
			public ChooseTechTypeListener(int i) {
				index = i;
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Action action = new Action(ActionType.RESEARCH_TECHNOLOGY_TYPE);
				action.object = GetData();
				action.quantity = index;
				cmp.action_consumer.accept(action);
				cmp.Repaint();
			}
			
			private int index;
		}
		
		public void Reset(State state) {
			int i = state.GetTechnology().GetResearchingTechnologyType().ordinal();
			Technology tech = state.GetTechnology().GetResearchingTechnology();
			if (tech != null) {
				int j = state.Get().technologies.obtained[i] + 1;
				int x_offset = kOffset + i * kWidth;
				int y_offset = kOffset + j * kHeight + kHeightGap;
				long cost = state.GetTechnology().GetResearchingTechnology().cost;
				// long cost = data.GetParam().GetTechnologyCost(Technologies.typed_technology[i][j + 1]);
				current_tech_bar.Resize(new Rectangle(x_offset, y_offset + kFrameHeight - 15, kFrameWidth, 15));
				current_tech_bar.SetMax(cost).SetValue(state.GetTechnology().GetResearchProgress());
				Resource<Long> total_yields = cmp.utils.state_util.GetMonthlyProduce(state);
	//			cmp.utils.state_util.SubstractMilitaryBudgetIncrease(state, total_yields);
				Resource<Long> export = cmp.utils.state_util.GetExportResources(state, total_yields);
				long income = cmp.utils.state_util.GetNetIncome(state, total_yields, export, cmp.utils.state_util.GetExpense(state));
				int tech_boost = cmp.utils.state_util.GetTechBoost(cmp.utils.state_util.GetTechBudget(
						state, income), cmp.utils.state_util.GetTechMultiplier(state));
				current_tech_bar.SetAddition(tech_boost);
				current_tech_bar.SetVisibility(true);
			} else {
				current_tech_bar.SetVisibility(false);
			}
			SetVisibilityForPlayerElement(state == data.GetPlayer().GetState());
		}

		public void paintComponent(Graphics g)  {		
			super.paintComponent(g);
			State state = GetData();
			if (state == null) return;
			
			ShapeDrawer shape_drawer = new ShapeDrawer(g);
			TextWriter text_writer = new TextWriter(g);
			BarDrawer bar_drawer = new BarDrawer(g);
			for (int i = 0; i < TechnologyType.values().length; ++i) {
				int x_offset = kOffset + i * kWidth;
				int x_center = x_offset + kFrameWidth / 2;
				text_writer.SetAlignment(Alignment.CENTER).SetFontSize(16).SetBold(true);
				text_writer.DrawString(x_center, kOffset + 15, Texts.technologyTypeNames[i]);
				ArrayList<Technology> techs = data.GetConstData().typed_techs.get(TechnologyType.values()[i]);
				for (int j = 0; j < kMaxNumTechs; j++) {	
					if (j >= techs.size()) continue;
					Technology tech = techs.get(j);
					
					int y_offset = kOffset + j * kHeight + kHeightGap;
					
					Rectangle rect = new Rectangle(x_offset, y_offset, kFrameWidth, kFrameHeight);
					shape_drawer.DrawField(rect, null);
					shape_drawer.DrawLine(x_offset, y_offset + 20 ,kFrameWidth, 0);
					text_writer.SetAlignment(Alignment.CENTER).SetFontSize(14).SetBold(false);
					text_writer.DrawString(x_center, y_offset + 10, tech.name);
				
					text_writer.SetAlignment(Alignment.LEFT);
					text_writer.SetFontSize(12);
					text_writer.DrawMultiLine(x_offset + 2, y_offset + 30, 20, kFrameWidth - 2, Description(tech));
				
					Rectangle area = new Rectangle(x_offset, y_offset + kFrameHeight - 15, kFrameWidth, 15);
					if (state.Get().technologies.obtained[i] >= j) {
						bar_drawer.DrawBarWithText(area, 1, Texts.yes);
					} else if (state.Get().technologies.obtained[i] == j - 1 &&
							state.GetTechnology().GetResearchingTechnologyType().ordinal() == i) {
//						bar_drawer.DrawRatio(area, state.GetTechnology().GetResearchProgress(), 100);
					} else {
						bar_drawer.DrawBarWithText(area, 0, "");
					}

					if (j + 1 < techs.size()) {
						shape_drawer.DrawArrow(x_center, y_offset + kFrameHeight + kOffset * 2,
								x_center, y_offset + kFrameHeight + kHeightGap - kOffset);
					}
				}
			}
		}
		
		private RatioBarElement current_tech_bar;
		
		// Player Element
		private JButton[] choose_tech_type = new JButton[TechnologyType.values().length];
	};
	
	private TechPanel tech_panel;
	
	private static final int kOffset = 5;
	private static final int kWidth = 140;
	private static final int kHeight = 160;
	private static final int kHeightGap = 40;
	private static final int kFrameWidth = kWidth - 2 * kOffset;
	private static final int kFrameHeight = kHeight - 2 * kOffset - kHeightGap;
	private int kMaxNumTechs;
//	private State state;
	
}
