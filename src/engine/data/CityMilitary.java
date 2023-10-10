package ac.engine.data;

import ac.data.ArmyData.SoldierType;
import ac.data.CityData.MilitaryData;
import ac.data.CityData.RecruitmentData;
import ac.data.base.Date;
import ac.data.constant.ConstGameData;
import ac.data.constant.Unit;

public class CityMilitary extends Data {

	protected CityMilitary(DataAccessor accessor, City city) {
		super(accessor);
		this.city = city;
		this.garrison = new Garrison(accessor, city, city.Get().military.garrison);
		data = city.Get().military;
	}
	
	public Garrison GetGarrison() {
		return garrison;
	}
	
	public static class RecruitmentInfo {
		protected RecruitmentInfo(City city, ConstGameData const_data, RecruitmentData data) {
			army = city.GetOwner().GetMilitary().GetArmy(data.army);
			unit = const_data.units.get(data.unit);
			type = data.soldier_type;
		}
		public Army army;
		public Unit unit;
		public SoldierType type;
	}
	
	public RecruitmentInfo GetRecruitmentInfo() {
		if (data.recruiting == null) return null;
		return new RecruitmentInfo(city, accessor.GetConstData(), data.recruiting);
	}
	
	public Date GetRecruitmentCompleteDate() {
		return data.complete_date;
	}
	
	public double GetAdvancedUnitRatio() {
		return (double)data.advanced_unit_pct / 100;
	}
	
	public int GetAdvancedUnitPercent() {
		return data.advanced_unit_pct;
	}
	
	public void SetCurrentRecruitment(Army army, Unit unit, SoldierType soldier_type, int recruitment_days) {
		data.recruiting = new RecruitmentData();
		data.recruiting.army = army.id;
		data.recruiting.unit = accessor.GetConstData().GetUnitIndex(unit);
		data.recruiting.soldier_type = soldier_type;
		data.complete_date = accessor.GetDate().CreateDate(recruitment_days);
	}
	
	public void ResetRecruitment() {
		data.recruiting = null;
		data.complete_date = null;
	}
	
	public void SetAdvancedUnitPercent(int pct) {
		data.advanced_unit_pct = pct;
	}
	
	private City city;
	private MilitaryData data;
	private Garrison garrison;
}
