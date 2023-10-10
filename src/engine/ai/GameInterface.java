package ac.engine.ai;

import java.util.HashMap;

import ac.data.constant.Policies.Policy;
import ac.engine.ActionExecutor;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.IdKeyedData;
import ac.engine.data.Monarch;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.engine.data.Treaty;

public class GameInterface {
	public interface AICreator {
		public AI Create(DataAccessor data, ActionExecutor action_executor, State state);
	}
	public GameInterface(AICreator creator) {
		this.creator = creator;
	}
	
	public void Init(DataAccessor data, ActionExecutor action_executor) {
		this.data = data;
		for (State state : data.GetAllPlayableStates()) {
			if (state == data.GetPlayer().GetState()) {
				state_ai.put(state, new PlayerAI(data, action_executor, state));
			} else {
				state_ai.put(state, creator.Create(data, action_executor, state));
			}
		}
	}
	
	public void Process() {
		for (State state : data.GetAllPlayableStates()) {
			GetAI(state).Process();
		}
	}
	public void OnStart() {
		for (State state : data.GetAllPlayableStates()) {
			GetAI(state).OnStart();
		}
	}
	public void OnTechnologyComplete(State state) {
		GetAI(state).OnTechnologyComplete();
	}
	public void OnPolicyInvalided(State state, Policy policy, IdKeyedData object, IdKeyedData object2) {
		GetAI(state).OnPolicyInvalided(policy, object, object2);
	}
	public void OnPolicyComplete(State state, Policy policy, IdKeyedData object, IdKeyedData object2) {
		GetAI(state).OnPolicyComplete(policy, object, object2);
	}
	public void OnCityRiotIncreased(State state, City city, int threshold) {
		GetAI(state).OnCityRiotIncreased(city, threshold);
	}
	public void OnCityLost(State state, City city) {
		GetAI(state).OnCityLost(city);
	}
	public void OnCityConquered(State state, City city) {
		GetAI(state).OnCityConquered(city);
	}
	public void OnMonarchDeath(Monarch person) {
		GetAI(person.GetOwner()).OnMonarchDeath(person);
	}
	public void OnPersonDeath(Person person) {
		GetAI(person.GetOwner()).OnPersonDeath(person);
	}
	public void OnDiplomacyProposal(Treaty treaty) {
		GetAI(treaty.GetTargetState()).OnDiplomacyProposal(treaty);
	}
	public void OnArmyRouteBlocked(Army army) {
		GetAI(army.GetState()).OnArmyRouteBlocked(army);
	}
	public void OnInvaded(State state, City city, Army army) {
		GetAI(state).OnInvaded(city, army);
	}
	
	public static interface AI {
		public void Process();
		public void OnStart();
		public void OnTechnologyComplete();
		public void OnPolicyInvalided(Policy policy, IdKeyedData object, IdKeyedData object2);
		public void OnPolicyComplete(Policy policy, IdKeyedData object, IdKeyedData object2);
		public void OnCityRiotIncreased(City city, int threshold);
		public void OnCityLost(City city);
		public void OnCityConquered(City city);
		public void OnMonarchDeath(Monarch person);
		public void OnPersonDeath(Person person);
		public void OnDiplomacyProposal(Treaty treaty);
		public void OnArmyRouteBlocked(Army army);
		public void OnInvaded(City city, Army army);
	}
	
	public static AI DoNothing = new AI() {	
		@Override
		public void Process() {}
		@Override
		public void OnStart() {}
		@Override
		public void OnTechnologyComplete() {}
		@Override
		public void OnPolicyInvalided(Policy policy, IdKeyedData object, IdKeyedData object2) {}
		@Override
		public void OnPolicyComplete(Policy policy, IdKeyedData object, IdKeyedData object2) {}
		@Override
		public void OnCityRiotIncreased(City city, int threshold) {}
		@Override
		public void OnCityLost(City city) {}
		@Override
		public void OnCityConquered(City city) {}
		@Override
		public void OnMonarchDeath(Monarch person) {}
		@Override
		public void OnPersonDeath(Person person) {}
		@Override
		public void OnDiplomacyProposal(Treaty treaty) {}
		@Override
		public void OnArmyRouteBlocked(Army army) {}
		@Override
		public void OnInvaded(City city, Army army) {}
	};
	
	private AI GetAI(State state) {
		return state_ai.getOrDefault(state, DoNothing);
	}
	
	private HashMap<State, AI> state_ai = new HashMap<State, AI>();
	
	private DataAccessor data;
	private AICreator creator;
	//private ActionExecutor executor;
}
