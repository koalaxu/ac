package ac.data;

import java.util.TreeMap;

import ac.data.base.Date;
import ac.data.base.Position;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;

public class ArmyData {
	public static enum SoldierType {
		CONSCRIPTION,
		FUBING,
		RECRUITMENT,
	}
	public static final int kMaxSoldierTypes = SoldierType.values().length;
	public long[][] typed_soldier_quantities;  // Unit x SoldierType
	public TreeMap<Integer, Double> city_conscriptions;
	public int logistical_city = -1;
	public int logistic_cost = 0;
	public TreeMap<Integer, Long> city_supporting_labors;
	
	public static class UnitDistribution {
		public TreeMap<Integer, Long> unit_quantities = new TreeMap<Integer, Long>();
	}
	public UnitDistribution[] typed_unit_quantities = new UnitDistribution[Unit.kMaxUnitType];
	
	public double training;
	public double morale = 1.0;
	
	public int base_city = -1;
	public static class Target {
		public int city = -1;
		public int army_state = -1;
		public int army_index = -1;
		public boolean pursue = false;
	}
	
	public static class March {
		public int direction = -1;
		public Date arrive_date;
		public Date final_arrive_date;
	}
	
	public Position position;
	public Target target;
	public boolean retreat;
	public March march = new March();
	
	public static ArmyData CreateGarrison() {
		ArmyData data = new ArmyData();
		data.typed_unit_quantities[UnitType.ARCHERY.ordinal()] = new UnitDistribution();
		return data;
	}
	
	public static ArmyData CreateArmy(Position pos) {
		ArmyData data = new ArmyData();
		for (int i = 0; i < data.typed_unit_quantities.length; ++i) {
			data.typed_unit_quantities[i] = new UnitDistribution();
		}
		data.typed_soldier_quantities = new long[Unit.kMaxUnitType][kMaxSoldierTypes];
		data.city_conscriptions = new TreeMap<Integer, Double>();
		data.city_supporting_labors = new TreeMap<Integer, Long>();
		data.position = new Position(pos);
		return data;
	}
	
	private ArmyData() {}
}
