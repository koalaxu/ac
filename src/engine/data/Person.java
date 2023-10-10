package ac.engine.data;

import java.util.Comparator;

import ac.data.PersonData;
import ac.data.base.Date;
import ac.data.constant.Ability;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.ConstPersonData;
import ac.data.constant.Role.AssignmentType;
import ac.data.constant.Role.RoleType;
import ac.util.StringUtil;

public class Person extends IdKeyedData {

	protected Person(DataAccessor accessor, int id, PersonData data, ConstPersonData const_data) {
		super(accessor, id);
		this.data = data;
		this.const_data = const_data;
	}
	
	@Override
	public String GetName() {
		return const_data != null ? const_data.name : data.family_name + data.given_name;
	}
	
	public boolean IsDead() {
		return data.die_time.GetDifference(accessor.GetDate()) < 0;
	}
	
	public int HistoricalDeathYear() {
		return const_data.death;
	}
	
	public int GetAbility(AbilityType type) {
		if (data.ability != null) {
			return data.ability[type.ordinal()];
		}
		switch (type) {
		case ADMIN:
			return const_data.administration;
		case DIPLOMACY:
			return const_data.diplomacy;
		case MILITARY:
			return const_data.military;
		}
		return 0;
	}
	
	public boolean IsFake() {
		return const_data == null;
	}
	
	public String GetFamilyName() {
		if (data.family_name != null) return data.family_name;
		return StringUtil.IfNullEmpty(const_data.family_name);
	}
	
	public String GetSurname() {
		return StringUtil.IfNullEmpty(const_data.surname);
	}
	
	public String GetGivenName() {
		if (data.given_name != null) return data.given_name;
		return StringUtil.IfNullEmpty(const_data.given_name);
	}
	
	public String GetCourtesyName() {
		return StringUtil.IfNullEmpty(const_data.courtesy_name);
	}
	
	public String GetPosthumousName() {
		return StringUtil.IfNullEmpty(const_data.posthumous_name);
	}
	
	public Person GetFather() {
		if (const_data != null && const_data.father > 0) {
			return accessor.persons.get(const_data.father);
		}
		return null;
	}
	
	public City GetHometown() {
		if (const_data == null) return null;
		if (const_data.birth_city >= 0) {
			return accessor.cities.get(const_data.birth_city);
		}
		if (const_data.city >= 0) {
			return accessor.cities.get(const_data.city);
		}
		return null;
	}
	
	public State GetOwner() {
		return data.owner_state <= 0 ? null : accessor.GetState(data.owner_state);
	}
	
	public State GetOriginalState() {
		State state = accessor.GetState(data.original_state);
		if (state != null) return state;
		if (const_data.city < 0) return null;
		return accessor.cities.get(const_data.city).GetOwner();
	}
	
	public RoleType GetRoleType() {
		return data.role_type;
	}
	
	public AssignmentType GetAssignmentType() {
		return data.assignment_role_type;
	}
	
	public City GetAssignedCity() {
		if (GetAssignmentType() != AssignmentType.GOVERNOR) return null;
		return accessor.cities.get(data.assignment);
	}
	
	public Army GetAssignedArmy() {
		if (GetAssignmentType() != AssignmentType.GENERAL) return null;
		return GetOwner().GetMilitary().GetArmy(data.assignment);
	}
	
	protected void SetRole(RoleType type) {
		data.role_type = type;
	}
	
	protected void AssignToCity(City city) {
		data.assignment_role_type = AssignmentType.GOVERNOR;
		data.assignment = city.id;
	}
	
	protected void AssignToArmy(Army army) {
		data.assignment_role_type = AssignmentType.GENERAL;
		data.assignment = army.id;
	}
	
	public void ResetRole() {
		State state = GetOwner();
		if (state == null) return;
		state.SetOfficer(GetRoleType(), null);
		SetRole(null);
	}
	
	public void ResetAssignment() {
		if (data.assignment_role_type == AssignmentType.GOVERNOR) {
			GetAssignedCity().SetGovernor(null);
		} else if (data.assignment_role_type == AssignmentType.GENERAL) {
			GetAssignedArmy().SetGeneral(null);
		}
		data.assignment_role_type = AssignmentType.NONE;
		data.assignment = -1;
	}
	
	public Date GetDeathTime() {
		return data.die_time;
	}
	
	public void SetOwner(State state) {
		data.owner_state = state.id;
		if (!IsFake()) accessor.sorted_real_persons.Remove(this);
	}
	
	public void ResetOwner() {
		data.owner_state = 0;
		if (!IsFake()) accessor.sorted_real_persons.Insert(this);;
	}
	
	public void SetAbility(AbilityType type, int ability) {
		if (data.ability == null) {
			data.ability = new int[Ability.kMaxTypes];
		}
		data.ability[type.ordinal()] = ability;
	}
	
	public void SetFamilyName(String family_name) {
		data.family_name = family_name;
	}
	
	public void SetGivenName(String given_name) {
		data.given_name = given_name;
	}
	
	public void OnDead() {
		if (!IsFake()) accessor.sorted_real_persons.Remove(this);
	}
	
	protected void SetDeathTime(Date date) {
		data.die_time = date;
	}
	
	public static class AgeComparator implements Comparator<Person>, KeyOrderedVector.ElementComparator<Person, Date> {
		@Override
		public int compare(Person o1, Person o2) {
			Date avail1 = o1.data.available_time;
			Date avail2 = o2.data.available_time;
			int diff = avail1.compareTo(avail2);
			if (diff < 0) return -1;
			if (diff > 0) return 1;
			return o1.GetDeathTime().compareTo(o2.GetDeathTime());
		}

		@Override
		public int CompareKey(Person person, Date date) {
			return person.data.available_time.compareTo(date);
		}

		@Override
		public int CompareElement(Person o1, Person o2) {
			return compare(o1, o2);
		}
	}
	
	public static class DeathTimeComparator implements KeyOrderedVector.ElementComparator<Person, Date> {
		@Override
		public int CompareKey(Person person, Date date) {
			return person.GetDeathTime().compareTo(date);
		}
		
		@Override
		public int CompareElement(Person o1, Person o2) {
			return CompareKey(o1, o2.GetDeathTime());
		}
	}

	protected PersonData data;
	protected ConstPersonData const_data;
}
