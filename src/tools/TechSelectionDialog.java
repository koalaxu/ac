package ac.tools;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JFrame;

import ac.data.GameData;
import ac.data.constant.Technology;
import ac.data.constant.Technology.TechnologyType;

public class TechSelectionDialog extends SelectionDialog {
	private static final long serialVersionUID = 1L;

	public TechSelectionDialog(JFrame parent, Consumer<Integer> func, TechnologyType type, Predicate<Integer> condition) {
		super(parent, "Select Tech", func);
		
		ArrayList<Technology> techs = GameData.const_data.typed_techs.get(type);
		for (int i = -1; i < techs.size(); ++i) {
	    	if (condition != null && !condition.test(i)) continue;
	    	AddButton(i, i == -1 ? "Reset" : techs.get(i).name);
		}
	}

}
