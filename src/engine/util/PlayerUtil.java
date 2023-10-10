package ac.engine.util;

import java.util.ArrayList;

import ac.data.ArmyData.SoldierType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.engine.data.CityMilitary.RecruitmentInfo;

public class PlayerUtil extends BaseUtil {
	protected PlayerUtil(DataAccessor data, Utils utils) {
		super(data, utils);
	}
	
	public long GetTypedSoldiersInQueue(SoldierType type) {
		long soldiers = 0L;
		for (City city : GetPlayerState().GetOwnedCities()) {
			ArrayList<RecruitmentInfo> recruitment = data.GetPlayer().GetRecruitmentQueue(city);
			for (RecruitmentInfo info : recruitment) {
				if (info.type == type) soldiers += param.base_recruitment;
			}
		}
		return soldiers;
	}
	
	public long CountTypedSoldiersInQueue(Army army, long[] typed_soldiers) {
		long soldiers = 0L;
		for (City city : GetPlayerState().GetOwnedCities()) {
			ArrayList<RecruitmentInfo> recruitment = data.GetPlayer().GetRecruitmentQueue(city);
			for (RecruitmentInfo info : recruitment) {
				if (info.army == army) {
					soldiers += param.base_recruitment;
					int unit_type = info.unit.type.ordinal();
					typed_soldiers[unit_type] += param.base_recruitment;
				}
			}
		}
		return soldiers;
	}
	
	private State GetPlayerState() {
		return data.GetPlayer().GetState();
	}
}