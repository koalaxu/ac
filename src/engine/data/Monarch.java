package ac.engine.data;

import ac.data.PersonData;
import ac.data.constant.ConstPersonData;
import ac.data.constant.Role.RoleType;
import ac.util.StringUtil;

public class Monarch extends Person {

	protected Monarch(DataAccessor accessor, int id, PersonData data, ConstPersonData const_data) {
		super(accessor, id, data, const_data);
	}
	
	@Override
	public String GetName() {
		if (IsFake()) {
			return StringUtil.IfNullEmpty(data.family_name) + data.given_name;
		}
		if (!GetSurname().isEmpty()) {
			return GetSurname() + GetGivenName();
		}
		if (!GetFamilyName().isEmpty() && !GetGivenName().isEmpty()) {
			return GetFamilyName() + GetGivenName();
		}
		if (!GetGivenName().isEmpty()) {
			return GetGivenName();
		}
		return GetState().GetDescription().OfficialName() + GetPosthumousName() + GetState().GetDescription().Nobility();
	}
	
	public int GetEarliestAvailableYear() {
		return Math.min(const_data.available, const_data.death - accessor.GetParam().earlist_available_year_prior_to_death);
	}
	
	public void InheritFrom(Monarch ancestor) {
		if (ancestor == null) {
			data.family_name = GetOwner().GetDescription().FamilyName();
			return;
		}
		if (ancestor.const_data == null) {
			data.family_name = ancestor.data.family_name;
			return;
		}
		if (!ancestor.GetSurname().isEmpty()) {
			data.family_name = ancestor.GetSurname();
		} else {
			data.family_name = ancestor.GetFamilyName();
		}
	}
	
	@Override
	public RoleType GetRoleType() {
		return RoleType.KING;
	}	

	protected State GetState() {
		if (const_data == null) return GetOwner();
		return accessor.GetState(const_data.state);
	}
}
