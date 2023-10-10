package ac.engine.util;

import ac.data.base.Date;
import ac.data.constant.Texts;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Ideologies.Ideology;
import ac.data.constant.Ideologies.IdeologyType;
import ac.engine.ai.GameInterface;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.Monarch;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.engine.data.City.CityType;
import ac.util.RandomUtil;

public class PersonUtil extends BaseUtil {

	protected PersonUtil(DataAccessor data, Utils utils) {
		super(data, utils);
		name_util = new NameUtil(data, utils);
	}
	
	public Monarch CreateNewMonarch(State state) {
		Date death_date = data.GetDate().CreateDate(
				(int) RandomUtil.SampleFromNormalDistribution(param.age_mean * 365.0, param.age_std * 365.0, data.GetRandom()));
		Monarch monarch = data.CreateNewMonarch(death_date);
		monarch.SetOwner(state);
		monarch.InheritFrom(state.GetKing());
		InitlizePerson(null, monarch);
		return monarch;
	}
	
	public Person CreateNewPerson(State state) {
		Date death_date = data.GetDate().CreateDate(
				(int) RandomUtil.SampleFromNormalDistribution(param.age_mean * 365.0, param.age_std * 365.0, data.GetRandom()));
		Person person = data.CreateNewPerson(death_date);
		person.SetOwner(state);
		InitlizePerson(state, person);
		person.SetFamilyName(name_util.CreateFamilyName());
		return person;
	}
	
	public void RandomizeAbility(State state, Person person) {
		double[] ability_var_prob = param.ability_var_prob;
		if (state != null) {
			if (state.GetPolicy().HasIdeology(Ideology.NOMINATION)) {
				ability_var_prob = param.ability_var_prob_nomination;
			} else if (state.GetPolicy().HasIdeology(Ideology.IMPERIAL_EXAM)) {
				ability_var_prob = param.ability_var_prob_exam;
			}
		}
		for (AbilityType type : AbilityType.values()) {
			int ability = param.base_ability;
			for (int i = 0; i < param.ability_var_num; ++i) {
				ability += RandomUtil.GetRandomIndexFromProbabilities(ability_var_prob, data.GetRandom()) - 1;
			}
			person.SetAbility(type, Math.min(10, Math.max(1, ability)));
		}
	}

	private void InitlizePerson(State state, Person person) {
		RandomizeAbility(state, person);
		String given_name = name_util.CreateGivenName();
		person.SetGivenName(given_name);
	}
	
//	public void RetirePerson(Person person) {
//		State state = person.GetOwner();
//		if (state == null) return;
//		RoleType type = person.GetRoleType();
//		state.SetOfficer(type, null);
//		person.ResetRole();
//	}
	
	public int GetAbility(Person person, AbilityType type) {
		if (person == null) return param.base_ability;
		return person.GetAbility(type);
	}
	
	public void CoronateNewKing(State state, GameInterface ai) {
		Monarch old_king = state.GetKing();
		Monarch new_king = null;
		for (Monarch monarch : state.GetHistoricalMonarchs()) {
			if (!monarch.IsDead() && monarch.GetEarliestAvailableYear() <= data.GetDate().GetYear() &&
					data.GetDate().compareTo(monarch.GetDeathTime()) < 0) {
				monarch.SetOwner(state);
				new_king = monarch;
				break;
			}
		}
		if (new_king == null) {
			new_king = utils.person_util.CreateNewMonarch(state);
		}
		if (old_king != null) {
			data.AddMessage(state.GetName() + Texts.monarch + old_king.GetName() + Texts.dead);
			if (utils.state_util.DecreaseStability(state)) {
				utils.state_util.StateFall(state, ai);
				return;
			}
		}
		state.SetKing(new_king);
		if (old_king != null) data.AddMessage(state.GetName() + Texts.monarch + new_king.GetName() + Texts.coronate);
	}
	
	public boolean CityAllowsGovernor(City city) {
		return city.GetType() == CityType.CAPITAL || city.GetType() == CityType.COUNTY;
	}
	
	public int GetMaxAllowedOfficer(State state) {
		int allowed = param.base_officer_number;
		Ideology ideology = state.GetPolicy().GetIdeology(IdeologyType.CIVIL);
		if (ideology != null) {
			allowed += param.ideology_officer_number_boost;
			if (ideology == Ideology.RETAINER) {
				allowed += param.retainer_officer_number_boost;
			}
		}
		return allowed;
	}
	
	public Person GetAvailablePerson(State state) {
		for (Person person :  data.GetAvailablePeople()) {
			if (person.GetOriginalState() == state) {
				return person;
			}
		}
		return null;
	}
	
	public boolean CanHirePeople(State state) {
		if (state.GetPersons().size() < GetMaxAllowedOfficer(state)) return true;
		if (GetAvailablePerson(state) != null) return true;
		return false;
	}
	
	private NameUtil name_util;
}
