package ac.ui.swing.dialog;

import java.util.function.Function;
import java.util.function.Supplier;

import ac.data.constant.Ability;
import ac.data.constant.Role;
import ac.data.constant.Texts;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Role.RoleType;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.DialogValidator;
import ac.ui.swing.GenericFrame;
import ac.ui.swing.elements.ScrollListComponent;

public class PersonSelectionDialog extends StateDialog {

	private static final long serialVersionUID = 1L;

	public PersonSelectionDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<State> state_getter, boolean include_king, boolean show_assignment, Function<Person, Action> func) {
		super(parent, cmp, data, state_getter, Texts.select + Texts.person, 500, 620, false);
		this.include_king = include_king;
		this.show_assignment = show_assignment;
		this.func = func;
		
		person_list = new ScrollListComponent(kColumnWidth, 20);
		person_list.SetColumnHeaders(show_assignment ? kAssignColumnNames : kRoleColumnNames);
		
		person_list.setBounds(10, 40, 400, 550);
		add(person_list);
		
		InitDone();
		Refresh();
	}
	
	public PersonSelectionDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<City> city_getter, Function<Person, Action> func) {
		super(parent, cmp, data, DialogValidator.StateGetter(city_getter), Texts.select + Texts.person, 500, 600, false);
		this.include_king = false;
		this.show_assignment = true;
		this.func = func;
		this.SetAdditionalValidator(() -> DialogValidator.ValidateCity(city_getter.get(), data));
		
		person_list = new ScrollListComponent(kColumnWidth, 20);
		person_list.SetColumnHeaders(show_assignment ? kAssignColumnNames : kRoleColumnNames);
		
		person_list.setBounds(10, 40, 400, 550);
		add(person_list);
		
		InitDone();
		Refresh();
	}
	
	
	@Override
	protected void Refresh() {
		if (state == null) return;
		person_list.Resize(state.GetPersons().size() + (include_king ? 1 : 0));
		int row = 0;
		if (include_king) DrawOnePerson(state.GetKing(), row++);
		for (Person person : state.GetPersons()) {
			DrawOnePerson(person, row++);
		}
	}

	@Override
	protected void Confirm() {
	}
	
	private void DrawOnePerson(Person person, int row) {
		person_list.SetValue(row, 0, person.GetName());
		for (int i = 0; i < Ability.kMaxTypes; ++i) {
			person_list.SetValue(row, 1 + i, String.valueOf(person.GetAbility(AbilityType.values()[i])));
		}
		if (show_assignment) {
			City city = person.GetAssignedCity();
			Army army = person.GetAssignedArmy();
			if (city != null) {
				person_list.SetValue(row, 4, city.GetName());
			} else if (army != null) {
				person_list.SetValue(row, 4, army.GetIdName());
			} else {
				person_list.SetValue(row, 4, "");
			}
		} else {
			RoleType role = person.GetRoleType();
			person_list.SetValue(row, 4, "");
			for (int i = 0; i < Ability.kMaxTypes; ++i) {
				if (role == Role.kMinisters[i]) {
					person_list.SetValue(row, 4, Texts.abilityIcons[i] + " " + Texts.abilities[i] + Texts.minister);
					break;
				}
			}
		}
		person_list.SetCallback(row, () -> {
			cmp.action_consumer.accept(func.apply(person));
			CloseDialog();
		});
	}
	
	public static Function<Person, Action> AssignCityGovernor(Supplier<City> getter) {
		return person -> {
			Action action = new Action(ActionType.ASSIGN_GOVERNOR);
			action.object = person;
			action.object2 = getter.get();
			return action;
		};
	}
	
	public static Function<Person, Action> AssignArmyGeneral(Supplier<Army> getter) {
		return person -> {
			Action action = new Action(ActionType.ASSIGN_GENERAL);
			action.object = person;
			action.object2 = getter.get();
			return action;
		};
	}
	
	public static Function<Person, Action> AssignMinister(AbilityType ability_type) {
		return person -> {
			Action action = new Action(ActionType.ASSIGN_MINISTER);
			action.object = person;
			action.quantity = ability_type.ordinal();
			return action;
		};
	}
	
	private boolean include_king;
	private boolean show_assignment;
	private Function<Person, Action> func;

	private ScrollListComponent person_list;
	
	private static final int[] kColumnWidth = { 60, 30, 30, 30, 80 };
	private static final String[] kAssignColumnNames = { Texts.person, Texts.abilities[0], Texts.abilities[1], Texts.abilities[2], Texts.assignment };
	private static final String[] kRoleColumnNames = { Texts.person, Texts.abilities[0], Texts.abilities[1], Texts.abilities[2], Texts.role };
}
