package ac.util;

import java.awt.Point;
import java.awt.Rectangle;

public class GeometryUtil {
	public static void RestrictPoint(Point p, int left, int top, int right, int bottom) {
		p.x = Math.min(right, Math.max(left, p.x));
		p.y = Math.min(bottom, Math.max(top, p.y));
	}
	
	public static boolean CheckRectangle(Point p, Rectangle rect) {
		return p.x > rect.x && p.y > rect.y && p.x < rect.x + rect.width && p.y < rect.y + rect.height;
	}
	
	public static Point GetCenter(Rectangle rect) {
		return new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
	}
	
	public static double Distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}
}
