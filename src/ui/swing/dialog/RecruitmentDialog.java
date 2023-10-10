package ac.ui.swing.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.function.Supplier;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import ac.data.ArmyData;
import ac.data.ArmyData.SoldierType;
import ac.data.constant.ConstStateData;
import ac.data.constant.Texts;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.CityMilitary.RecruitmentInfo;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.util.StringUtil;

public class RecruitmentDialog extends CityDialog {
	private static final long serialVersionUID = 1L;
	public RecruitmentDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<City> city_getter) {
		super(parent, cmp, data, city_getter, Texts.recruitment, 600, 400, false);
	
		JLabel city_label = new JLabel(Texts.city + ": ");
		JLabel city_pop_label = new JLabel(Texts.recruitable + Texts.population + ": ");
		city_label.setBounds(10, 40, 40, 20);
		city_name.setBounds(60, 40, 60, 20);
		city_pop_label.setBounds(140, 40, 80, 20);
		city_pop.setBounds(230, 40, 80, 20);
		city_soldiers_in_queue.setBounds(320, 40, 80, 20);
		add(city_label);
		add(city_name);
		add(city_pop_label);
		add(city_pop);
		add(city_soldiers_in_queue);
		
		ButtonGroup group0 = new ButtonGroup();
		for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
			armies[i] = new JRadioButton();
			armies[i].setBounds(10, 80 + i * 30, 120, 20);
			add(armies[i]);
			group0.add(armies[i]);
			
			army_soldiers[i] = new JLabel();
			army_soldiers[i].setBounds(150, 80 + i * 30, 120, 20);
			add(army_soldiers[i]);
		}
		armies[0].setSelected(true);
		
		ButtonGroup group1 = new ButtonGroup();
		for (int i = 0; i < ArmyData.kMaxSoldierTypes; ++i) {
			soldier_types[i] = new JRadioButton(Texts.soldierType[i]);
			soldier_types[i].setBounds(10, 280 + i * 30, 160, 20);
			add(soldier_types[i]);
			group1.add(soldier_types[i]);
		}
		soldier_types[0].setSelected(true);
		
		for (int i = 0; i < Unit.kMaxUnitType; ++i) {
			ArrayList<Unit> units = data.GetConstData().typed_units.get(i);
			unit_buttons[i] = new JButton[units.size()];
			for (int j = 0; j < units.size(); ++j) {
				unit_buttons[i][j] = new JButton(units.get(j).name);
				unit_buttons[i][j].setBounds(280 + i * 80, 80 + j * 30, 70, 20);
				unit_buttons[i][j].addActionListener(new RecruitButtonClick(data.GetConstData().typed_units.get(i).get(j)));
				add(unit_buttons[i][j]);
			}
		}
		
		InitDone();
		Refresh();
	}
	
	private class RecruitButtonClick implements ActionListener {
		private RecruitButtonClick(Unit unit) {
			this.unit = unit;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Army army = null;
			for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
				if (armies[i].isSelected()) {
					army = city.GetOwner().GetMilitary().GetArmies().get(i);
					break;
				}
			}
			SoldierType type = null;
			for (int i = 0; i < ArmyData.kMaxSoldierTypes; ++i) {
				if (soldier_types[i].isSelected()) {
					type = SoldierType.values()[i];
					break;
				}
			}
			data.GetPlayer().AppendRecruitment(city, army, unit, type);
			cmp.Repaint();
		}
		
		private Unit unit;
	}

	@Override
	protected void Refresh() {
		State state = city.GetOwner();
		city_name.setText(city.GetName());
		city_pop.setText(StringUtil.LongNumber(cmp.utils.city_util.GetAvailableSoldierCandidate(city)));
		ArrayList<RecruitmentInfo> recruitment_queue = data.GetPlayer().GetRecruitmentQueue(city);
		long soldiers_in_queue = data.GetParam().base_recruitment * recruitment_queue.size();
		if (soldiers_in_queue <= 0) {
			city_soldiers_in_queue.setText("");
		} else {
			city_soldiers_in_queue.setText("-" + StringUtil.LongNumber(soldiers_in_queue));
		}
		ArrayList<Army> army_list = state.GetMilitary().GetArmies();
		for (int i = 0; i < ConstStateData.kMaxArmies; ++i) {
			if (i < army_list.size()) {
				Army army = army_list.get(i);
				armies[i].setText(army_list.get(i).GetName());
				armies[i].setEnabled(cmp.utils.army_util.IsArmyReinforceable(army));
				long[] typed_soldiers = new long[Unit.kMaxUnitType];
				for (UnitType type : UnitType.values()) {
					typed_soldiers[type.ordinal()] = army.GetTypedSoldier(type);
				}
				soldiers_in_queue = cmp.utils.player_util.CountTypedSoldiersInQueue(army, typed_soldiers);
				army_soldiers[i].setText(String.format("%,d + %,d", army.GetTotalSoldier(), soldiers_in_queue));
				army_soldiers[i].setToolTipText(StringUtil.JoinLongNumber(" / ", typed_soldiers));
			} else {
				armies[i].setText("");
				armies[i].setEnabled(false);
				if (armies[i].isSelected()) armies[0].setSelected(true);
				army_soldiers[i].setText("");
			}
		}
		for (int i = 1; i < ArmyData.kMaxSoldierTypes; ++i) {
			SoldierType type = SoldierType.values()[i];
			long allowed_soldiers = cmp.utils.state_util.GetAllowedTypedSoldiers(state, type);
			allowed_soldiers -= cmp.utils.state_util.GetTypedSoldiers(state, type) + cmp.utils.state_util.GetTypedSoldiersUnderConstruction(state, type)
				+ cmp.utils.player_util.GetTypedSoldiersInQueue(type);
			if (allowed_soldiers >= data.GetParam().base_recruitment) {
				soldier_types[i].setText(String.format("%s (%,d)", Texts.soldierType[i], allowed_soldiers));
				soldier_types[i].setEnabled(true);
			} else {
				soldier_types[i].setText(String.format("%s (%,d)", Texts.soldierType[i], allowed_soldiers));
				soldier_types[i].setEnabled(false);
				if (soldier_types[i].isSelected()) soldier_types[0].setSelected(true);
			}
		}
		
		for (int i = 0; i < unit_buttons.length; ++i) {
			for (int j = 0; j < unit_buttons[i].length; ++j) {
				Unit unit = data.GetConstData().typed_units.get(i).get(j);
				unit_buttons[i][j].setVisible(cmp.utils.state_util.IsUnitAvailable(state, unit));
			}
		}
	}

	@Override
	protected void Confirm() {
	}
	
	private JLabel city_name = new JLabel();
	private JLabel city_pop = new JLabel();
	private JLabel city_soldiers_in_queue = new JLabel();
	private JRadioButton[] armies = new JRadioButton[ConstStateData.kMaxArmies];
	private JLabel[] army_soldiers = new JLabel[ConstStateData.kMaxArmies];
	private JRadioButton[] soldier_types = new JRadioButton[ArmyData.kMaxSoldierTypes];
	private JButton[][] unit_buttons = new JButton[Unit.kMaxUnitType][];
}
