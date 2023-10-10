package ac.ui.swing.panel;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import ac.data.constant.Ability;
import ac.data.constant.Policies;
import ac.data.constant.Role;
import ac.data.constant.Texts;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Policies.Policy;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.IdKeyedData;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.dialog.PersonSelectionDialog;
import ac.ui.swing.dialog.PolicyDialog;
import ac.ui.swing.elements.RatioBarElement;
import ac.ui.swing.elements.ScrollListComponent;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;

public class StatePersonPanel extends TypedDataPanel<State> {
	private static final long serialVersionUID = 1L;
	public StatePersonPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		monarch = new TextElement(new Rectangle(80, 10, 60, 15));
		monarch.SetFontSize(12)
			.SetAlignment(Alignment.CENTER)
			.SetUseHandCursor(true);
		for (int i = 0; i < Ability.kMaxTypes; ++i) {
			ministers[i] = new TextElement(new Rectangle(80, 70 + 70 * i, 60, 15));
			ministers[i].SetFontSize(12).SetAlignment(Alignment.CENTER);
			AddVisualElement(ministers[i]);
			policies[i] = new TextElement(new Rectangle(5, 90 + 70 * i, 140, 15));
			policies[i].SetHasFrame(false);
			AddVisualElement(policies[i]);
			policy_progress[i] = new RatioBarElement(new Rectangle(5, 110 + 70 * i, 150, 15));
			AddVisualElement(policy_progress[i]);
			
			assign_minister[i] = new JButton(Texts.settingIcon);
			assign_minister[i].setBounds(150, 70 + 70 * i, 15, 15);
			assign_minister[i].addActionListener(new AssignMinister(AbilityType.values()[i]));
			AddPlayerElement(assign_minister[i]);
			choose_policy[i] = new JButton(Texts.settingIcon);
			choose_policy[i].setBounds(150, 90 + 70 * i, 15, 15);
			choose_policy[i].addActionListener(new ChoosePolicy(AbilityType.values()[i]));
			AddPlayerElement(choose_policy[i]);
		}
		AddVisualElement(monarch);
		officers = new TextElement(new Rectangle(290, 265, 50, 15));
		officers.SetFontSize(12);
		AddVisualElement(officers);
		salary = new TextElement(new Rectangle(410, 265, 40, 15));
		salary.SetFontSize(12);
		AddVisualElement(salary);
		
		person_list = new ScrollListComponent(kColumnWidth, 20);
		person_list.SetColumnHeaders(kColumnNames);
		
		person_list.setBounds(170, 10, 280, 250);
		add(person_list);
		
