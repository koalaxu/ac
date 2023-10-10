package ac.tools;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JFrame;

import ac.data.GameData;

public class CitySelectionDialog extends SelectionDialog {
	private static final long serialVersionUID = 1L;
	public CitySelectionDialog(JFrame parent, Consumer<Integer> func, GameData data, Predicate<Integer> condition) {
		super(parent, "Select City", func);
		
	    for (int i = -1; i < data.cities.size(); ++i) {
	    	if (condition != null && !condition.test(i)) continue;
	    	AddButton(i, i == -1 ? "Reset" : data.cities.get(i).name);
	    }
			
	}
}
