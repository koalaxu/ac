package ac.util;

import java.util.ArrayList;

import ac.data.constant.ConstGameData;
import ac.data.base.Position;

public class TileUtil {
	public static ArrayList<Position> GetNeighborPositions(Position pos) {
		ArrayList<Position> neighbors = new ArrayList<Position>();
		for (int i = 0; i < 6; i++) {
			Position neighbor = GetNeighborPosition(pos, i);
			if (neighbor != null) neighbors.add(neighbor);
		}
		return neighbors;
	}
	
	public static Position GetNeighborPosition(Position pos, int direction_index) {
		Position[] direction = (pos.y % 2 == 0) ? kDirection0 : kDirection1;
		Position delta = direction[direction_index];
		int nx = pos.x + delta.x;
		int ny = pos.y + delta.y;
		if (ny >= 0 && ny < ConstGameData.kMapHeight && nx >= 0 && nx < ConstGameData.kMapWidth) {
			return new Position(nx, ny);
		}
		return null;
	}
	
	public static double Distance(Position p1, Position p2) {
		double x1 = p1.x + ((p1.y % 2 == 0) ? 0.0 : 0.5); 
		double x2 = p2.x + ((p2.y % 2 == 0) ? 0.0 : 0.5);
		double y1 = p1.y;
		double y2 = p2.y;
		double dx = (x1 - x2);
		double dy = (y1 - y2) / kAspectRatio;
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	public static final double kAspectRatio = 2 * Math.sqrt(3.0) / 3;
	
	
	private static Position[] kDirection0 = {
		new Position(-1, -1), new Position(0, -1),
		new Position(-1, 0), new Position(1, 0),
		new Position(-1, 1), new Position(0, 1),
	};
	
	private static Position[] kDirection1 = {
		new Position(0, -1), new Position(1, -1),
		new Position(-1, 0), new Position(1, 0),
		new Position(0, 1), new Position(1, 1),
	};
}