		hire_officer.setBounds(170, 265, 75, 15);
		hire_officer.setFont(new Font(hire_officer.getFont().getName(), 0, 12));
		hire_officer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Action action = new Action(ActionType.HIRE_OFFICER);
				action.object = value;
				cmp.action_consumer.accept(action);
				cmp.Repaint();
			}
		});
		AddPlayerElement(hire_officer);
	}

	@Override
	public void Reset(State state) {
		monarch.SetText(state.GetKing().GetName())
			.SetClickCallback(() -> { cmp.person.Show(state.GetKing()); });
		for (int i = 0; i < Ability.kMaxTypes; ++i) {
			Person minister = state.GetOfficer(Role.kMinisters[i]);
			if (minister != null) {
				ministers[i].SetText(minister.GetName()).SetClickCallback(() -> { cmp.person.Show(minister); } );
				ministers[i].SetUseHandCursor(true);
			} else {
				ministers[i].SetText("").SetClickCallback(null);
				ministers[i].SetUseHandCursor(false);
			}
			AbilityType type = AbilityType.values()[i];
			Policy policy = state.GetPolicy().GetPolicy(type);
			policies[i].SetText(GetPolicyString(state, type));
			if (policy != Policy.ADOPT_IDEOLOGY && state.GetPolicy().GetPolicyContextQuantity(type) > 0) {
				policies[i].SetTooltipText(Texts.quantity + ": " + state.GetPolicy().GetPolicyContextQuantity(type));
			} else {
				policies[i].SetTooltipText(null);
			}
			
			if (policy == Policy.NONE) {
				policy_progress[i].SetMax(0).SetValue(0).SetAddition(null);
			} else {
				policy_progress[i].SetMax(Policies.GetCost(policy)).SetValue(state.GetPolicy().GetPolicyProgress(type))
					.SetAddition(cmp.utils.state_util.GetPolicyPoint(state, type));
			}
		}
		
		person_list.Resize(state.GetPersons().size() + 1);
		int row = 0;
		DrawOnePerson(state.GetKing(), row++);
		for (Person person : state.GetPersons()) {
			DrawOnePerson(person, row++);
		}
		officers.SetText(String.format("%d/%d", state.GetPersons().size(), cmp.utils.person_util.GetMaxAllowedOfficer(state)));
		salary.SetNumber(cmp.utils.state_util.GetSalaryExpense(state));
		
		int hiring_cost = cmp.utils.state_util.GetHiringCost(state);
		hire_officer.setEnabled(state.GetResource().gold >= hiring_cost && cmp.utils.person_util.CanHirePeople(state));
		hire_officer.setToolTipText(Texts.requirement + ": " + hiring_cost + Texts.goldIcon);
	}
	
	private String GetPolicyString(State state, AbilityType type) {
		Policy policy = state.GetPolicy().GetPolicy(type);
		IdKeyedData object = state.GetPolicy().GetPolicyObject(type);
		IdKeyedData object2 = state.GetPolicy().GetPolicyObject2(type);
		long context_quantity = state.GetPolicy().GetPolicyContextQuantity(type);
		String policy_text = Texts.policy + ":  " + Texts.policies[policy.ordinal()];
		if (object != null) {
			policy_text += "  (" + object.GetName() + (object2 == null ? "" : "->" + object2.GetName()) +  ")";
		} else if (context_quantity > 0) {
			policy_text += "  (" + Texts.ideologies[(int)context_quantity] + ")";
		}
		return policy_text;
	}
	
	private void DrawOnePerson(Person person, int row) {
		person_list.SetValue(row, 0, person.GetName());
		for (int i = 0; i < Ability.kMaxTypes; ++i) {
			person_list.SetValue(row, 1 + i, String.valueOf(person.GetAbility(AbilityType.values()[i])));
		}
		City city = person.GetAssignedCity();
		Army army = person.GetAssignedArmy();
		if (city != null) {
			person_list.SetValue(row, 4, city.GetName());
		} else if (army != null) {
			person_list.SetValue(row, 4, army.GetIdName());
		} else {
			person_list.SetValue(row, 4, "");
		}
		person_list.SetCallback(row, () -> {  cmp.person.Show(person);  });
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);
		
		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		text_writer.DrawString(5, 10, Texts.kingIcon + " " + Texts.monarch);
		
		for (int i = 0; i < Ability.kMaxTypes; ++i) {
			text_writer.DrawString(5, 70 + 70 * i, Texts.abilityIcons[i] + " " + Texts.abilities[i] + Texts.minister);
		}
		text_writer.DrawString(260, 265, Texts.upperbound);
		text_writer.DrawString(350, 265, Texts.salary + Texts.expense);
	}
	
	private class AssignMinister implements ActionListener {
		public AssignMinister(AbilityType type) {
			this.type = type;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			new PersonSelectionDialog(cmp.city, cmp, data, () -> GetData(), false, false, PersonSelectionDialog.AssignMinister(type));
		}
		
		private AbilityType type;
	}
	
	private class ChoosePolicy implements ActionListener {
		public ChoosePolicy(AbilityType type) {
			this.type = type;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			new PolicyDialog(cmp.state, cmp, data, () -> GetData(), type);
		}
		
		private AbilityType type;
	}
	
	private TextElement monarch;
	private TextElement ministers[] = new TextElement[Ability.kMaxTypes];
	private TextElement policies[] = new TextElement[Ability.kMaxTypes];
	private RatioBarElement policy_progress[] = new RatioBarElement[Ability.kMaxTypes];
	private TextElement officers;
	private TextElement salary;
	
	private ScrollListComponent person_list;
	
	// Player Element
	private JButton hire_officer = new JButton(Texts.hireOfficer);
	private JButton[] assign_minister = new JButton[Ability.kMaxTypes];
	private JButton[] choose_policy = new JButton[Ability.kMaxTypes];
	
	private static final int[] kColumnWidth = { 60, 30, 30, 30, 80 };
	private static final String[] kColumnNames = { Texts.person, Texts.abilities[0], Texts.abilities[1], Texts.abilities[2], Texts.assignment };
}
