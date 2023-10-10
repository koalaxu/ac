package ac.engine.ai;

import java.util.function.Predicate;

import ac.engine.data.City;
import ac.engine.data.City.CityType;

public class Filter {
	public static Predicate<City> county_candidate_filter = new Predicate<City>() {
		@Override
		public boolean test(City t) {
			return t.GetType() != CityType.NONE;
		}
	};
}
