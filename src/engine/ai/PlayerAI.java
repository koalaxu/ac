package ac.engine.ai;

import ac.data.constant.Ability.AbilityType;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.PlayerData.InformationData.InformationType;
import ac.data.TreatyData.Relationship;
import ac.data.constant.Policies;
import ac.data.constant.Policies.Policy;
import ac.engine.Action;
import ac.engine.ActionExecutor;
import ac.engine.Action.ActionType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.CityMilitary.RecruitmentInfo;
import ac.engine.data.DataAccessor;
import ac.engine.data.IdKeyedData;
import ac.engine.data.Monarch;
import ac.engine.data.Person;
import ac.engine.data.Player;
import ac.engine.data.Player.Information;
import ac.engine.data.State;
import ac.engine.data.Treaty;
import ac.engine.util.Utils;

public class PlayerAI implements GameInterface.AI {
	
	public PlayerAI(DataAccessor data, ActionExecutor action_executor, State state) {
		this.data = data;
		player = data.GetPlayer();
		utils = data.GetUtils();
		player_state = state;
		this.action_executor = action_executor;
	}

	@Override
	public void Process() {
		CheckConstruction();
		CheckRecruitment();
		for (City city : player_state.GetOwnedCities()) {
			if (data.GetPlayer().HasAutoAppease(city)) {
				CheckCityGrowth(city);
			}
		}
		CheckExpiredTreaty();
	}

	@Override
	public void OnStart() {
	}

	@Override
	public void OnTechnologyComplete() {
		player.AddInformation(InformationType.TECH_COMPLETED,
				player_state.GetTechnology().GetCurrentTechnology(player_state.GetTechnology().GetResearchingTechnologyType()));
	}
	
	@Override
	public void OnPolicyInvalided(Policy policy, IdKeyedData object, IdKeyedData object2) {
		player.AddInformation(InformationType.POLICY_INVALIDED, policy);
	}
	
	@Override
	public void OnPolicyComplete(Policy policy, IdKeyedData object, IdKeyedData object2) {
		boolean auto_renewed = MaybeAutoRenewPolicy(policy, object, object2);
		AbilityType type = Policies.GetType(policy);
		data.GetPlayer().SetPolicyAutoRenew(type, auto_renewed);
		player.AddInformation(InformationType.POLICY_COMPLETED, policy);
	}
	
	private boolean MaybeAutoRenewPolicy(Policy policy, IdKeyedData object, IdKeyedData object2) {
		AbilityType type = Policies.GetType(policy);
		if (player.DoesPolicyAutoRenew(type)) {
			State state = data.GetPlayer().GetState();
			if (!utils.diplomacy_util.ValidateDiplomacyPolicy(policy, state, object)) return false;
			switch (policy) {
			case PROPOSE_ALLIANCE:
				if (!utils.diplomacy_util.IsTreatyEligible(state, (State)object, Relationship.ALLIANCE)) return false;
			case PROPOSE_ALLY:
				if (!utils.diplomacy_util.IsTreatyEligible(state, (State)object, Relationship.ALLY)) return false;
			case PROPOSE_OPEN_BORDER:
				if (!utils.diplomacy_util.IsTreatyEligible(state, (State)object, Relationship.OPEN_BORDER)) return false;
			case PROPOSE_VASSAL:
				if (!utils.diplomacy_util.IsTreatyEligible(state, (State)object, Relationship.VASSAL)) return false;
			case PROPOSE_SUZERAINTY:
				if (!utils.diplomacy_util.IsTreatyEligible(state, (State)object, Relationship.SUZERAINTY)) return false; 
			default:
			}
			Action action = new Action(ActionType.ADOPT_POLICY);
			action.object = state;
			action.quantity = policy.ordinal();
			action.object2 = object;
			action.object3 = object2;
			action_executor.Execute(action);
			return true;
		}
		return false;
	}
	
	@Override
	public void OnCityRiotIncreased(City city, int threshold) {
		player.AddInformation(InformationType.CITY_RIOT_INCREASED, city);
	}
	

	@Override
	public void OnCityLost(City city) {
		player.CancelAllConstructionImprovement(city);
		player.CancelAllRecruitment(city);
		player.SetAutoAppease(city, false);
		player.AddInformation(InformationType.CITY_LOST, city);
	}

