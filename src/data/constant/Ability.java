package ac.data.constant;

public class Ability {
	public enum AbilityType {
		MILITARY,
		ADMIN,
		DIPLOMACY,
	}
	
	public static final int kMaxTypes = AbilityType.values().length;
	public static final int kMaxAbility = 10;
}
