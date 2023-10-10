package ac.ui.swing.dialog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.GenericDialog;
import ac.ui.swing.GenericFrame;
import ac.ui.swing.elements.ScrollListComponent;
import ac.util.StringUtil;

public class CitySelectionDialog extends StateDialog {

	private static final long serialVersionUID = 1L;
	public enum Ranker {
		REVOLT,
		FOREIGNER,
		HAPPINESS,
		POPULATION,
	}
	
	public CitySelectionDialog(GenericDialog parent, Components cmp, DataAccessor data, Supplier<State> state_getter, Predicate<City> filter, Ranker ranker,
			Consumer<City> func) {
		super(parent, cmp, data, state_getter, Texts.choose + Texts.city, 500, 620, false);
		Init(cmp, data, filter, ranker, func);
	}
	
	public CitySelectionDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<State> state_getter, Predicate<City> filter, Ranker ranker,
			Consumer<City> func) {
		super(parent, cmp, data, state_getter, Texts.choose + Texts.city, 500, 620, false);
		Init(cmp, data, filter, ranker, func);
	}
	
	private void Init(Components cmp, DataAccessor data, Predicate<City> filter, Ranker ranker, Consumer<City> func) {
		this.filter = filter;
		this.ranker = ranker;
		this.func = func;

		kColumnNames[2] = GetRankerColumn(ranker);
		city_list = new ScrollListComponent(kColumnWidth, 20);
		city_list.SetColumnHeaders(kColumnNames);
		
		city_list.setBounds(10, 40, 400, 550);
		add(city_list);
		
		InitDone();
		Refresh();
	}

	@Override
	protected void Refresh() {
		ArrayList<City> cities = new ArrayList<City>(state.GetOwnedCities());
		if (filter != null) {
			cities.removeIf(filter.negate());
		}
		city_list.Resize(cities.size());
		cities.sort(city_comparator);
		for (int i = 0; i < cities.size(); ++i) {
			City city = cities.get(i);
			city_list.SetValue(i, 0, city.GetName());
			city_list.SetValue(i, 1, Texts.cityTypeSymbol[city.GetType().ordinal()]);
			city_list.SetValue(i, 2, StringUtil.LongNumber((long)GetRankerValue(city, ranker)));
			city_list.SetCallback(i, () -> {
				func.accept(city);
				CloseDialog();
			});
		}
	}

	@Override
	protected void Confirm() {
	}
	
	private Comparator<City> city_comparator = new Comparator<City>() {
		@Override
		public int compare(City o1, City o2) {
			int sign = 1;
			switch (ranker) {
			case FOREIGNER:
			case HAPPINESS:
				sign = -1;
			default:
			}
			return (int) (GetRankerValue(o2, ranker) - GetRankerValue(o1, ranker)) * sign;
		}	
	};
	
	public static String GetRankerColumn(Ranker ranker) {
		switch (ranker) {
		case REVOLT:
			return Texts.riot + "(%)";
		case FOREIGNER:
			return Texts.domesticPopulation + "(%)";
		case HAPPINESS:
			return Texts.happiness+ "(%)";
		case POPULATION:
			return Texts.population;
		}
		return null;
	}
	
	public static double GetRankerValue(City city, Ranker ranker) {
		switch (ranker) {
		case REVOLT:
			return city.Get().riot;
		case FOREIGNER:
			return city.GetPopulation().GetPopRatio(0) * 100;
		case HAPPINESS:
			return city.Get().happiness;
		case POPULATION:
			return city.Get().population;
		default:
			break;
		}
		return 0L;
	}

	private Consumer<City> func;
	private Predicate<City> filter;
	private Ranker ranker;
	private ScrollListComponent city_list;
	
	private static final int[] kColumnWidth = { 60, 20, 100};
	private String[] kColumnNames = { Texts.city, "", "" };
}
