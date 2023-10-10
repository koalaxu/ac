package ac.ui.swing;

import java.util.function.Consumer;

import javax.swing.JPanel;

import ac.engine.Action;
import ac.engine.data.DataAccessor;
import ac.engine.util.Utils;
import ac.ui.swing.frame.ArmyFrame;
import ac.ui.swing.frame.BattleFieldFrame;
import ac.ui.swing.frame.CityFrame;
import ac.ui.swing.frame.InformationFrame;
import ac.ui.swing.frame.MainFrame;
import ac.ui.swing.frame.OverviewFrame;
import ac.ui.swing.frame.PersonFrame;
import ac.ui.swing.frame.StateFrame;
import ac.ui.swing.panel.MapPanel;
import ac.ui.swing.panel.SidePanel;

public class Components {
	public Components(Consumer<DataAccessor> game_initializer_callback, Consumer<DataAccessor> game_loader_callback) {
		this.game_initializer_callback = game_initializer_callback;
		this.game_loader_callback = game_loader_callback;
		
	}
	
	public void ShowGameSelection() {
		if (game_selection != null) {
			game_selection.dispose();
		}
		game_selection = new GameSelectionFrame(this, game_initializer_callback, game_loader_callback);
		game_selection.Show();
	}
	
	public void SetUp(DataAccessor data, Consumer<Action> action_consumer, Runnable pause, Runnable resume) {
		game_selection.setVisible(false);
		
		this.action_consumer = action_consumer;
		
		if (main != null) {
			main.dispose();
			city.dispose();
			state.dispose();
			person.dispose();
			army.dispose();
			battle_field.dispose();
			overview.dispose();
			information.dispose();
		}
		
		main_panel = new JPanel(null);
		map_panel = new MapPanel(this, data);
		side_panel = new SidePanel(this, data, pause, resume);
		
		main = new MainFrame(this, data);
		city = new CityFrame(this, data);
		state = new StateFrame(this, data);
		person = new PersonFrame(this, data);
		army = new ArmyFrame(this, data);
		battle_field = new BattleFieldFrame(this, data);
		overview = new OverviewFrame(this, data);
		information = new InformationFrame(this, data);
		
		utils = data.GetUtils();
		Repaint();
	}
	
	public void Repaint() {	
		side_panel.Repaint();
		main.Repaint();
		city.Repaint();
		state.Repaint();
		person.Repaint();
		army.Repaint();
		battle_field.Repaint();
		overview.Repaint();
		information.Repaint();
		map_panel.repaint();
		if (dialog_on_frame != null) dialog_on_frame.Repaint();
		if (dialog_on_dialog != null) dialog_on_dialog.Repaint();
	}
	
	public void SetEnabled(boolean enabled) {
		if (main != null) {
			main.setEnabled(enabled);
			city.setEnabled(enabled);
			state.setEnabled(enabled);
			person.setEnabled(enabled);
			army.setEnabled(enabled);
			battle_field.setEnabled(enabled);
			overview.setEnabled(enabled);
			information.setEnabled(enabled);
			map_panel.setEnabled(enabled);
		}
	}
	
	protected void OnSelectionFrameClose() {
		if (main == null) {
			System.exit(0);
		}
		game_selection.dispose();
		SetEnabled(true);
	}
	
	public GameSelectionFrame game_selection;
	
	public MainFrame main;
	public CityFrame city;
	public StateFrame state;
	public PersonFrame person;
	public ArmyFrame army;
	public BattleFieldFrame battle_field;
	public OverviewFrame overview;
	public InformationFrame information;
	
	public JPanel main_panel;
	public MapPanel map_panel;
	public SidePanel side_panel;
	
	public Utils utils;
	public Consumer<Action> action_consumer;
	
	protected GenericDialog dialog_on_frame;
	protected GenericDialog dialog_on_dialog;
	
	private Consumer<DataAccessor> game_initializer_callback;
	private Consumer<DataAccessor> game_loader_callback;
}
