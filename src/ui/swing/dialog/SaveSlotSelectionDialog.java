package ac.ui.swing.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JFrame;

import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.GenericDialog;
import ac.util.StringUtil;

public class SaveSlotSelectionDialog extends GenericDialog {
	private static final long serialVersionUID = 1L;
	public SaveSlotSelectionDialog(JFrame parent, Components cmp, DataAccessor data, String title, Consumer<Integer> func, boolean disable_empty_slots) {
		super(parent, cmp, data, title, 280, 450, false);
		this.func = func;
		
		for (int i = 0; i < kMaxSaveSlots; ++i) {
			String slot_name = DataAccessor.CheckSlot(i);
			JButton button = new JButton(StringUtil.IfNull(slot_name, "Empty Slot"));
			button.setBounds(25, 40 + 40 * i, 150, 20);
			button.setEnabled(!disable_empty_slots || slot_name != null);
			button.addActionListener(new SlotClicked(i));
			add(button);
		}
	}

	@Override
	protected Status Valid() {
		return null;
	}

	@Override
	protected void Refresh() {
	}

	@Override
	protected void Confirm() {
	}
	
	private class SlotClicked implements ActionListener {
		public SlotClicked(int i) {
			index = i;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			func.accept(index);
			CloseDialog();
			dispose();
			
		}
		private int index;
	}
	
	private static final int kMaxSaveSlots = 10;
	private Consumer<Integer> func;
}
