package ac.tools;

import java.util.function.Consumer;
import javax.swing.JFrame;

import ac.data.GameData;

public class RaceSelectionDialog extends SelectionDialog {
	private static final long serialVersionUID = 1L;
	public RaceSelectionDialog(JFrame parent, Consumer<Integer> func) {
		super(parent, "Select Race", func);
		
	    for (int i = 0; i < GameData.const_data.races.size(); ++i) {
	    	AddButton(i, GameData.const_data.races.get(i).name);
	    }
	}
}
