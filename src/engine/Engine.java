package ac.engine;

import ac.data.constant.Ability.AbilityType;
import ac.engine.ai.GameInterface;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.Monarch;
import ac.engine.data.State;

public class Engine {
	public Engine(DataAccessor data, GameInterface ai) {
		this.data = data;
		this.ai = ai;
		
		executor = new ActionExecutor(data);
		
		army_processor = new ArmyProcessor(data, ai);
		city_development = new CityDevelopment(data, ai);
		state_development = new StateDevelopment(data, ai);
		market_handler = new MarketHandler(data);
		diplomacy_handler = new DiplomacyHandler(data);
		
		ai.Init(data, executor);
	}
	
	public void Init() {
		for (Monarch monarch : data.GetAllMornach()) {
			if (monarch.GetAbility(AbilityType.ADMIN) <= 0) data.GetUtils().person_util.RandomizeAbility(null, monarch);
		}
		for (State state : data.GetAllPlayableStates()) {
			Monarch king = state.GetKing();
			if (king == null || king.IsDead()) {
				state_development.CoronateNewKing(state);
			}
			state_development.CheckArmyNumber(state);
		}
		data.DebugPrint();
		ai.OnStart();
	}
	
	public void Proceed() {
		data.GetDate().Increment();
		HandleNewDay();
		ai.Process();
		if (data.GetDate().GetDay() == 1) {
			HandleNewMonth();
			if (data.GetDate().GetMonth() % 3 == 1) {
				HandleNewSeason();
				if (data.GetDate().GetMonth() == 1) {
					HandleNewYear();
				}
			}
		}
		// System.err.println("end to proceed");
		// data.Save();
		FinalizeNewDay();
	}
	
	public ActionExecutor GetActionExecutor() {
		return executor;
	}
	
	private void HandleNewDay() {
		state_development.CheckPersonDeath();
		city_development.HandleConstructionDone();
		city_development.HandleRiot();
		army_processor.HandleBattles();
		army_processor.HandleMovement();
		army_processor.HandleLogistics();
		diplomacy_handler.CheckTreatyProposalExpiration();
	}
	
	private void FinalizeNewDay() {
		//city_development.HandleLaborDone();
		city_development.HandleLabor();
	}
	
	private void HandleNewMonth() {
		for (City city : data.GetAllPlayableCities()) {
			city_development.HandlePopulation(city);
		}
		
		if (data.GetDate().GetMonth() == 7) {
			city_development.HandleClimate();
		} else if (data.GetDate().GetMonth() == 10) {
			city_development.HandleHarvest();
		} else {
			city_development.HandleBarbarian();
		}
		
		for (State state : data.GetAllPlayableStates()) {
			state_development.HandleMonthlyProduce(state);
		}
		market_handler.Trade();
		army_processor.HandleReinforcement();
		
		//city_development.HandleLaborForMonth();
	}
	
	private void HandleNewSeason() {
		for (State state :data.GetAllPlayableStates()) {
			state_development.CollectGovernmentPoints(state);
			state_development.CheckArmyNumber(state);
		}
	}
	
	private void HandleNewYear() {
		city_development.ResetLaborForYear();
	}
	
	private DataAccessor data;
	
	private ArmyProcessor army_processor;
	private CityDevelopment city_development;
	private StateDevelopment state_development;
	private MarketHandler market_handler;
	private DiplomacyHandler diplomacy_handler;
	
	private ActionExecutor executor;
	private GameInterface ai;
}