	@Override
	public void OnCityConquered(City city) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void OnMonarchDeath(Monarch person) {
		player.AddInformation(InformationType.MONARCH_DIED, person);
	}

	@Override
	public void OnPersonDeath(Person person) {
		player.AddInformation(InformationType.PERSON_DIED, person);
	}

	@Override
	public void OnDiplomacyProposal(Treaty treaty) {
		player.AddInformation(InformationType.TREATY_RECEIVED, treaty);
	}

	@Override
	public void OnArmyRouteBlocked(Army army) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnInvaded(City city, Army army) {
		player.AddInformation(InformationType.CITY_INVADED, city);
	}
	
	private void CheckRecruitment() {
		for (City city : player_state.GetOwnedCities()) {
			RecruitmentInfo info = city.GetMilitary().GetRecruitmentInfo();
			if (info != null) return;
			info = data.GetPlayer().GetNextRecruitment(city);
			if (info == null) continue;
//			if (!utils.state_util.IsUnitAvailable(state, unit)) return false;
//			if (utils.state_util.GetTypedSoldiers(state, soldier_type) + utils.state_util.GetTypedSoldiersUnderConstruction(state, soldier_type)
//					+ params.base_recruitment > utils.state_util.GetAllowedTypedSoldiers(state, soldier_type)) return false;
//			if (!utils.state_util.IsRecruitmentAffordable(state, unit) || utils.city_util.GetAvailableSoldierCandidate(city) < params.base_recruitment
//					|| !utils.army_util.IsArmyReinforceable(army)) return false;
			
			if (!utils.state_util.IsUnitAvailable(player_state, info.unit)) continue;
			if (utils.state_util.GetTypedSoldiers(player_state, info.type) + utils.state_util.GetTypedSoldiersUnderConstruction(player_state, info.type)
					+ data.GetParam().base_recruitment > utils.state_util.GetAllowedTypedSoldiers(player_state, info.type)) continue;
			if (!utils.state_util.IsRecruitmentAffordable(player_state, info.unit) || utils.city_util.GetAvailableSoldierCandidate(city) < data.GetParam().base_recruitment
					|| !utils.army_util.IsArmyReinforceable(info.army)) continue;
			Action action = new Action(ActionType.RECRUIT_ARMY);
			action.object = info.army;
			action.object2 = city;
			action.object3 = info.unit;
			action.quantity = info.type.ordinal();
			action_executor.Execute(action);
			data.GetPlayer().PopNextRecruitment(city);
		}
	}
	
	private void CheckConstruction() {
		for (City city : player_state.GetOwnedCities()) {
			if (city.GetImprovements().GetCurrentConstruction() != null) return;
			ImprovementType impr = player.GetNextConstructionImprovement(city);
			if (impr == null) continue;
			if (utils.state_util.IsImprovementAffordable(player_state, city, impr)) {
				Action action = new Action(ActionType.CONSTRUCT_IMPROVEMENT);
				action.object = city;
				action.quantity = impr.ordinal();
				action_executor.Execute(action);
				data.GetPlayer().PopNextConstructionImprovement(city);
			}
		}
	}
	
	private void CheckCityGrowth(City city) {
		long minimum_consumption = (long) (city.Get().population / 12 * data.GetParam().pop_growth_cutoff);
		long delta = minimum_consumption - city.Get().remaining_food;
		if (delta > 0) {
			Action action = new Action(ActionType.APPEASE_SUBJECT_WITH_FOOD);
			action.object = city;
			action.quantity = Math.min(delta, player_state.GetResource().food);
			action_executor.Execute(action);
		}
	}
	
	private void CheckExpiredTreaty() {
		for (int i = 0; i < player.GetInformationList().size();) {
			Information info = player.GetInformationList().get(i);
			if (info.GetType() == InformationType.TREATY_RECEIVED) {
				Treaty treaty = info.GetTreaty();
				if (treaty.GetExpireDate().GetDifference(data.GetDate()) <= 1) {
					player.RemoveInformation(info);
					continue;
				}
			}
			i++;
		}
	}

	private DataAccessor data;
	private Utils utils;
	private State player_state;
	private ActionExecutor action_executor;
	private Player player;
}
