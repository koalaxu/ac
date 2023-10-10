package ac.ui.swing.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import ac.data.base.Resource.ResourceType;
import ac.data.constant.Texts;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.engine.data.StateEconomic;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;

public class BudgetAllocateDialog extends StateDialog {
	private static final long serialVersionUID = 1L;
	public BudgetAllocateDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<State> state_getter) {
		super(parent, cmp, data, state_getter, Texts.budget + Texts.allocate, 480, 160, true);
		
		StateEconomic economic = state_getter.get().GetEconomic();
		values[0] = economic.GetTechBudgetPercentage();
		for (int i = 0; i < kResourceTypes.length; ++i) {
			values[i + 1] = economic.GetResourcePurchaseBudgetPercentage(kResourceTypes[i]);
		}

		for (int i = 0; i <= kResourceTypes.length; i++) {
			lValue[i] = new JLabel();
			lValue[i].setBounds(153, 40 + i * 30, 80, 20);
			lValue[i].setHorizontalAlignment(SwingConstants.CENTER);
			add(lValue[i]);
			lType[i] = new JLabel();
			lType[i].setBounds(10, 40 + i * 30, 80, 20);
			add(lType[i]);
		}
		lType[0].setText(Texts.technology + Texts.budget);
		UpdateValue();
		JButton[][] button = new JButton[kResourceTypes.length + 1][2];
		for (int i = 0; i <= kResourceTypes.length; i++) {
			button[i][0] = new JButton(Texts.leftIcon);
			button[i][1] = new JButton(Texts.rightIcon);
			button[i][0].setBounds(110, 40 + i * 30, 16, 16);
			button[i][1].setBounds(260, 40 + i * 30, 16, 16);
			add(button[i][0]);
			add(button[i][1]);
			button[i][0].addActionListener(new ValueChangeListener(i, -1));
			button[i][1].addActionListener(new ValueChangeListener(i, 1));
			if (i > 0) {
				bExport[i - 1] = new JButton(Texts.exports);
				bExport[i - 1].setBounds(300, 40 + i * 30, 60, 18);
				bExport[i - 1].addActionListener(new ValueInutListener(this, cmp, data, state_getter, i)); 
				add(bExport[i - 1]);
			}
		}
		
		InitDone();
	}
	
	private class ValueInutListener implements ActionListener {
		public ValueInutListener(BudgetAllocateDialog dialog, Components cmp, DataAccessor data, Supplier<State> state_getter, int index) {
			this.dialog = dialog;
			this.cmp = cmp;
			this.data = data;
			this.state_getter = state_getter;
			this.index = index;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			ResourceType type = kResourceTypes[index - 1];
			new NumberDialog(dialog, cmp, data, state_getter, Texts.exports, Texts.resourcesIcon[type.ordinal()],
					values[index] >= 0 ? 0 : -values[index], () -> cmp.utils.prod_util.GetMaxExportAmount(type), number -> {
						if (number > 0) {
							values[index] = (int) -number;
							UpdateValue();
						}
			});
		}
		private Components cmp;
		private DataAccessor data;
		private BudgetAllocateDialog dialog;
		private int index;
		private Supplier<State> state_getter;
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
			if (values[index] < 0) {
				values[index] = 0;
			} else {
				values[index] = Math.min(100, values[index] + 1);
			}
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
		for (int i = 0; i <= kResourceTypes.length; i++) {
			int value = values[i];
			if (value >= 0) {
				lValue[i].setText(String.format("%d%%", value));
			} else {
				lValue[i].setText(String.format("%,d", -value));
			}
			if (i > 0) {
				lType[i].setText(String.format("%s (%s)", Texts.resourcesIcon[kResourceTypes[i - 1].ordinal()], value >= 0 ? Texts.imports : Texts.exports));
			}
		}
		ValidateData();
	}
	
	private void ValidateData() {
		int sum = 0;
		for (int i = 0; i <= kResourceTypes.length; i++) {
			sum += Math.max(0, values[i]);
		}
		EnableConfirmButton(sum <= 100);
	}
	
	@Override
	protected void Confirm() {
		Action action = new Action(ActionType.ALLOCATE_BUDGET);
		action.object = state;
		action.quantity = values[0];
		action.quantity2 = values[1];
		action.quantity3 = values[2];
		action.quantity4 = values[3];
		cmp.action_consumer.accept(action);
	}
	
	private Integer[] values = { 0, 0, 0, 0 };
	private static ResourceType[] kResourceTypes = { ResourceType.FOOD, ResourceType.HORSE, ResourceType.IRON };
	private JLabel[] lValue = new JLabel[kResourceTypes.length + 1];
	private JLabel[] lType = new JLabel[kResourceTypes.length + 1];
	private JButton[] bExport = new JButton[kResourceTypes.length];
}
