package ac.tools;

import java.util.function.Consumer;

import javax.swing.JFrame;

import ac.data.GameData;

public class UnitSelectionDialog extends SelectionDialog {
	private static final long serialVersionUID = 1L;
	public UnitSelectionDialog(JFrame parent, Consumer<Integer> func) {
		super(parent, "Select Unit", func);
		
	    for (int i = -1; i < GameData.const_data.units.size(); ++i) {
	    	AddButton(i, i >= 0 ? GameData.const_data.units.get(i).name : "Reset");
	    }
	}
}
