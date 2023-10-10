package ac.engine.data;

import java.util.Collection;
import java.util.HashMap;
import ac.data.CityData;
import ac.data.base.Position;
import ac.data.constant.ConstCityData;

public class City extends IdKeyedData {
	protected City(DataAccessor accessor, int id, CityData data, ConstCityData const_data) {
		super(accessor, id);
		this.data = data;
		this.const_data = const_data;
		improvements = new CityImprovements(accessor, this, data.improvements, const_data);
		military = new CityMilitary(accessor, this);
		natural_info = new CityNaturalInfo(accessor, const_data, data);
		population = new CityPopulation(accessor, data, this);
		description = new CityDescription(accessor, const_data);
		commerce = new CityCommerce(accessor);
	}
	
	@Override
	public String GetName() {
		return data.name;
	}
	
	public CityData Get() {
		return data;
	}
	
	public Position GetPosition() {
		return const_data.coordinate;
	}
	
	public long GetTotalPopulation() {
		return data.population;
	}
	
	public Person GetGovernor() {
		return governor;
	}
	
	public Position GetCoordinate() {
		return const_data.coordinate;
	}
	
	public CityImprovements GetImprovements() {
		return improvements;
	}
	
	public CityMilitary GetMilitary() {
		return military;
	}
	
	public CityNaturalInfo GetNaturalInfo() {
		return natural_info;
	}
	
	public CityPopulation GetPopulation() {
		return population;
	}
	
	public CityDescription GetDescription() {
		return description;
	}
	
	public CityCommerce GetCommerce() {
		return commerce;
	}
	
	public State GetOwner() {
		return accessor.GetState(data.owner);
	}
	
	public static enum CityType {
		NONE,
		CAPITAL,
		COUNTY,
		JIMI_COUNTY,
	}
	
	public CityType GetType() {
		if (data.is_county) return CityType.COUNTY;
		if (data.is_jimi_county) return CityType.JIMI_COUNTY;
		if (GetOwner() != null && GetOwner().GetCapital() == this) return CityType.CAPITAL;
		return CityType.NONE;
	}
	
	public void SetType(CityType type) {
		data.is_county = (type == CityType.COUNTY);
		data.is_jimi_county = (type == CityType.JIMI_COUNTY);
	}
	
	public String toString() {
		return Get().name;
	}
	
	public void UpdateTransportation() {
		accessor.GetUtils().trans_util.UpdateTransportation(this, transportation_cost);
	}
	
	public int GetTransportation(City city) {
		return transportation_cost.getOrDefault(city, Integer.MAX_VALUE);
	}
	
	public Collection<City> GetNeighbors() {
		return transportation_cost.keySet();
	}
	
	public HashMap<City, Integer> GetNeighborAndTransportation() {
		return transportation_cost;
	}
	
	public int GetTransportationToCapital() {
		return GetOwner().city_paths.get(this);
	}
	
	public void SetOwner(State state) {
		GetImprovements().ResetConstruction();
		GetMilitary().ResetRecruitment();
		if (state == null) {
			data.Reset();
			return;
		}
		data.owner = state.id;
	}
	
	public void SetGovernor(Person person) {
		governor = person;
		if (governor != null) governor.AssignToCity(this);
	}
	
	private CityData data;
	private ConstCityData const_data;
	private CityImprovements improvements;
	private CityMilitary military;
	private CityNaturalInfo natural_info;
	private CityPopulation population;
	private CityDescription description;
	private CityCommerce commerce;
	private HashMap<City, Integer> transportation_cost = new HashMap<City, Integer>();
	private Person governor;
}
