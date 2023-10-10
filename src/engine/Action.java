package ac.engine;

import ac.engine.data.Data;
import ac.util.StringUtil;

public class Action {
	public Action(ActionType type) {
		this.type = type;
	}
	public enum ActionType {
		// City
		APPEASE_SUBJECT_WITH_FOOD,
		CONSTRUCT_IMPROVEMENT,
		ALLOCATE_PROFESSION,
		CHANGE_ADVANCED_UNIT_PERCENTAGE,
		// State
		CHANGE_FOOD_TAX,
		ALLOCATE_BUDGET,
		RESEARCH_TECHNOLOGY_TYPE,
		HIRE_OFFICER,
		ASSIGN_MINISTER,
		ASSIGN_GENERAL,
		ASSIGN_GOVERNOR,
		ADOPT_POLICY,
		RESPOND_TO_TREATY,
		// ARMY
		RECRUIT_ARMY,
		REBASE_ARMY,
		TARGET_ARMY,
		TARGET_CITY,
		RESET_TARGET,
	}
	public ActionType type;
	public Data object;
	public Data object2;
	public Object object3;
	public Number quantity;
	public Number quantity2;
	public Number quantity3;
	public Number quantity4;
	public boolean flag;
	
	public String toString() {
		return "Action: " + type.toString() + " Object: " + object.toString()
			+ StringUtil.IfNullEmpty(object2) + StringUtil.IfNullEmpty(object3)
			+ StringUtil.IfNullEmpty(quantity) + StringUtil.IfNullEmpty(quantity2) + StringUtil.IfNullEmpty(quantity3) + StringUtil.IfNullEmpty(quantity4);
	}
}
