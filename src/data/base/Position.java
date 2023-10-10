package ac.data.base;

public class Position {
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Position() {
		x = 0;
		y = 0;
	}
	
	public Position(Position pos) {
		x = pos.x;
		y = pos.y;
	}	
	
	public int x;
	public int y;
	
    @Override
	public int hashCode() {
		final int prime = 10031;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (x != other.x || y != other.y)
			return false;
		return true;
	}

    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + "]";
    }
    
    public String FormatString() {
    	return x + "," + y;
    }
}
