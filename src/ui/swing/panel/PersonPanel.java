package ac.ui.swing.panel;

import ac.data.constant.Texts;
import ac.data.constant.Ability;
import ac.data.constant.Ability.AbilityType;
import ac.engine.data.Data;
import ac.engine.data.DataAccessor;
import ac.engine.data.Person;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.ScrollListComponent;

public class PersonPanel extends TypedDataPanel<Data> {
	private static final long serialVersionUID = 1L;
	public PersonPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		person_list = new ScrollListComponent(kColumnWidth, 20);
		person_list.SetColumnHeaders(kColumnNames);
		
		person_list.setBounds(5, 5, 450, 260);
		add(person_list);
	}
	
	@Override
	public void Reset(Data input) {
		person_list.Resize(data.GetAllPersons().size());
		int row = 0;
		for (Person person : data.GetAllPersons()) {
			person_list.SetValue(row, 0, person.GetName());
			person_list.SetValue(row, 1, person.IsDead() ? Texts.yes : "");
			for (int i = 0; i < Ability.kMaxTypes; ++i) {
				person_list.SetValue(row, 2 + i, String.valueOf(person.GetAbility(AbilityType.values()[i])));
			}
			person_list.SetCallback(row, () -> {  cmp.person.Show(person);  });
			++row;
		}
	}

	private ScrollListComponent person_list;
	
	private static final int[] kColumnWidth = { 60, 30, 30, 30, 30 };
	private static final String[] kColumnNames = { Texts.person, Texts.dead, Texts.abilities[0], Texts.abilities[1], Texts.abilities[2] };

}
