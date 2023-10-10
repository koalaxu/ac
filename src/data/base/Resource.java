package ac.data.base;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import ac.data.constant.Texts;

public class Resource<T extends Number>  {
	public static enum ResourceType {
		FOOD,
		HAMMER,
		GOLD,
		HORSE,
		IRON
	}
	
	//protected ArrayList<T> values = new ArrayList<T>();
	public T food;
	public T hammer;
	public T gold;
	public T horse;
	public T iron;
	
	public static final int kMaxTypes = ResourceType.values().length;
	
//	public T Get(ResourceType type) {
//		switch (type) {
//		case FOOD:
//			return food;
//		case GOLD:
//			return gold;
//		case HAMMER:
//			return hammer;
//		case HORSE:
//			return horse;
//		case IRON:
//			return iron;
//		}
//		return null;
//	}
	
//	public void Set(ResourceType type, T value) {
//		switch (type) {
//		case FOOD:
//			food = value;
//			return;
//		case GOLD:
//			gold = value;
//			return;
//		case HAMMER:
//			hammer = value;
//			return;
//		case HORSE:
//			horse = value;
//			return;
//		case IRON:
//			iron = value;
//			return;
//		}
//	}
	
	public Resource(Resource<T> that) {
		Assign((x, y) -> y, that);
	}
	
	public Resource(T n) {
		SetAll(n);
	}
	
	public void SetAll(T n) {
		Assign((x, y) -> { return n; }, this);
	}
	
	public boolean CheckAll(Predicate<T> func) {
		return func.test(food) && func.test(hammer) && func.test(gold) && func.test(horse) && func.test(iron);
	}
	
	public <U extends Number> boolean CheckAll(BiPredicate<T, U> func, Resource<U> that) {
		return func.test(food, that.food) && func.test(hammer, that.hammer) && func.test(gold, that.gold) && func.test(horse, that.horse)
				&& func.test(iron, that.iron);
	}
	
	public <U extends Number> double Aggregate(BiFunction<T, U, Double> func, Function<ResourceType, U> getter) {
		double ret = 0L;
		ret += func.apply(food, getter.apply(ResourceType.FOOD));
		ret += func.apply(hammer, getter.apply(ResourceType.HAMMER));
		ret += func.apply(gold, getter.apply(ResourceType.GOLD));
		ret += func.apply(horse, getter.apply(ResourceType.HORSE));
		ret += func.apply(iron, getter.apply(ResourceType.IRON));
		return ret;
	}
	
	public <U extends Number> void Assign(BiFunction<T, U, T> func, Resource<U> that) {
		food = func.apply(food, that.food);
		hammer = func.apply(hammer, that.hammer);
		gold = func.apply(gold, that.gold);
		horse = func.apply(horse, that.horse);
		iron = func.apply(iron, that.iron);
	}
	
	public <U extends Number, V extends Number> void Assign(BiFunction<U, V, T> func, Resource<U> that, Function<ResourceType, V> getter) {
		food = func.apply(that.food, getter.apply(ResourceType.FOOD));
		hammer = func.apply(that.hammer, getter.apply(ResourceType.HAMMER));
		gold = func.apply(that.gold, getter.apply(ResourceType.GOLD));
		horse = func.apply(that.horse, getter.apply(ResourceType.HORSE));
		iron = func.apply(that.iron, getter.apply(ResourceType.IRON));
	}
	
	public <U extends Number, V extends Number> void Assign(BiFunction<U, V, T> func, Resource<U> input1, Resource<V> input2) {
		food = func.apply(input1.food, input2.food);
		hammer = func.apply(input1.hammer, input2.hammer);
		gold = func.apply(input1.gold, input2.gold);
		horse = func.apply(input1.horse, input2.horse);
		iron = func.apply(input1.iron, input2.iron);
	}
	
	public void Assign(Function<T, T> func) {
		food = func.apply(food);
		hammer = func.apply(hammer);
		gold = func.apply(gold);
		horse = func.apply(horse);
		iron = func.apply(iron);
	}
	
