package ac.engine.util;

import java.util.ArrayList;
import java.util.Collection;

import ac.data.TreatyData.Relationship;
import ac.data.constant.ConstStateData;
import ac.data.constant.Texts;
import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Policies.Policy;
import ac.data.constant.Role.RoleType;
import ac.engine.data.Army;
import ac.engine.data.DataAccessor;
import ac.engine.data.IdKeyedData;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.engine.data.StateDiplomacy;
import ac.engine.data.Treaty;
import ac.util.RandomUtil;

public class DiplomacyUtil extends BaseUtil {

	protected DiplomacyUtil(DataAccessor data, Utils utils) {
		super(data, utils);
	}

	public boolean AtWar(State a, State b) {
		//return a.GetDiplomacy().AtWar(b) || b.GetDiplomacy().AtWar(a);
		return DeclareWar(a, b) || DeclareWar(b, a);
	}
	public boolean DeclareWar(State a, State b) {
		if (a == b) return false;
		for (Army army : a.GetMilitary().GetArmies()) {
			if (army.GetTarget() != null && army.GetTarget().GetState() == b) return true;
		}
		for (Army army : b.GetMilitary().GetArmies()) {
			if (utils.army_util.GetPositionOwner(army) == a) return true;
		}
		return false;
	}
	public boolean AtEmbargo(State a, State b) {
		if (a == b) return false;
		return AtWar(a, b) || (!AreAlly(a, b) && (a.GetDiplomacy().GetAttitude(b) <= param.max_attitude_for_embargo ||
				b.GetDiplomacy().GetAttitude(a) <= param.max_attitude_for_embargo));
	}
	public boolean AreAlly(State a, State b) {
		return a.GetDiplomacy().IsAlly(b) || b.GetDiplomacy().IsAlly(a);
	}
	public boolean AreAlliance(State a, State b) {
		return a.GetDiplomacy().IsAlliance(b) || b.GetDiplomacy().IsAlliance(a);
	}
	public boolean BorderOpened(State state, State open_to) {
		if (state == null || !state.Playable()) return true;
		return state.GetDiplomacy().GetSuzerainty() == open_to || state.GetDiplomacy().BorderOpened(open_to);
	}
	
	public Collection<State> GetAllAlliances(State state) {
		ArrayList<State> alliances = new ArrayList<State>();
		for (State other : data.GetAllPlayableStates()) {
			if (other == state) continue;
			if (AreAlliance(other, state)) alliances.add(other);
		}
		return alliances;
	}

	
	public boolean UnstableIfRejected(State state) {
		Person person = state.GetOfficer(RoleType.DIPLOMAT_OFFICER);
		if (person == null || person.GetAbility(AbilityType.DIPLOMACY) < state.GetKing().GetAbility(AbilityType.DIPLOMACY)) {
			person = state.GetKing();
		}
		boolean ret = RandomUtil.WhetherToHappend(person.GetAbility(AbilityType.DIPLOMACY) * 0.1 - 0.1, data.GetRandom());
		ret |= RandomUtil.WhetherToHappend(state.Get().prestige * param.prestige_boost_multiplier_for_treaty, data.GetRandom());
		ret &= !RandomUtil.WhetherToHappend(-state.Get().prestige * param.prestige_penalty_multiplier_for_treaty, data.GetRandom());
		return ret;
	}
	
	public boolean IsTreatyEligible(Treaty treaty) {
		return IsTreatyEligible(treaty.GetProposer(), treaty.GetTargetState(), treaty.GetProposedRelation());
	}
	
