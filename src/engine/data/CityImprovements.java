package ac.engine.data;

import ac.data.CityData;
import ac.data.base.Date;
import ac.data.constant.ConstCityData;
import ac.data.constant.Improvement;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Improvement.SpecialImprovementType;

public class CityImprovements extends Data {

	protected CityImprovements(DataAccessor accessor, City city, CityData.ImprovementData data, ConstCityData const_data) {
		super(accessor);
		this.city = city;
		this.data = data;
		this.const_data = const_data;
	}
	
	public int GetCount(ImprovementType type) {
		switch (type) {
		case FARM:
		case AQEDUCTED_FARM:
		case IRRIGATED_FARM:
		case OXED_FARM:
		case IRONED_FARM:
			return data.agriculture_improvements[type.ordinal() - Improvement.kAgricultureImprovements[0].ordinal()];		
		case CHINA:
		case FISH:
		case IRON_MINE:
		case MINE:
		case PASTURE:
		case SALT:
		case SILK:
			return data.industry_improvements[type.ordinal() - Improvement.kIndustryImprovements[0].ordinal()];
		case DAM:
		case WALL:
		case SPECIAL:
			return data.auxiliary_improvements[type.ordinal() - Improvement.kAuxiliaryImprovements[0].ordinal()];
		case NONE:
		case WORKSHOP:
		default:
			break;
		}
		return 0;
	}
	
	public int GetMaxCount(ImprovementType type) {
		switch (type) {
		case FARM:
		case OXED_FARM:
		case IRONED_FARM:
			return const_data.max_farm + const_data.max_irrigated_farm;
		case AQEDUCTED_FARM:
			return accessor.GetOverrides().IsCityBenefittedFromSpecialAqeduct(city) ? 0 : const_data.max_farm;
		case IRRIGATED_FARM:
			return accessor.GetOverrides().IsCityBenefittedFromSpecialAqeduct(city) ? const_data.max_farm + const_data.max_irrigated_farm : 0;
		case CHINA:
			return const_data.max_china;
		case FISH:
			return const_data.max_fish;
		case IRON_MINE:
			return const_data.max_ironmine;
		case MINE:
			return const_data.max_mine;
		case PASTURE:
			return const_data.max_pasture;
		case SALT:
			return const_data.max_salt;
		case SILK:
			return const_data.max_silk;
		case DAM:
			return GetCount(ImprovementType.IRRIGATED_FARM);
		case WALL:
		case SPECIAL:
			if (type == ImprovementType.SPECIAL && GetSpecialImprovement() == SpecialImprovementType.NONE) return 0;
			return accessor.GetParam().max_auxiliary_improvments[type.ordinal() - Improvement.kAuxiliaryImprovements[0].ordinal()];
		case NONE:
		case WORKSHOP:
		default:
			break;
		}
		return 0;		
	}
	
	public void IncrementCount(ImprovementType type) {
		if (accessor.GetOverrides().IsCityBenefittedFromSpecialAqeduct(city) && type == ImprovementType.AQEDUCTED_FARM) {
			type = ImprovementType.FARM;
		}
		ChangeCount(type, 1);
	}
	
	public void DecreaseCount(ImprovementType type) {
		if (GetCount(type) > 0) ChangeCount(type, -1);
	}
	
	public SpecialImprovementType GetSpecialImprovement() {
		return const_data.special_improvement;
	}
	
	public SpecialImprovementType GetFinishedSpecialImprovement() {
		if (GetCount(ImprovementType.SPECIAL) == GetMaxCount(ImprovementType.SPECIAL)) return GetSpecialImprovement();
		return SpecialImprovementType.NONE;
	}
	
	public ImprovementType GetCurrentConstruction() {
		return data.construction;
	}
	
	public Date GetConsturctionCompleteDate() {
		return data.complete_date;
	}
	
	public void SetCurrentConstruction(ImprovementType type, int construction_days) {
		data.construction = type;
		data.complete_date = accessor.GetDate().CreateDate(construction_days);
	}
	
	public void ResetConstruction() {
		data.construction = null;
		data.complete_date = null;
	}
	
	private void ChangeCount(ImprovementType type, int inc) {
		switch (type) {
		case FARM:
			if (inc < 0) {
				data.agriculture_improvements[ImprovementType.FARM.ordinal() - Improvement.kAgricultureImprovements[0].ordinal()] += inc;
				break;
			}
			if (GetCount(ImprovementType.IRRIGATED_FARM) + inc <= GetMaxCount(ImprovementType.IRRIGATED_FARM)) {
				data.agriculture_improvements[ImprovementType.IRRIGATED_FARM.ordinal() - Improvement.kAgricultureImprovements[0].ordinal()] += inc;
			} else {
				data.agriculture_improvements[ImprovementType.FARM.ordinal() - Improvement.kAgricultureImprovements[0].ordinal()] += inc;
			}
			break;
		case IRRIGATED_FARM:
			if (inc < 0) {
				data.agriculture_improvements[ImprovementType.IRRIGATED_FARM.ordinal() - Improvement.kAgricultureImprovements[0].ordinal()] += inc;
			}
			break;
		case OXED_FARM:
		case IRONED_FARM:
			data.agriculture_improvements[type.ordinal() - Improvement.kAgricultureImprovements[0].ordinal()] += inc;
			break;
		case AQEDUCTED_FARM:
			data.agriculture_improvements[ImprovementType.FARM.ordinal() - Improvement.kAgricultureImprovements[0].ordinal()] -= inc;
			data.agriculture_improvements[ImprovementType.AQEDUCTED_FARM.ordinal() - Improvement.kAgricultureImprovements[0].ordinal()] += inc;
			break;
		case CHINA:
		case FISH:
		case IRON_MINE:
		case MINE:
		case PASTURE:
		case SALT:
		case SILK:
			data.industry_improvements[type.ordinal() - Improvement.kIndustryImprovements[0].ordinal()] += inc;
			break;
		case DAM:
		case WALL:
		case SPECIAL:
			data.auxiliary_improvements[type.ordinal() - Improvement.kAuxiliaryImprovements[0].ordinal()] += inc;
			break;
		case NONE:
		case WORKSHOP:
		default:
			break;
		}
	}

	private City city;
	private CityData.ImprovementData data;
	private ConstCityData const_data;
}
