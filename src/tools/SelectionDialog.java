package ac.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SelectionDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	public SelectionDialog(JFrame parent, String title, Consumer<Integer> func) {
		super(parent, title);
		this.func = func;
		
		SetSizeAndCenter(850, 640);
		parent.setEnabled(false);
	    setResizable(false);
	    setLayout(null);
	    
		scroll_pane = new JScrollPane();
		scroll_pane.setBounds(0, 0, 850, 610);
		scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(scroll_pane);
		
		main_area = new JPanel() {
			private static final long serialVersionUID = 1L;
		};
		main_area.setLayout(null);
		main_area.setBounds(0, 0, 800, 600);
		scroll_pane.setViewportView(main_area);
			
		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				CloseDialog();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				parent.setEnabled(true);
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
		setVisible(true);
	}
	
	protected JButton AddButton(int index, String name) {
    	JButton button = new JButton(name);
    	button.setBounds(x * 100 + 10, y * 30 + 10, 80, 20);
    	button.addActionListener(new ButtonListener(index, func));
    	main_area.add(button);
    	if (++x >= 8) {
    		x = 0;
    		++y;
    	}
    	//main_area.setBounds(0, 0, 850, Math.max(630, y * 30 + 10));
    	main_area.setPreferredSize(new Dimension(800, Math.max(600, y * 30 + 40)));
		main_area.revalidate();
    	return button;
	}
	
	private class ButtonListener implements ActionListener {
		public ButtonListener(int index, Consumer<Integer> func) {
			this.index = index;
			this.func = func;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			func.accept(index);
			CloseDialog();
		}
		private Consumer<Integer> func;
		private int index;
	}
	
	private void SetSizeAndCenter(int width, int height) {
		setSize(width, height);
		Component c = getParent();
        Point point = c.getLocation();
        setLocation(point.x + (c.getWidth() - width) / 2, point.y + (c.getHeight() - height) / 2);
	}

	private void CloseDialog() {
		setVisible(false);
		dispose();
	}
	
	private int x;
	private int y;
	private Consumer<Integer> func;
	private JScrollPane scroll_pane;
	private JPanel main_area;
}
