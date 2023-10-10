package ac.data.constant;

public class Improvement {
	public enum SpecialImprovementType {
		NONE,
		GREATWALL,
		FORT,
		ZHENGGUO_AQEDUCT,
		DUJIANGYAN_AQEDUCT,
		XIMEN_AQEDUCT,
		LING_AQEDUCT,
	}
	
	public enum ImprovementType {
		NONE,
		// Agriculture
		FARM,
		AQEDUCTED_FARM,
		IRRIGATED_FARM,
		OXED_FARM,
		IRONED_FARM,
		// Industry
		MINE,
		SILK,
		CHINA,
		FISH,
		SALT,
		IRON_MINE,
		PASTURE,
		WORKSHOP,
		// Auxiliary
		DAM,
		WALL,
		SPECIAL,
	}

	public static final ImprovementType[] kAgricultureImprovements = {
			ImprovementType.FARM, ImprovementType.AQEDUCTED_FARM, ImprovementType.IRRIGATED_FARM, 
			ImprovementType.OXED_FARM, ImprovementType.IRONED_FARM,
	};
	public static final ImprovementType[] kIndustryImprovements = {
			ImprovementType.MINE, ImprovementType.SILK, ImprovementType.CHINA, ImprovementType.FISH,
			ImprovementType.SALT, ImprovementType.IRON_MINE, ImprovementType.PASTURE,
	};
	public static final ImprovementType[] kAuxiliaryImprovements = {
			ImprovementType.DAM, ImprovementType.WALL, ImprovementType.SPECIAL,
	};
	public static final SpecialImprovementType[] kAqeductSpecialImprovements = {
			SpecialImprovementType.ZHENGGUO_AQEDUCT, SpecialImprovementType.DUJIANGYAN_AQEDUCT, SpecialImprovementType.XIMEN_AQEDUCT, SpecialImprovementType.LING_AQEDUCT
	};
}
