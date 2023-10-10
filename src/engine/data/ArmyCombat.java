package ac.engine.data;

import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;

public class ArmyCombat extends Data {

	protected ArmyCombat(DataAccessor accessor) {
		super(accessor);
	}
	
	public double GetAttack(UnitType type) {
		return attack[type.ordinal()];
	}
	
	public double GetDefend(UnitType type) {
		return defend[type.ordinal()];
	}
	
	public double GetSiegeAttack() {
		return siege_attack;
	}
	
	public double GetPowerMultiplier() {
		return power_multiplier;
	}
	
	public long GetKilled(UnitType type) {
		return killed[type.ordinal()];
	}
	
	public boolean GetWallDamaged() {
		return wall_damaged;
	}
	
	public long GetKilled() {
		long total = 0L;
		for (UnitType type : UnitType.values()) {
			total += GetKilled(type);
		}
		return total;
	}
	
	public Army GetTarget() {
		return target;
	}
	
	public boolean HasBattled() {
		return battled;
	}
	
	public void SetAttack(UnitType type, double power) {
		attack[type.ordinal()] = power;
	}
	
	public void SetDefend(UnitType type, double power) {
		defend[type.ordinal()] = power;
	}
	
	public void SetSiegeAttack(double power) {
		siege_attack = power;
	}
	
	public void SetPowerMultiplier(double power) {
		power_multiplier = power;
	}
	
	public void SetKilled(UnitType type, long kill) {
		killed[type.ordinal()] = kill;
	}
	
	public void SetWallDamage() {
		wall_damaged = true;
	}
	
	public void Reset() {
		target = null;
		for (int i = 0; i < Unit.kMaxUnitType; ++i) {
			attack[i] = 0;
			defend[i] = 0;
			killed[i] = 0;
		}
		siege_attack = 0;
		wall_damaged = false;
		power_multiplier = 0;
		battled = false;
	}
	
	public void SetTarget(Army army) {
		target = army;
	}
	
	public void SetBattled() {
		battled = true;
	}
	private double[] attack = new double[Unit.kMaxUnitType];
	private double[] defend = new double[Unit.kMaxUnitType];
	private double siege_attack;
	private long[] killed = new long[Unit.kMaxUnitType];
	private boolean wall_damaged;
	private double power_multiplier;
	private Army target;
	private boolean battled;
}
