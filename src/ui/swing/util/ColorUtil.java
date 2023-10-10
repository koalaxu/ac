package ac.ui.swing.util;

import java.awt.Color;
import java.util.HashMap;

import ac.data.constant.Colors;
import ac.data.constant.Tile;
import ac.data.constant.Tile.Terrain;


public class ColorUtil {
	public static Color GetTerrainColor(Terrain terrain) {
		return kTerrainColor.getOrDefault(terrain, Color.LIGHT_GRAY);
	}
	
	public static Color GetRiverColor() {
		return GetTerrainColor(Terrain.SEA);
	}
	
	public static Color GetStateForegroundColor(int index) {
		return kStateForegroundColor[index < 0 ? 0 : index];
		//return kStateForegroundColor[index < 0 ? 0 : index % (kStateForegroundColor.length - 1) + 1];
	}
	
	public static Color GetStateBackgroundColor(int index) {
		return kStateBackgroundColor[index < 0 ? 0 : index];
		//return kStateBackgroundColor[index < 0 ? 0 : index % (kStateBackgroundColor.length - 1) + 1];
	}	
	
	private final static HashMap<Tile.Terrain, Color> kTerrainColor = new HashMap<Tile.Terrain, Color>() {
		private static final long serialVersionUID = 1L;
	{
		put(Terrain.PLAIN, new Color(239,239,128)); put(Terrain.SWAMP, new Color(128,192,128));
		put(Terrain.HILL, new Color(192,192,128)); put(Terrain.MOUNTAIN, new Color(128,128,63));
		put(Terrain.SEA, new Color(127,127,255)); put(Terrain.FORREST, new Color(63,128,63));
		put(Terrain.HIGH_MOUNTAIN, new Color(63,63,63)); put(Terrain.DESERT, new Color(255,255,239));
	}};
	
	public final static Color[] kStateForegroundColor = {
			Color.BLACK, Color.WHITE, Color.WHITE, Color.BLACK,
			Color.BLACK, Color.BLACK, Color.WHITE, Color.BLACK,
			Color.BLACK, Color.WHITE, Color.BLACK, Color.BLACK,
			Color.WHITE, Color.WHITE, Color.BLACK, Color.WHITE,
			Color.BLACK, Color.WHITE, Color.WHITE, Color.WHITE,
			Color.BLACK, Color.BLACK, Color.WHITE, Color.BLACK,
			Color.WHITE, Color.BLACK, Color.WHITE, Color.WHITE,
	};
	public static Color[] kStateBackgroundColor = {
			Color.LIGHT_GRAY, Color.BLACK, Color.BLUE, Color.ORANGE,
			Color.CYAN, Color.WHITE, Colors.DARK_PURPLE, Colors.RED_ORANGE,
			Color.PINK, Color.MAGENTA, Colors.LIGHT_GREEN, Color.YELLOW,
			Colors.DARK_GREEN, Colors.DARK_RED, Colors.LIGHTER_GREEN, Colors.DARK_PINK,
			Colors.LIGHTER_YELLOW, Colors.DARKER_GREEN, Colors.DARK_BLUE, Color.RED,
			Colors.DARK_YELLOW, Colors.LIGHTER_BLUE, Colors.DARKER_RED, Color.GREEN,
			Colors.YELLOW_ORANGE, Colors.LIGHT_RED, Colors.LIGHT_PURPLE, Colors.DARKER_BLUE,
	};
}