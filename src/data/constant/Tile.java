package ac.data.constant;

import ac.data.base.Position;

public class Tile {
	public Position GetPoint() {  return coordinate;  }
	public Position coordinate;
	public enum Terrain {
		PLAIN,
		HILL,
		MOUNTAIN,
		SWAMP,
		RIVER_VALLEY,
		FORREST,
		HIGH_MOUNTAIN,
		SEA,
		DESERT,
	}
	public Terrain terrain = Terrain.PLAIN;
	public boolean[] border_has_river = new boolean[6];
	public int county = -1;
	
	public static final int[] kBorderOrder = {0, 1, 5, 2, 4, 3};
	public static final int[] kReverseBorderOrder = {0, 1, 3, 5, 4, 2};
	public static final int kMaxArmies = ConstStateData.kMaxArmies;
}
