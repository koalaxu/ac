package ac.ui.swing.frame;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;

public class MainFrame extends GenericFrame {
	private static final long serialVersionUID = 1L;

	public MainFrame(Components cmp, DataAccessor data) {
		super(cmp, data, 900, 587);
		


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        // Map Panel
        JPanel main_panel = new JPanel(null);
        main_panel.add(cmp.map_panel);
//        main_panel.add(components.info_panel);
//        main_panel.add(components.map_panel);
        cmp.map_panel.setBounds(0, 0, 762, 557);
//        components.info_panel.setBounds(720, 0, 180, 270);
//        components.map_panel.setBounds(720, 270, 180, 330);
        cards.add(main_panel, "main");
        cmp.side_panel.setBounds(770, 0, 130, 557);
        
        add(cards);
        main_panel.add(cmp.side_panel);
        ShiftCard("main");   
        setVisible(true);
    }
	
	public void ShiftCard(String card_name) {
		card_layout.show(cards, card_name);
	}
	
	@Override
	protected void Refresh() {
	}
	
//	public void SetUtils(UtilityHub utils) {
//		components.utils = utils;
//	}
	
//	public void ShowMessage(String text) {
//		components.canvas.SetMessage(text);
//	}
	
//	public void EnableUI() {
//		components.SetUIEnabled(true, GameData.data.remaining_turns > 0);
//		this.repaint();
//	}
	
	private CardLayout card_layout = new CardLayout();
	private JPanel cards = new JPanel(card_layout);


}