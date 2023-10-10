package ac.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

class GenericEditPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected GenericEditPanel(Runnable callback) {
		setLayout(null);
		JButton update = new JButton("Update");
		update.setBounds(10, DataEditorFrame.kHeight - 150, 80, 20);
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (FieldEditor<?> editor : field_editors) {
					try {
						editor.ConvertAndSet();
						if (callback != null) callback.run();
						// OnUpdateButtonClicked();
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		add(update);
	}
	
	// protected void OnUpdateButtonClicked() {}
	
	protected void ClearFieldEditors() {
		for (FieldEditor<?> editor : field_editors) {
			this.remove(editor.GetComponent());
		}
		field_editors.clear();
	}
	protected void AddFieldEditor(FieldEditor<?> field_editor, int x, int y, int width, int height, JPanel owner) {
		field_editors.add(field_editor);
		field_editor.GetComponent().setBounds(x, y, width, height);
		owner.add(field_editor.GetComponent());
	}
	private ArrayList<FieldEditor<?>> field_editors = new ArrayList<FieldEditor<?>>();
}