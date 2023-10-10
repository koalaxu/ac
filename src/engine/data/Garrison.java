package ac.engine.data;

import java.util.TreeMap;

import ac.data.ArmyData;
import ac.data.base.Position;
import ac.data.constant.ConstCityData;
import ac.data.constant.Texts;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;
import ac.util.MathUtil;

public class Garrison extends Army {

	protected Garrison(DataAccessor accessor, City city, ArmyData data) {
		super(accessor, city.GetOwner(), -1, data);
		this.city = city;
	}
	
	@Override
	public String GetName() {
		return city.GetName() + Texts.garrison;
	}
	
	@Override
	public boolean IsGarrison() {
		return true;
	}
	
	@Override
	public Person GetGeneral() {
		return city.GetGovernor();
	}
	
	@Override
	public State GetState() {
		return city.GetOwner();
	}
	
	public long GetMaxSoldier() {
		return city.Get().military.garrison_max;
	}
	
	public long GetTotalSoldier() {
		return ComputeTypedSoldier(UnitType.ARCHERY);
	}
	
	public TreeMap<Unit, Long> GetUnitQuantities() {
		return GetUnitQuantities(UnitType.ARCHERY);
	}
	
	public void UpdateGarrisonMax() {
		city.Get().military.garrison_max = (long) Math.max(city.Get().military.garrison_max,
				MathUtil.FlooredByHundred((long) (city.GetTotalPopulation() * ConstCityData.kGarrisonRatio)));
	}
	
	@Override
	public City GetBaseCity() {
		return city;
	}
	
	@Override
	public Position GetPosition() {
		return city.GetCoordinate();
	}

	private City city;
}