	public boolean IsTreatyEligible(State state, State target_state, Relationship relation) {
		if (utils.diplomacy_util.AtWar(state, target_state)) return false;
		if (AreAlliance(state, target_state)) {
			return false;
		}
		if (relation == Relationship.ALLIANCE) {
			if (!AreAlly(state, target_state) || !(BorderOpened(state, target_state) || BorderOpened(target_state, state))) {
				return false;
			}
		} else if (relation == Relationship.VASSAL) {
			if (!AreAlly(state, target_state) || !BorderOpened(state, target_state) || state.GetDiplomacy().GetSuzerainty() != null) {
				return false;
			}
			if (HasCircularVassalage(target_state, state)) return false;
		} else if (relation == Relationship.SUZERAINTY) {
			if (!AreAlly(state, target_state) || !BorderOpened(target_state, state) || target_state.GetDiplomacy().GetSuzerainty() != null) {
				return false;
			}
			if (HasCircularVassalage(state, target_state)) return false;
		} else if (relation == Relationship.OPEN_BORDER) {
			if (!AreAlly(state, target_state) || BorderOpened(target_state, state)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean HasCircularVassalage(State a, State b) {
		State c = a;
		while (c != null) {
			c = c.GetDiplomacy().GetSuzerainty();
			if (c == b) return true;
		}
		return false;
	}
	
	public void CreateVassalage(State suzerainty, State vassal) {
		vassal.GetDiplomacy().SetSuzerainty(suzerainty);
		suzerainty.GetDiplomacy().GetVassals().add(vassal);
		data.AddMessage(vassal.GetName() + Texts.become + suzerainty.GetName() + Texts.of + Texts.vassal);
	}
	
	public void CancelVassalage(State suzerainty, State vassal) {
		vassal.GetDiplomacy().SetSuzerainty(null);
		suzerainty.GetDiplomacy().GetVassals().remove(vassal);
		data.AddMessage(vassal.GetName() + Texts.and + suzerainty.GetName() + Texts.cancel + Texts.vassal + Texts.relationship);
	}
	
	public void CreateAlliance(State state, State target) {
		state.GetDiplomacy().SetAlliance(target, true);
		target.GetDiplomacy().SetAlliance(state, true);
		data.AddMessage(state.GetName() + Texts.and + target.GetName() + Texts.become + Texts.alliance);
	}
	
	public void CancelAlliance(State state, State target) {
		state.GetDiplomacy().SetAlliance(target, false);
		target.GetDiplomacy().SetAlliance(state, false);
		data.AddMessage(state.GetName() + Texts.and + target.GetName() + Texts.cancel + Texts.alliance + Texts.relationship);
	}
	
	public void CreateAlly(State state, State target) {
		state.GetDiplomacy().SetAlly(target, true);
		target.GetDiplomacy().SetAlly(state, true);
		data.AddMessage(state.GetName() + Texts.and + target.GetName() + Texts.become + Texts.ally);
	}
	
	public void CancelAlly(State state, State target) {
		state.GetDiplomacy().SetAlly(target, false);
		target.GetDiplomacy().SetAlly(state, false);
		data.AddMessage(state.GetName() + Texts.and + target.GetName() + Texts.cancel + Texts.ally + Texts.relationship);
	}
	
	public void CheckAndResetRealationship(State state, State target) {
		StateDiplomacy state_diplomacy = state.GetDiplomacy();
		StateDiplomacy target_diplomacy = target.GetDiplomacy();
		int max_attitude = Math.max(state_diplomacy.GetAttitude(target), target_diplomacy.GetAttitude(state));
		max_attitude += param.delta_attitude_for_maintanence;
		if (max_attitude >= param.min_attitude_for_alliance) return;
		if (utils.diplomacy_util.AreAlliance(state, target)) {
			CancelAlliance(state, target);
		}
		if (state_diplomacy.GetSuzerainty() == target) {
			CancelVassalage(target, state);
		}
		if (target_diplomacy.GetSuzerainty() == state) {
			CancelVassalage(state, target);
		}
		if (max_attitude >= param.min_attitude_for_open_border) return;
		state_diplomacy.SetOpenBorder(target, false);
		target_diplomacy.SetOpenBorder(state, false);
		if (max_attitude >= param.min_attitude_for_ally) return;
		if (utils.diplomacy_util.AreAlly(state, target)) {
			CancelAlly(state, target);
		}
	}
	
	public void IncreasePrestige(State state, int inc) {
		state.Get().prestige = Math.min(ConstStateData.kMaxPrestige, state.Get().prestige + inc);
	}
	
	public void HandleTreatyResponse(Treaty treaty, boolean accept) {
		if (accept) {
			State proposer = treaty.GetProposer();
			State target = treaty.GetTargetState();
			switch (treaty.GetProposedRelation()) {
			case ALLIANCE:
				utils.diplomacy_util.CreateAlliance(proposer, target);
				break;
			case ALLY:
				utils.diplomacy_util.CreateAlly(proposer, target);
				break;
			case OPEN_BORDER:
				target.GetDiplomacy().SetOpenBorder(proposer, true);
				break;
			case SUZERAINTY:
				utils.diplomacy_util.CreateVassalage(proposer, target);
				break;
			case VASSAL:
				utils.diplomacy_util.CreateVassalage(target, proposer);
				break;
			default:
				break;
			}
		} else {
			if (treaty.UnstableIfReject()) {
				State target = treaty.GetTargetState();
				if (target.Get().stability == ConstStateData.kMaxStability) {
					utils.state_util.DecreaseStability(target);
				}
				target.Get().prestige -= param.prestige_decrease_for_treaty_rejection;
			}
		}
	}
	
	public boolean ValidateDiplomacyPolicy(Policy policy, State state, IdKeyedData object) {
		if (policy == Policy.PROPOSE_ALLY) {
			if (state.GetDiplomacy().GetAttitude((State)object) < param.min_attitude_for_ally) return false;
		} else if (policy == Policy.PROPOSE_OPEN_BORDER) {
			if (state.GetDiplomacy().GetAttitude((State)object) < param.min_attitude_for_open_border) return false;
		} else if (policy == Policy.PROPOSE_VASSAL || policy == Policy.PROPOSE_ALLIANCE) {
			if (state.GetDiplomacy().GetAttitude((State)object) < param.min_attitude_for_alliance) return false;
		} else if (policy == Policy.PROPOSE_SUZERAINTY) {
			if (state.GetDiplomacy().GetAttitude((State)object) < param.min_attitude_for_alliance ||
					state.Get().prestige < param.min_prestige_for_suzerainy) return false;
		}
		return true;
	}
}
