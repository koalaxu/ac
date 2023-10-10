package ac.engine.data;

import ac.data.constant.ConstStateData;
import ac.util.StringUtil;

public class StateDescription extends Data {
	protected StateDescription(DataAccessor accessor, ConstStateData data) {
		super(accessor);
		this.data = data;
	}
	
	public String OfficialName() {
		return StringUtil.IfNull(data.official_name, data.name);
	}
	
	public String Alias() {
		return StringUtil.IfNullEmpty(data.alias);
	}
	
	public String Nobility() {
		return StringUtil.IfNullEmpty(data.nobility);
	}
	
	public String Race() {
		return accessor.GetConstData().races.get(data.race).name;
	}
	
	public String FamilyName() {
		return StringUtil.IfNullEmpty(data.family_name);
	}
	
	public boolean IsNonHuaxiaRace() {
		return data.race > 0;
	}
	
	private ConstStateData data;
}
