package ac.ui.swing.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import ac.data.base.Pair;
import ac.data.base.Position;
import ac.data.constant.Colors;
import ac.data.constant.ConstGameData;
import ac.data.constant.Texts;
import ac.data.constant.Tile;
import ac.data.constant.Tile.Terrain;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.City.CityType;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.GenericPanel;
import ac.ui.swing.util.ColorUtil;
import ac.ui.swing.util.ShapeDrawer;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;
import ac.ui.swing.util.TileDrawer;
import ac.ui.swing.util.TileGeometry;
import ac.util.GeometryUtil;
import ac.util.TileUtil;

public class MapPanel extends GenericPanel {
	private static final long serialVersionUID = 1L;
	public MapPanel(Components componentHub, DataAccessor data) {
		super(componentHub, data);
		
        addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				Point anchor = e.getPoint();
				scale = scale * Math.pow(1.2, -e.getUnitsToScroll());
				scale = Math.max(12, Math.min(100, scale));
				scale = Math.floor(scale / 2 + 0.5) * 2;
				double dx = offset.x - anchor.x;
				double dy = offset.y - anchor.y;
				TileGeometry new_tile_util = new TileGeometry(offset.x, offset.y, (int) scale);
				offset.x = (int) (anchor.x + dx * new_tile_util.cell_width / tile_util.cell_width);
				offset.y = (int) (anchor.y + dy * (new_tile_util.cell_height + new_tile_util.cell_slope_height) /
						(tile_util.cell_height + tile_util.cell_slope_height));
				tile_util.Reset(offset.x, offset.y, (int) scale);
				RestrictOffest();
				tile_util.Reset(offset.x, offset.y, (int) scale);
				repaint();
			}
        	
        });
        
        addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if (legend_rect != null && GeometryUtil.CheckRectangle(e.getPoint(), legend_rect)) return;
				Position pos = GetPosition(e.getPoint());
				City city = GetCity(pos);
				Tile tile = data.GetConstData().GetTile(pos);
				if (city != null) {
					if (e.getClickCount() == 2) {
						cmp.city.Show(city);
					} else if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK) {
						cmp.city.Show(city);
					} else if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
						cmp.state.Show(city.GetOwner());
					} else if ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK) {
						cmp.battle_field.Show(pos);
					}
				}
				if (tile.terrain == Terrain.HIGH_MOUNTAIN) {
					cmp.side_panel.ShowTile(null);
				} else {
					cmp.side_panel.ShowTile(pos);
				}
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				prev = e.getPoint();				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				prev = null;
			}

			@Override
			public void mouseEntered(MouseEvent e) {			
			}

			@Override
			public void mouseExited(MouseEvent e) {			
			}
        });
        
        addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				Point next = e.getPoint();
				if (prev != null) {
					offset.translate(next.x - prev.x, next.y - prev.y);
					RestrictOffest();
					prev = next;
					tile_util.Reset(offset.x, offset.y, (int) scale);
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				Tile tile = GetTile(e.getPoint());
				if (tile != null && tile.county > 0 &&
						(legend_rect == null || !GeometryUtil.CheckRectangle(e.getPoint(), legend_rect))) {
					ChangeCursor(cmp.side_panel.IsSelectingTarget() ? CursorType.TARGET : CursorType.HAND);
				} else {
					ChangeCursor(CursorType.DEFAULT);
				}
			}
        });
	}
	
	public void ChangeLayer() {
		ArrayList<Pair<String, Color>> legend = GetLegend();
		legend_rect = null;
		if (legend != null) {
			legend_rect = new Rectangle(getWidth() - legendWidth - 10, getHeight() - legendHeight * legend.size() - 10,
					legendWidth, legendHeight * legend.size());
		}
		repaint();
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);
		
		TileDrawer tile_drawer = new TileDrawer(g, tile_util);
		for (Tile tile : data.GetConstData().tiles) {
        	Color[] border_colors = new Color[6];
        	for (int i = 0; i < 6; i++) {
        		if (tile.border_has_river[i]) {
        		  border_colors[i] = ColorUtil.GetRiverColor();
        		} else if (data.GetConfig().map_layer == 0) {
        			Tile neighbor = data.GetConstData().GetTile(TileUtil.GetNeighborPosition(tile.coordinate, i));
        			City city = data.GetCityTerritoryByTile(tile);
        			if (neighbor == null || neighbor.county == tile.county || city == null) continue;
        			City neighbor_city = data.GetCityTerritoryByTile(neighbor);
        			border_colors[i] = (neighbor_city != null && neighbor_city.GetOwner() == city.GetOwner()
        					&& neighbor_city.GetOwner().Playable()) ? Color.LIGHT_GRAY : Color.BLACK;
        		}
        	}
			tile_drawer.DrawOneTile(tile.coordinate, GetTileColor(tile), border_colors);
			if (scale >= 45) {
				ArrayList<Army> armies = data.GetArmiesByPosition(tile.coordinate);
				if (!armies.isEmpty()) {
					TextWriter text_writer = new TextWriter(g);
					text_writer.SetAlignment(Alignment.CENTER);
					text_writer.SetFontSize(9);
					text_writer.SetColor(GetFontColor(tile));
					Point p = tile_util.GetAnchorPoint(tile.coordinate);
					ShapeDrawer shape_drawer = new ShapeDrawer(g);
					
					int left = 0;
					int right = 0;
					int y = p.y + tile_util.cell_height * 3 / 4;
					if (armies.size() > 1) {
						left = p.x + tile_util.cell_width / 4;
						right = p.x + tile_util.cell_width * 3 / 4;
					}
					for (int i = 0; i < armies.size(); ++i) {
						Army army = armies.get(i);
						Color font_color = GetFontColor(tile);
						int x = p.x + tile_util.cell_width / 2;
						if (armies.size() > 1) {
							x = left + (right - left) * i / (armies.size() - 1);
						}
						if (scale >= 50) {
							Position next_tile = army.GetNextTile();
							if (next_tile != null) {
								g.setColor(font_color);
								Point o = tile_util.GetAnchorPoint(next_tile);
								shape_drawer.SetArrowLength((int) (scale * 0.1));
								shape_drawer.DrawArrow(x, y, o.x + x - p.x, o.y + y - p.y, scale * 0.2);
							}
						}
						DrawArmy(g, font_color, army, x, y, text_writer, shape_drawer);
					}
				}
			}
		}		
		
		TextWriter text_writer = new TextWriter(g);
		text_writer.SetAlignment(Alignment.CENTER);
		text_writer.SetFontSize(8);
		for (City city : data.GetAllCities()) {
			text_writer.SetColor(GetFontColor(data.GetConstData().GetTile(city.GetCoordinate())));
			Point p = tile_util.GetAnchorPoint(city.GetCoordinate());
			if (scale >= 24) {
				int y = scale >= 45 ? p.y + tile_util.cell_height / 4 : p.y + tile_util.cell_height / 3;
				text_writer.DrawString(p.x + tile_util.cell_width / 2, y, city.GetName());
			}
			if (scale >= 30 || (scale < 24 && scale >= 15)) {
				int y = scale >= 30 ? p.y - tile_util.cell_height / 6 : p.y + tile_util.cell_height / 2;
				if (scale >= 30 || city.GetType() == CityType.CAPITAL) {
					text_writer.DrawString(p.x + tile_util.cell_width / 2, y, Texts.cityTypeSymbol[city.GetType().ordinal()]);
				}
			}
		}
		
		
		ArrayList<Pair<String, Color>> legend = GetLegend();
		if (legend != null) {
			g.setColor(Colors.LIGHTER_GREY);
			g.fillRoundRect(legend_rect.x, legend_rect.y, legend_rect.width, legend_rect.height, 10, 10);
			g.setColor(Color.BLACK);
			g.drawRoundRect(legend_rect.x, legend_rect.y, legend_rect.width, legend_rect.height, 10, 10);
			text_writer.SetAlignment(Alignment.RIGHT_MIDDLE);
			text_writer.SetFontSize(12);
			for (int i = 0; i < legend.size(); ++i) {
				Pair<String, Color> pair = legend.get(i);
				g.setColor(pair.second);
				g.fillRoundRect(legend_rect.x + 5, legend_rect.y + i * legendHeight + 5, 40, legendHeight - 10, 5, 5);
				g.setColor(Color.BLACK);
				g.drawRoundRect(legend_rect.x + 5, legend_rect.y + i * legendHeight + 5, 40, legendHeight - 10, 5, 5);
				text_writer.DrawString(legend_rect.x + legendWidth - 5, legend_rect.y + i * legendHeight + legendHeight / 2, pair.first);
			}
		}
	}
	
	private void DrawArmy(Graphics g, Color tile_color, Army army, int x, int y, TextWriter text_writer, ShapeDrawer shape_drawer) {
		g.setColor(tile_color);
		shape_drawer.DrawCircle(x, y, 7);
		g.setColor(Color.LIGHT_GRAY);
		shape_drawer.DrawCircle(x, y, 6);
		g.setColor(ColorUtil.GetStateBackgroundColor(army.GetState().ColorIndex()));
		shape_drawer.DrawCircle(x, y, 5);
		text_writer.SetColor(ColorUtil.GetStateForegroundColor(army.GetState().ColorIndex()));
		text_writer.DrawString(x, y, army.GetId());
	}
	
	private Position GetPosition(Point p) {
		return tile_util.GetCityPosition(p);
	}
	
	private Tile GetTile(Point p) {
		return data.GetConstData().GetTile(GetPosition(p));
	}
	
	private City GetCity(Position p) {
		return data.GetCityTerritoryByTile(data.GetConstData().GetTile(p));
	}
	
	private Color GetTileColor(Tile tile) {
		Color color = ColorUtil.GetTerrainColor(tile.terrain);
		if (data.GetConfig().map_layer == 1) return color;
		City city = data.GetCityTerritoryByTile(tile);
		if (city == null) return color;
		if (data.GetConfig().map_layer == 2) {
			int rain = city.GetNaturalInfo().GetRainLevelOrDefault();
			return kRainLegend.get(rain).second;
		}
		if (data.GetConfig().map_layer == 3) {
			int temp = city.GetNaturalInfo().GetTemperatureLevelOrDefault();
			return kTemperatureLegend.get(temp - 1).second;
		}
		if (data.GetConfig().map_layer == 4) {
			if (city.GetNaturalInfo().GetFloodSeverity() > 0) return kDisasterLegend.get(0).second;
			else if (city.GetNaturalInfo().GetLocustSeverity() > 0) return kDisasterLegend.get(1).second;
			return ColorUtil.GetStateBackgroundColor(0);
		}
		State state = city.GetOwner();
		if (state == null) return color;
		return ColorUtil.GetStateBackgroundColor(state.ColorIndex());
	}
	
	private Color GetFontColor(Tile tile) {
		Color color = Color.BLACK;
		if (data.GetConfig().map_layer > 0) return color;
		City city = data.GetCityTerritoryByTile(tile);
		if (city == null) return color;
		State state = city.GetOwner();
		if (state == null) return color;
		return ColorUtil.GetStateForegroundColor(state.ColorIndex());
	}	
	
	private int GetScaledMapWidth() {
		return (int) (tile_util.cell_width * (ConstGameData.kMapWidth - 0.5));
	}
	
	private int GetScaledMapHeight() {
		return (int) ((tile_util.cell_height + tile_util.cell_slope_height) * (ConstGameData.kMapHeight - 0.5));
	}
	
	private void RestrictOffest() {
		GeometryUtil.RestrictPoint(offset, getWidth() - GetScaledMapWidth(), getHeight() - GetScaledMapHeight(), 0, 0);
	}
	
	private ArrayList<Pair<String, Color>> GetLegend() {
		int layer = data.GetConfig().map_layer;
		if (layer == 1) return kTerrainLegend;
		if (layer == 2) return kRainLegend;
		if (layer == 3) return kTemperatureLegend;
		if (layer == 4) return kDisasterLegend;
		return null;
	}
	
	private double scale = 12;
	private Point offset = new Point(0, 0);
	private Point prev = null;
	private TileGeometry tile_util = new TileGeometry(0, 0, (int) scale);	
	private static int legendWidth = 100;
	private static int legendHeight = 20;
	private Rectangle legend_rect;
	
	private static ArrayList<Pair<String, Color>> kTerrainLegend = new ArrayList<Pair<String, Color>>(){
		private static final long serialVersionUID = 1L; {
			add(new Pair<String, Color>(Texts.plain, ColorUtil.GetTerrainColor(Terrain.PLAIN)));
			add(new Pair<String, Color>(Texts.hill, ColorUtil.GetTerrainColor(Terrain.HILL)));
			add(new Pair<String, Color>(Texts.mountain, ColorUtil.GetTerrainColor(Terrain.MOUNTAIN)));
			add(new Pair<String, Color>(Texts.highMountain, ColorUtil.GetTerrainColor(Terrain.HIGH_MOUNTAIN)));
			add(new Pair<String, Color>(Texts.swamp, ColorUtil.GetTerrainColor(Terrain.SWAMP)));
			add(new Pair<String, Color>(Texts.desert, ColorUtil.GetTerrainColor(Terrain.DESERT)));
	}};
	
	private static ArrayList<Pair<String, Color>> kRainLegend = new ArrayList<Pair<String, Color>>(){
		private static final long serialVersionUID = 1L; {
			add(new Pair<String, Color>(String.format("%s x 0", Texts.rainLevel), Color.RED));
			add(new Pair<String, Color>(String.format("%s x 1", Texts.rainLevel), Color.ORANGE));
			add(new Pair<String, Color>(String.format("%s x 2", Texts.rainLevel), Color.YELLOW));
			add(new Pair<String, Color>(String.format("%s x 3", Texts.rainLevel), Color.GREEN));
			add(new Pair<String, Color>(String.format("%s x 4", Texts.rainLevel), Colors.LIGHT_BLUE));
			add(new Pair<String, Color>(String.format("%s x 5", Texts.rainLevel), Color.BLUE));
	}};
	
	private static ArrayList<Pair<String, Color>> kTemperatureLegend = new ArrayList<Pair<String, Color>>(){
		private static final long serialVersionUID = 1L; {
			add(new Pair<String, Color>(String.format("%s x 1", Texts.temperatureLevel), Colors.LIGHT_BLUE));
			add(new Pair<String, Color>(String.format("%s x 2", Texts.temperatureLevel), Color.GREEN));
			add(new Pair<String, Color>(String.format("%s x 3", Texts.temperatureLevel), Color.YELLOW));
			add(new Pair<String, Color>(String.format("%s x 4", Texts.temperatureLevel), Color.ORANGE));
			add(new Pair<String, Color>(String.format("%s x 5", Texts.temperatureLevel), Color.RED));
	}};
	
	private static ArrayList<Pair<String, Color>> kDisasterLegend = new ArrayList<Pair<String, Color>>(){
		private static final long serialVersionUID = 1L; {
			add(new Pair<String, Color>(Texts.flood, Color.BLUE));
			add(new Pair<String, Color>(Texts.locust, Color.RED));
	}};
}
