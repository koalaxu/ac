package ac.ui.swing;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import ac.data.constant.Texts;
import ac.engine.data.DataAccessor;

public abstract class GenericDialog extends Dialog {
	private static final long serialVersionUID = 1L;

	public GenericDialog(GenericDialog parent, Components cmp, DataAccessor data, String title, int width, int height, boolean has_confirm_button) {
		super(parent, title);
		this.parent = parent;
		parent.setEnabled(false);
		cmp.dialog_on_dialog = this;
		Init(cmp, data, width, height, has_confirm_button);
	}
	public GenericDialog(JFrame parent, Components cmp, DataAccessor data, String title, int width, int height, boolean has_confirm_button) {
		super(parent, title);
		cmp.dialog_on_frame = this;
		Init(cmp, data, width, height, has_confirm_button);
	}
	public void Init(Components cmp, DataAccessor data, int width, int height, boolean has_confirm_button) {
		//cmp.AddDialog(this);
		this.cmp = cmp;
		this.data = data;
		
		cmp.SetEnabled(false);
        setResizable(false);
        this.setLayout(null);
        
		bReturn = new JButton(Texts.no);
		bReturn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CloseDialog();
			}
			
		});
		bReturn.setBounds(width - 70, 40, 60, 20);
		add(bReturn);
		
		if (has_confirm_button) {
			bConfirm = new JButton(Texts.yes);
			bConfirm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Confirm();
					CloseDialog();
				}
				
			});
			bConfirm.setBounds(width - 70, height - 30, 60, 20);
			add(bConfirm);
		}
		this.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				CloseDialog();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				if (parent != null) {
					cmp.dialog_on_dialog = null;
				} else {
					cmp.dialog_on_frame = null;
				}
				cmp.SetEnabled(true);
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});
		SetSizeAndCenter(width, height);
		this.setVisible(true);
	}
	
	private void SetSizeAndCenter(int width, int height) {
		setSize(width, height);
		Component c = getParent();
        Point point = c.getLocation();
        setLocation(point.x + (c.getWidth() - width) / 2, point.y + (c.getHeight() - height) / 2);
	}
	
	protected static enum Status {
		IDLE,
		VALID,
		INVALID,
	};
	
	protected void Repaint() {
		if (!initiated) return;
		switch (Valid()) {
		case INVALID:
			CloseDialog();
			return;
		case VALID:
			Refresh();
			repaint();
		default:
			break;
		}
	}
	
	protected abstract Status Valid();
	protected abstract void Refresh();
	protected abstract void Confirm();
	
	protected void CloseDialog() {
		setVisible(false);
		if (parent != null) {
			parent.setEnabled(true);
			parent.Refresh();
		}
		dispose();
		cmp.Repaint();
	}
	
	protected void EnableConfirmButton(boolean enabled) {
		bConfirm.setEnabled(enabled);
	}
	
	protected void InitDone() {
		initiated = true;
	}
	
	protected JButton bReturn;
	protected JButton bConfirm;
	protected Components cmp;
	protected DataAccessor data;
	private boolean initiated = false;
	private GenericDialog parent;
}
