package ac.ui.swing.util;

import java.awt.Point;

import ac.data.base.Position;

public class TileGeometry {
	public TileGeometry(int width_offset, int height_offset, int scale) {
		Reset(width_offset, height_offset, scale);
	}
	
	public void Reset(int width_offset, int height_offset, int scale) {
		cell_width = scale;
		cell_height = (int) Math.floor((double)scale / Math.sqrt(3) + 0.5);
		cell_slope_width = scale / 2;
		cell_slope_height = (int) Math.floor((double)scale / Math.sqrt(3) / 2.0 + 0.5);
		offset_x = width_offset - cell_width / 2;
		offset_y = height_offset - cell_slope_height;
	}
	
	public Point GetAnchorPoint(Position position) {
		Point p = new Point(position.x * cell_width + offset_x,
				position.y * (cell_height + cell_slope_height) + offset_y + cell_slope_height);
		if ((position.y % 2) == 1) {
			p.x += cell_slope_width;
		}
		return p;
	}
	
	public Position GetCityPosition(Point point) {
		int x = point.x - offset_x;
		int y = point.y - offset_y;
		int line = y / (cell_height + cell_slope_height);
		if ((line % 2) == 1) x -= cell_slope_width;
		Position ret = new Position(x / cell_width, line);		
		if (y - line * (cell_height + cell_slope_height) < cell_slope_height) { // "Head"
			y -= line * (cell_height + cell_slope_height);
			int width = y * cell_slope_width / cell_slope_height;
			int column = x / cell_width;
			x -= column * cell_width;
			if (x < cell_slope_width - width ) { // UpLeft
				if ((ret.y % 2) == 0) ret.x -= 1;
				ret.y = line - 1;
			} else if (x > cell_slope_width + width) { // UpRight
				if ((ret.y % 2) == 1) ret.x += 1;
				ret.y = line - 1;					
			}
		}		
		return ret;
	}

	public int offset_x;
	public int offset_y;
	public int cell_width;
	public int cell_height;
	public int cell_slope_width;
	public int cell_slope_height;
}
