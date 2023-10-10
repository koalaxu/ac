package ac.ui.swing.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import ac.data.CityData.Profession;
import ac.data.constant.ConstCityData;
import ac.data.constant.Texts;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;

public class ProfessionAllocateDialog extends CityDialog {
	private static final long serialVersionUID = 1L;
	public ProfessionAllocateDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<City> city_getter) {
		super(parent, cmp, data, city_getter, Texts.professionDistribution, 320, 160, true);
		
		for (int i = 1; i < ConstCityData.kMaxProfessions; ++i) {
			values[i - 1] = city.GetPopulation().GetProfessionTargetPct(Profession.values()[i]);
		}
		for (int i = 0; i < ConstCityData.kMaxProfessions; i++) {
			lValue[i] = new JLabel();
			lValue[i].setBounds(113, 40 + i * 30, 40, 20);
			lValue[i].setHorizontalAlignment(SwingConstants.CENTER);
			add(lValue[i]);
			lType[i] = new JLabel(Texts.professions[i]);
			lType[i].setBounds(10, 40 + i * 30, 30, 20);
			add(lType[i]);
		}
		UpdateValue();
		JButton[][] button = new JButton[3][2];
		for (int i = 0; i < 3; i++) {
			button[i][0] = new JButton(Texts.leftIcon);
			button[i][1] = new JButton(Texts.rightIcon);
			button[i][0].setBounds(70, 70 + i * 30, 16, 16);
			button[i][1].setBounds(180, 70 + i * 30, 16, 16);
			add(button[i][0]);
			add(button[i][1]);
			button[i][0].addActionListener(new ValueChangeListener(i, -1));
			button[i][1].addActionListener(new ValueChangeListener(i, 1));
		}
		
		InitDone();
	}

	private class ValueChangeListener implements ActionListener {
		public ValueChangeListener(int index, int inc) {
			this.index = index;
			this.inc = inc;
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			if (inc == -1) {
				values[index] = Math.max(0, values[index] - 1);
				UpdateValue();
				return;
			}		
			values[index] = Math.min(100, values[index] + 1);
			UpdateValue();
		}
		
		private int inc;
		private int index;
	}
	
	@Override
	protected void Refresh() {
		ValidateData();
	}

	private void UpdateValue() {
		for (int i = 0; i < 3; i++) {
			lValue[i + 1].setText(values[i] + "%");
		}
		lValue[0].setText((100 - values[0] - values[1] - values[2]) + "%");
		ValidateData();
	}
	
	private void ValidateData() {
		boolean valid = cmp.utils.city_util.IsProfessionAllocationAllowed(city, values[0].intValue(), values[1].intValue(), values[2].intValue());
		EnableConfirmButton(valid);
	}
	
	@Override
	protected void Confirm() {
		Action action = new Action(ActionType.ALLOCATE_PROFESSION);
		action.object = city;
		action.quantity = values[0];
		action.quantity2 = values[1];
		action.quantity3 = values[2];
		cmp.action_consumer.accept(action);
	}
	
	private Integer[] values = { 0, 0, 0 };
	private JLabel[] lValue = new JLabel[ConstCityData.kMaxProfessions];
	private JLabel[] lType = new JLabel[ConstCityData.kMaxProfessions];
}
