package ac.tools;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JFrame;

import ac.data.GameData;

public class PersonSelectionDialog extends SelectionDialog {
	private static final long serialVersionUID = 1L;
	public PersonSelectionDialog(JFrame parent, Consumer<Integer> func, Predicate<Integer> condition) {
		super(parent, "Select Person", func);
		
	    for (int i = -1; i < GameData.const_data.persons.size(); ++i) {
	    	if (condition != null && !condition.test(i)) continue;
	    	AddButton(i, i == -1 ? "Reset" : GameData.const_data.persons.get(i).name);
	    }
			
	}
}
