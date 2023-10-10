package ac.engine.data;

import java.util.LinkedList;

import ac.data.MarketData;
import ac.data.base.Resource;
import ac.data.base.Resource.ResourceType;

public class Market extends Data {
	protected Market(DataAccessor accessor, MarketData data) {
		super(accessor);
		this.data = data;
	}
	
	public double GetPrice(ResourceType type) {
		if (!IsTradableResource(type)) return 0.0;
		return GetPriceHistory(type).getLast();
	}
	
	public double GetHistoricalPrice(ResourceType type, int i) {
		if (!IsTradableResource(type)) return 0.0;
		return GetPriceHistory(type).get(i);
	}
	
	public long GetHistoricalAmount(ResourceType type, int i) {
		if (!IsTradableResource(type)) return 0L;
		return data.quantity_history.get(type).get(i);
	}
	
	public long GetLatestAmount(ResourceType type) {
		if (!IsTradableResource(type)) return 0L;
		return data.quantity_history.get(type).getLast();
	}
	
	public void AddFoodToMarket(double food) {
		resource_on_market.food += food;
	}
	
	public long AddResourceToMarket(Resource<Long> produce, double ratio, boolean reduce) {
//		long price = 0L;
//		price += AddResourceToMarket(produce, ratio, ResourceType.FOOD, reduce);
//		price += AddResourceToMarket(produce, ratio, ResourceType.HORSE, reduce);
//		price += AddResourceToMarket(produce, ratio, ResourceType.IRON, reduce);
//		System.err.println(resource_on_market);
//		return price;
		Resource<Long> export = new Resource<Long>(0L);
		export.Assign((p, r) -> (long) Math.ceil(p * r), produce, type -> Double.valueOf(IsTradableResource(type) ? ratio : 0.0));
		Resource.CollectResource(resource_on_market, export);
		if (reduce) {
			Resource.SubtractResource(produce, export);
		}
		return (long)Math.ceil(export.Aggregate((num, price) -> num * price, this::GetPrice));
	}
	
	public long GetResourceOnMarket(ResourceType type) {
		if (type == ResourceType.FOOD) {
			return (long) Math.ceil(resource_on_market.food);
		} else if (type == ResourceType.HORSE) {
			return (long) Math.ceil(resource_on_market.horse);
		} else if (type == ResourceType.IRON) {
			return (long) Math.ceil(resource_on_market.iron);
		}
		return 0L;
	}
	
	public void Record(ResourceType type, double new_price, long amount_on_market) {
		LinkedList<Double> prices = data.price_history.get(type);
		prices.add(new_price);
		prices.pollFirst();
		LinkedList<Long> amounts = data.quantity_history.get(type);
		amounts.add(amount_on_market);
		amounts.pollFirst();
	}
	
	public void ResetResourceOnMarket() {
		resource_on_market.SetAll(0.0);	
	}
	
	public static boolean IsTradableResource(ResourceType type) {
		return type != ResourceType.HAMMER && type != ResourceType.GOLD;
	}
//	
//	private long AddResourceToMarket(Resource<Long> produce, double ratio, ResourceType type, boolean reduce) {
//		long export = (long) Math.ceil(produce.Get(type) * ratio);
//		resource_on_market.Set(type, resource_on_market.Get(type) + export);
//		if (reduce) {
//			produce.Set(type, produce.Get(type) - export);
//		}
//		return (long) Math.ceil(export * GetPrice(type));
//	}
	
	private LinkedList<Double> GetPriceHistory(ResourceType type) {
		return data.price_history.get(type);
	}
	
	private MarketData data;
	public Resource<Double> resource_on_market = new Resource<Double>(0.0);
//	private Resource<Double> resource_on_market = new Resource<Double>(0.0);
}
