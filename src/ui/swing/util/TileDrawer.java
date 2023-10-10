 package ac.ui.swing.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import ac.data.base.Position;
import ac.data.constant.Tile;

public class TileDrawer extends BaseDrawer {
	public TileDrawer(Graphics g, TileGeometry util) {
		super(g);
		this.util = util;
	}
	

	
	public void DrawOneTile(Position pos, Color color, Color[] border_color) {	
		SaveOldColor();
		Point p = util.GetAnchorPoint(pos);
		int[] xpoints = {p.x, p.x + util.cell_slope_width, p.x + util.cell_width,
			p.x + util.cell_width, p.x + util.cell_slope_width, p.x};
		int[] ypoints = {p.y, p.y - util.cell_slope_height, p.y,
			p.y + util.cell_height, p.y + util.cell_height + util.cell_slope_height, p.y + util.cell_height};
		g.setColor(color);
		g.fillPolygon(xpoints, ypoints, 6);
		if (border_color != null && border_color.length == Tile.kBorderOrder.length) {
			for (int k = 0; k < Tile.kBorderOrder.length; k++) {
				if (border_color[k] == null) continue;
				int start = Tile.kBorderOrder[k];
				int end = (start + 1) % 6;
				g.setColor(border_color[k]);
				g.drawLine(xpoints[start], ypoints[start], xpoints[end], ypoints[end]);
			}
		}
		RestoreOldColor();
	}
	

	private TileGeometry util;
}
