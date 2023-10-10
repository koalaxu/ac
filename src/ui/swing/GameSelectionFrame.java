package ac.ui.swing;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ac.data.GameData;
import ac.data.constant.Scenario;
import ac.data.constant.Texts;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.dialog.SaveSlotSelectionDialog;
import data.JsonUtil;

public class GameSelectionFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	public GameSelectionFrame(Components cmp, Consumer<DataAccessor> game_initializer_callback, Consumer<DataAccessor> game_loader_callback) {
		this.cmp = cmp;
		this.game_initializer_callback = game_initializer_callback;
		this.game_loader_callback = game_loader_callback;
        setSize(kWidth, kHeight);
        setResizable(false);
        setTitle(Texts.menu);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Point point = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        setLocation(point.x - kWidth / 2, point.y - kHeight / 2);
        
        JsonUtil.ParseArrayFromJson("scenarios/scenarios.json", scenarios, Scenario.class, true);
        main = new MainPanel();
        main.setBounds(0, 0, kWidth, kHeight);
        add(main);
        
        this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
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
	}
	
	public void Show() {
		setVisible(true);
		main.setVisible(true);
		if (selection != null) remove(selection);
		if (load_dialog != null) {
			load_dialog.dispose();
			load_dialog.setVisible(false);
		}
	}
	
	private void SwitchStateSelectionPanel(DataAccessor data) {
		if (selection != null) remove(selection);
		selection = new StateSelectionPanel(data);
		selection.setBounds(0, 0, kWidth, kHeight);
		add(selection);
	}
	
	private class MainPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public MainPanel() {
			setLayout(null);
	        int x = 0;
	        int y = 0;
	        for (Scenario scenario : scenarios) {
	        	JButton button = new JButton(scenario.name);
	        	button.setBounds(30 + x * 100, 20 + y * 40, 80, 20);
	        	button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						main.setVisible(false);
						GameData data = GameData.Init(scenario.filename);
						DataAccessor accessor = new DataAccessor(data);
						SwitchStateSelectionPanel(accessor);
					}
	        	});
	        	add(button);
	        	if (++x >= 6) {
	        		x = 0;
	        		y++;
	        	}
	        }
	        JButton load = new JButton(Texts.load);
	        load.setBounds(450, 340, 80, 20);
	        load.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					load_dialog = new SaveSlotSelectionDialog(cmp.game_selection, cmp, null, Texts.load, i -> {
						DataAccessor accessor = DataAccessor.Load(i);
						game_loader_callback.accept(accessor);
					}, true);
				}
	        	
	        });
	        add(load);
	        JButton back = new JButton(Texts.buttonBack);
	        back.setBounds(550, 340, 80, 20);
	        back.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmp.OnSelectionFrameClose();
				}
	        });
	        add(back);
		}
	}
	
	private class StateSelectionPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public StateSelectionPanel(DataAccessor data) {
			setLayout(null);
			JLabel label = new JLabel(data.GetDate().toString());
			label.setBounds(10, 20, 200, 20);
			add(label);
			
	        int x = 0;
	        int y = 0;
	        for (State state : data.GetAllPlayableStates()) {
	        	JButton button = new JButton(state.GetName());
	        	button.setBounds(10 + x * 80, 60 + y * 40, 60, 20);
	        	button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						data.GetPlayer().SetPlayer(state);
						game_initializer_callback.accept(data);
					}
	        	});
	        	add(button);
	        	if (++x >= 8) {
	        		x = 0;
	        		y++;
	        	}
	        }
	        JButton watch = new JButton(Texts.buttonWatch);
	        watch.setBounds(450, 340, 80, 20);
	        watch.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					game_initializer_callback.accept(data);
				}
	        	
	        });
	        add(watch);
	        JButton back = new JButton(Texts.buttonBack);
	        back.setBounds(550, 340, 80, 20);
	        back.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					main.setVisible(true);
				}
	        });
	        add(back);
		}
	}

	private static final int kWidth = 640;
	private static final int kHeight = 400;
	
	private Components cmp;
	private MainPanel main;
	private StateSelectionPanel selection;
	private GenericDialog load_dialog;
	
	private ArrayList<Scenario> scenarios = new ArrayList<Scenario>();
	
	private Consumer<DataAccessor> game_initializer_callback;
	private Consumer<DataAccessor> game_loader_callback;
}