	public void Iterate(BiConsumer<T, Integer> func) {
		func.accept(food, 0);
		func.accept(hammer, 1);
		func.accept(gold, 2);
		func.accept(horse, 3);
		func.accept(iron, 4);
	}
	
	public <U extends Number> void Iterate(BiConsumer<T, U> func, Resource<U> input) {
		func.accept(food, input.food);
		func.accept(hammer, input.hammer);
		func.accept(gold, input.gold);
		func.accept(horse, input.horse);
		func.accept(iron, input.iron);
	}
	
//	public static void Itetater(Consumer<ResourceType> func) {
//		func.accept(ResourceType.FOOD);
//		func.accept(ResourceType.HAMMER);
//		func.accept(ResourceType.GOLD);
//		func.accept(ResourceType.HORSE);
//		func.accept(ResourceType.IRON);
//	}
	
	public String toString() {
		String[] ret = { "" };
		Iterate((res, index) -> { ret[0] += (index > 0 ? " | " : "") + Texts.resourcesIcon[index] + " " + res; });
		return ret[0];
	}
	
	public String toSimpleString() {
		String[] ret = { "" };
		Iterate((res, index) -> {
			if (res.doubleValue() > 0) ret[0] += (ret[0].isEmpty() ? "" :" | " ) + Texts.resourcesIcon[index] + " " + res; });
		return ret[0];
	}
	
	// a += b * c;	
	public static void CollectResource(Resource<Long> a, Resource<Double> b, double c) {
		if (c == 0) return;
		a.Assign((x, y) -> { return (long) (x + y * c); }, b); 
	}
	
	// a += b
	public static void CollectResource(Resource<Double> a, Resource<Long> b) {
		a.Assign((x, y) -> { return x + y; }, b);
	}
	
	// a += b * c
	public static void AddResource(Resource<Long> a, Resource<Integer> b, long c) {
		if (c == 0) return;
		a.Assign((x, y) -> { return (long) (x + y * c); }, b);
	}
	
	// a += b * c
	public static void AddResource(Resource<Long> a, Resource<Long> b, double c) {
		if (c == 0) return;
		a.Assign((x, y) -> { return (long) (x + y * c); }, b);
	}
	
	// a += b
	public static void AddResource(Resource<Long> a, Resource<Long> b) {
		a.Assign((x, y) -> { return x + y; }, b);
	}
	
	// return true if a >= b
	public static boolean NotLessThan(Resource<Long> a, Resource<Long> b) {
		return a.CheckAll((x, y) -> x >= y, b);
	}
	
	// a -= b;
	public static void SubtractResource(Resource<Long> a, Resource<Long> b) {
		a.Assign((x, y) -> { return x - y; }, b);
	}
	
	// a -= b * c;
	public static void SubtractResource(Resource<Long> a, Resource<Integer> b, long c) {
		a.Assign((x, y) -> { return x - y * c; }, b);
	}
	
	// a = b * c
	public static void Product(Resource<Long> a, Resource<Long> b, Resource<Double> c) {
		a.Assign((x, y) -> (long) (x * y), b, c);
	}
	
	// a = b * c
	public static void Multiple(Resource<Long> a, Resource<Long> b, Resource<Integer> c) {
		a.Assign((x, y) -> (long) (x * y), b, c);
	}
	
	// a *= (1 - b)
	public static void Reduce(Resource<Long> a, double b) {
		a.Assign(x -> (long) (x * (1 - b)));
	}
	
	// a = max(c - b, 0)
	public static Resource<Long> Debt(Resource<Long> balance, Resource<Long> unit_cost) {
		Resource<Long> debt = new Resource<Long>(0L);
		debt.Assign((x, y) -> Math.max(y - x, 0), balance, unit_cost);
		return debt;
	}
	
	// c = a / b
	public static long Divide(Resource<Long> a, Resource<Integer> b) {
		long[] c = { Long.MAX_VALUE };
		a.Iterate((x, y) -> { c[0] = Math.min(c[0], y > 0 ? x / y : Long.MAX_VALUE); }, b);
		return c[0];
	}
}
