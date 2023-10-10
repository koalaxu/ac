package ac.engine.data;

import java.util.ArrayList;

import ac.data.StateData.TechnologyData;
import ac.data.constant.Technology;
import ac.data.constant.Technology.Effect;
import ac.data.constant.Technology.TechnologyType;
import ac.data.constant.Unit;
import ac.data.constant.Unit.UnitType;

public class StateTechnology extends Data {

	protected StateTechnology(DataAccessor accessor, TechnologyData data) {
		super(accessor);
		this.data = data;
	}
	
	public boolean Has(Technology tech) {
		if (tech == null) return true;
		return data.obtained[tech.type.ordinal()] >= tech.index;
//		if (tech == Technology.NONE) return true;
//		Technology cur = GetCurrentTechnology(Technologies.technology_types.get(tech));
//		return cur.ordinal() >= tech.ordinal();
	}
	
	public boolean HasEffect(Effect effect) {
		return Has(accessor.GetConstData().tech_effects.get(effect));
	}
	
	public void SetResearchingTechnologyType(TechnologyType type) {
		data.researching = type;
		data.progress = 0;
	}
	
	public TechnologyType GetResearchingTechnologyType() {
		return data.researching;
	}
	
	public Technology GetResearchingTechnology() {
		TechnologyType type = GetResearchingTechnologyType();
		return GetTypedTechnology(type, data.obtained[type.ordinal()] + 1);
	}
	
	public long GetResearchProgress() {
		return data.progress;
	}
	
	public boolean IncreaseResearchProgress(int add) {
		data.progress += add;
		//if (data.progress >= accessor.GetParam().GetTechnologyCost(GetResearchingTechnology())) {
		Technology tech = GetResearchingTechnology();
		if (tech != null && data.progress >= tech.cost) {
			data.obtained[GetResearchingTechnologyType().ordinal()] += 1;
			data.progress = 0;
			return true;
		}
		return false;
	}
	
	public Technology GetCurrentTechnology(TechnologyType type) {
		return GetTypedTechnology(type, data.obtained[type.ordinal()]);
	}
	
	public Unit GetMostAdvancedUnit(UnitType type) {
		ArrayList<Unit> units = accessor.GetConstData().typed_units.get(type.ordinal());
		for (int i = units.size() - 1; i >= 0; --i) {
			Unit unit = units.get(i);
			if (Has(accessor.GetConstData().unit_techs.get(unit))) {
				return unit;
			}
		}
		return units.get(0);
	}
	
	public Unit GetSecondAdvancedUnit(UnitType type) {
		ArrayList<Unit> units = accessor.GetConstData().typed_units.get(type.ordinal());
		int rank = 1;
		for (int i = units.size() - 1; i >= 0; --i) {
			Unit unit = units.get(i);
			if (Has(accessor.GetConstData().unit_techs.get(unit))) {
				if (rank == 2) return unit;
				rank++;
			}
		}
		return units.get(0);
	}
	
	private Technology GetTypedTechnology(TechnologyType type, int index) {
		if (index >= accessor.GetConstData().typed_techs.get(type).size() || index < 0) return null;
		return accessor.GetConstData().typed_techs.get(type).get(index);
//		if (index >= Technologies.typed_technology[type.ordinal()].length) return null;
//		return Technologies.typed_technology[type.ordinal()][index];
	}

	private TechnologyData data;
}
