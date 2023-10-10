package ac.data.constant;

import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Policies.Policy;

public class Technology {
	public int index;
	public String name;
	public enum TechnologyType {
		ECONOMIC,
		MILITARY,
		CIVIC,
	}
	public TechnologyType type;
	public double agriculture_boost;
	public double commerce_boost;
	public double tax_boost;
	public int county_bonus;
	public ImprovementType improvement;
	public Policy policy;
	public Ideology ideology;
	public int unit = -1;
	public long cost;
	public int year;
	
	public double fubing;
	public double recruitment;
	
	public enum Effect {
		NONE,
		UNBLOCK_AQEDUCT_SPECIAL_IMPROVEMENT,
		UNBLOCK_GREATWALL,
		UNBLOCK_TAX_RATE_CHANGE,
		UNBLOCK_GARRISON,
		UNLIMIT_WORKER,
		HALVE_DISTANCE_PENALTY_FOR_COUNTY,
		BOOST_SALT_IRON,
		BOOST_HORSE,
		BOOST_SILK,
		BOOST_FISH,
		BOOST_MINE,
		BOOST_CHINA,
		REDUCE_BARBARIAN_PROB,
		REDUCE_LOCUST_DAMAGE,
		BOOST_CONSCRIPTION_COMBAT_POWER,
		REDUCE_LOGISTIC_LABOR,
		BOOST_FOREIGNER_CONVERSION,
	}
	public Effect effect;
}
