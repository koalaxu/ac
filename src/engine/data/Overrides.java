package ac.engine.data;

import ac.data.OverrideData;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Improvement.SpecialImprovementType;

public class Overrides extends Data {

	protected Overrides(DataAccessor accessor, OverrideData override) {
		super(accessor);
		this.override = override;
	}
	
	public void ApplySpecialImprovement(SpecialImprovementType type) {
		if (type == null) return;
		if (type == SpecialImprovementType.LING_AQEDUCT) {
			override.is_ling_aqeduct_built = true;
		}
		for (int city_id : accessor.GetParam().special_aqeduct_benefitting_cities[type.ordinal()]) {
			override.special_aqeduct_benefitted_cities.add(city_id);
			City city = accessor.cities.get(city_id);
			CityImprovements city_impr = city.GetImprovements();
			ChangeFarmsToIrrigated(city_impr, ImprovementType.FARM);
			ChangeFarmsToIrrigated(city_impr, ImprovementType.AQEDUCTED_FARM);
		}
	}
	
	public boolean IsCityBenefittedFromSpecialAqeduct(City city) {
		return override.special_aqeduct_benefitted_cities.contains(city.id);
	}
	
	public boolean IsLingAqudectBuilt() {
		return override.is_ling_aqeduct_built;
	}
	
	private void ChangeFarmsToIrrigated(CityImprovements city_impr, ImprovementType type) {
		int farm = city_impr.GetCount(type);
		for (int i = 0; i < farm; ++i) {
			city_impr.DecreaseCount(type);
			city_impr.IncrementCount(ImprovementType.IRRIGATED_FARM);
		}
	}

	private OverrideData override;
}
