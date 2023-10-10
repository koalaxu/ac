package ac.tools;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ac.data.base.Position;
import ac.data.constant.ConstCityData;
import ac.data.constant.ConstGameData;
import ac.data.constant.Tile;
import ac.data.constant.Tile.Terrain;
import data.FileUtil;
import ac.ui.swing.util.ColorUtil;
import ac.ui.swing.util.TileDrawer;
import ac.ui.swing.util.TileGeometry;
import ac.util.TileUtil;
import data.JsonUtil;

public class MapEditorFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		MapEditorFrame frame = new MapEditorFrame();
		frame.revalidate();
	}
	
	private class MainPanel extends JPanel implements AWTEventListener {
	    private static final long serialVersionUID = 1L;
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			
			TileDrawer tile_drawer = new TileDrawer(g, tile_util);
			for (Tile tile : const_data.tiles) {
	        	Color[] border_colors = new Color[6];
	        	for (int i = 0; i < 6; i++) {
	        		border_colors[i] = tile.border_has_river[i] ? Color.blue : Color.WHITE;
	        	}
				tile_drawer.DrawOneTile(tile.coordinate, ColorUtil.GetTerrainColor(tile.terrain), border_colors);
				if (tile.county >= 1) {
					  Font font = new Font(getFont().getFontName(), 0, 8);
					  g.setFont(font);
					  Point p = tile_util.GetAnchorPoint(tile.coordinate);
					  g.drawString(String.valueOf(tile.county), p.x + 4, p.y + 14);
				}
			}
			
			g.setColor(Color.BLACK);
			for (ConstCityData city : const_data.cities) {
				  Font font = new Font(getFont().getFontName(), 0, 8);
				  g.setFont(font);
				  Point p = tile_util.GetAnchorPoint(city.coordinate);
				  g.drawString(city.name, p.x + 2, p.y + 6);
			}
		}
		
		@Override
		public void eventDispatched(AWTEvent event) {
			if (chosen == null) return;
			if (event.getClass() == KeyEvent.class) {  
				this.getParent().requestFocus();
	            KeyEvent keyEvent = (KeyEvent) event; 
	            if (keyEvent.getID() == KeyEvent.KEY_TYPED) {
	            	char input = keyEvent.getKeyChar();
					int index = input - '0';
					if (index >= 0 && index < 6) {
		    			chosen.border_has_river[index] = !chosen.border_has_river[index];
		    			Position other = TileUtil.GetNeighborPosition(chosen.coordinate, index);
		    			Tile other_tile = const_data.GetTile(other);
		    			ArrayList<Position> pos = TileUtil.GetNeighborPositions(other);
		    			for (int i = 0; i < pos.size(); ++i) {
		    				if (pos.get(i).equals(chosen.coordinate)) {
		    					other_tile.border_has_river[i] = chosen.border_has_river[index];
		    				}
		    			}
					} else {
						index = input - 'a';
						if (index >= 0 && index < Terrain.values().length) {
							chosen.terrain = Terrain.values()[index];
						}
					}
	    			repaint();
	            }
			}
		}
	}
	
	public MapEditorFrame() {
		FileUtil.Init();
		const_data = new ConstGameData();
		
        setSize(kWidth, kHeight);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        MainPanel panel = new MainPanel();
        panel.setBounds(0, 0, kWidth, kHeight);
        panel.setLayout(null);
        Vector<String> names = new Vector<String>();
        for (ConstCityData city : const_data.cities) {
        	names.add(city.name);
        }
        JComboBox<String> drop_box = new JComboBox<String>(names);
        drop_box.setBounds(1350, 40, 150, 20);
        panel.add(drop_box);   
        
        labelPosition = new JLabel("X,Y");
        labelPosition.setBounds(10, 1020, 200, 20);
        panel.add(labelPosition);
        
        panel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Position pos = tile_util.GetCityPosition(e.getPoint());
				Tile tile = const_data.GetTile(pos);
				if (e.getButton() == MouseEvent.BUTTON1) {
					tile.county = drop_box.getSelectedIndex() + 1;
				} else if (e.getButton() == MouseEvent.BUTTON3){
					tile.county = -1;
				}
				chosen = tile;
				repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {			
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
        });
        
        panel.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Position pos = tile_util.GetCityPosition(e.getPoint());
				if (pos == null) labelPosition.setText("X,Y");
				labelPosition.setText(pos.x + "," + pos.y);
				chosen = const_data.GetTile(pos);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
        });
        
        JButton button = new JButton("Save");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JsonUtil.WriteArrayToJson("tiles.json", const_data.tiles, Tile.class, true);
				JsonUtil.WriteArrayToJson("cities.json", const_data.cities, ac.data.constant.ConstCityData.class, true);
			}
		});
		button.setBounds(1350, 10, 200, 20);
        panel.add(button);
        
        add(panel);
        
        Toolkit.getDefaultToolkit().addAWTEventListener(panel, AWTEvent.KEY_EVENT_MASK);  
        setVisible(true);
	}
	
	private static int kWidth = 1760;
	private static int kHeight = 1080;
	
	private Point offset = new Point(15, 15);
	private double scale = 20;
	private TileGeometry tile_util = new TileGeometry(offset.x, offset.y, (int) scale);
	private Tile chosen = null;
	private ConstGameData const_data;
	private JLabel labelPosition;
}
