package ac.engine.data;

import java.util.ArrayList;
import java.util.Map.Entry;

import ac.data.base.Pair;
import ac.data.constant.ConstCityData;
import ac.util.StringUtil;

public class CityDescription extends Data {
	protected CityDescription(DataAccessor accessor, ConstCityData data) {
		super(accessor);
		this.data = data;
	}
	
	public String GetHanName() {
		return StringUtil.IfNull(data.han_name, data.name);
	}
	
	public String GetHanCountyName() {
		return data.han_county;
	}
	
	public String GetTangName() {
		return StringUtil.IfNull(data.tang_name, GetHanName());
	}
	
	public String GetTangCountyName() {
		return data.tang_county;
	}
	
	public String GetCurrentName() {
		return data.current_name;
	}
	
	public ArrayList<Pair<City, Double>> GetNeighbors() {
		if (neighbors == null) {
			neighbors = new ArrayList<Pair<City, Double>>();
			for (Entry<Integer, Double> kv_pair : data.neighbor_cities.entrySet()) {
				neighbors.add(new Pair<City, Double>(accessor.cities.get(kv_pair.getKey()), kv_pair.getValue()));
			}
		}
		return neighbors;
	}

	private ConstCityData data;
	private ArrayList<Pair<City, Double>> neighbors;
}
