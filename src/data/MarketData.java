package ac.data;

import java.util.LinkedList;
import java.util.TreeMap;

import ac.data.base.Resource.ResourceType;

public class MarketData {
	public MarketData() {
		for (int i = 0; i < kMaxPriceHistory - 1; ++i) {
			price_history.get(ResourceType.FOOD).add(0.0);
			price_history.get(ResourceType.HORSE).add(0.0);
			price_history.get(ResourceType.IRON).add(0.0);
		}
		price_history.get(ResourceType.FOOD).add(1.0);
		price_history.get(ResourceType.HORSE).add(1.0);
		price_history.get(ResourceType.IRON).add(1.0);
		for (int i = 0; i < kMaxPriceHistory; ++i) {
			quantity_history.get(ResourceType.FOOD).add(0L);
			quantity_history.get(ResourceType.HORSE).add(0L);
			quantity_history.get(ResourceType.IRON).add(0L);
		}
	}
	public TreeMap<ResourceType, LinkedList<Double>> price_history = new TreeMap<ResourceType, LinkedList<Double>>() {
		private static final long serialVersionUID = 1L;{
		put(ResourceType.FOOD, new LinkedList<Double>());
		put(ResourceType.HORSE, new LinkedList<Double>());
		put(ResourceType.IRON, new LinkedList<Double>());
	}};
	
	public TreeMap<ResourceType, LinkedList<Long>> quantity_history = new TreeMap<ResourceType, LinkedList<Long>>() {
		private static final long serialVersionUID = 1L;{
		put(ResourceType.FOOD, new LinkedList<Long>());
		put(ResourceType.HORSE, new LinkedList<Long>());
		put(ResourceType.IRON, new LinkedList<Long>());
	}};
	
	public static int kMaxPriceHistory = 84;
}
