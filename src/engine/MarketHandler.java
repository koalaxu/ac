package ac.engine;

import ac.data.base.Resource;
import ac.data.base.Resource.ResourceType;
import ac.data.constant.Parameters;
import ac.engine.data.DataAccessor;
import ac.engine.data.Market;
import ac.engine.data.State;

public class MarketHandler {
	protected MarketHandler(DataAccessor data) {
		this.data = data;
		market = data.GetMarket();
		param = data.GetParam();
	}
	
	public void Trade() {
		TradeForOneResource(ResourceType.FOOD, param.resource_on_market_base.food);
		TradeForOneResource(ResourceType.HORSE, param.resource_on_market_base.horse);
		TradeForOneResource(ResourceType.IRON, param.resource_on_market_base.iron);
		market.ResetResourceOnMarket();
	}
	
	private void TradeForOneResource(ResourceType type, long market_base) {
		long on_market = market.GetResourceOnMarket(type);
		double old_price = market.GetPrice(type);
		double new_price = old_price;
		if (on_market > 0) {
			long total = on_market + market_base;
			
			double total_budget = market_base * old_price;
			for (State state : data.GetAllPlayableStates()) {
				total_budget += state.GetEconomic().GetResourceImportBudget(type);
			}
			double price_ratio = total_budget / (total * old_price);
			price_ratio = Math.min(param.max_market_price_increase, Math.max(1.0 / param.max_market_price_increase, price_ratio));
			new_price = Math.max(param.min_resource_price, old_price * price_ratio);
			for (State state : data.GetAllPlayableStates()) {
				Resource<Long> owned_resources = state.GetResource();
				long budget = state.GetEconomic().GetResourceImportBudget(type);
				double fulfillable = total / total_budget * budget;
				double affordable = budget / new_price;
				long imported = Math.round(Math.min(fulfillable, affordable));
				long cost = (long) (Math.min(fulfillable, affordable) * new_price);
				if (type == ResourceType.FOOD) {
					owned_resources.food += imported;
				} else if (type == ResourceType.HORSE) {
					owned_resources.horse += imported;
				} else if (type == ResourceType.IRON) {
					owned_resources.iron += imported;
				}
	//			state.GetResource().Set(type, state.GetResource().Get(type) + imported);
				owned_resources.gold -= cost;
			}
		}
		market.Record(type, new_price, on_market);
	}
	
	private Market market;
	private Parameters param;
	private DataAccessor data;
}
