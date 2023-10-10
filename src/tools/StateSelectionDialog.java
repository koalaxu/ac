package ac.tools;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JFrame;

import ac.data.GameData;
import ac.util.StringUtil;

public class StateSelectionDialog extends SelectionDialog {
	private static final long serialVersionUID = 1L;
	public StateSelectionDialog(JFrame parent, Consumer<Integer> func, Predicate<Integer> condition) {
		super(parent, "Select State", func);
		
	    for (int i = -1; i < GameData.const_data.states.size(); ++i) {
	    	if (condition != null && !condition.test(i)) continue;
	    	AddButton(i, i == -1 ? "Reset" : StringUtil.IfNull(GameData.const_data.states.get(i).alias, GameData.const_data.states.get(i).name));
	    }
			
	}
}
