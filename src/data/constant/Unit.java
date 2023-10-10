package ac.data.constant;

import ac.data.base.Resource;

public class Unit implements Comparable<Unit> {
	public enum UnitType {
		MELEE,
		ARCHERY,
		MOUNTED,
		SIEGE,
	}
	
	public static int kMaxUnitType = UnitType.values().length;
	
	public int index;
	public String name;
	public UnitType type;
	public int attack;
	public int defend;
	public double speed_multiplier;
	public Resource<Integer> cost = new Resource<Integer>(0);

	@Override
	public int compareTo(Unit o) {
		return index - o.index;
	}
}
