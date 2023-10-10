package ac.tools;

import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFrame;

import ac.data.constant.Texts;
import ac.ui.swing.util.ColorUtil;

public class ColorSelectionDialog extends SelectionDialog {

	private static final long serialVersionUID = 1L;

	public ColorSelectionDialog(JFrame parent, Consumer<Integer> func) {
		super(parent, "Select Color", func);
		
		for (int i = 0; i < ColorUtil.kStateBackgroundColor.length; ++i) {
			JButton button = AddButton(i, Texts.capitalSymbol);
			button.setForeground(ColorUtil.kStateForegroundColor[i]);
			button.setBackground(ColorUtil.kStateBackgroundColor[i]);
			button.setOpaque(true);
			button.setBorderPainted(false);
		}
	}

}
