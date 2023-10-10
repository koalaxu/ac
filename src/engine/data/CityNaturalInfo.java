package ac.engine.data;

import ac.data.CityData;
import ac.data.CityData.StatefulData;
import ac.data.constant.ConstCityData;

public class CityNaturalInfo extends Data {

	protected CityNaturalInfo(DataAccessor accessor, ConstCityData const_data, CityData data) {
		super(accessor);
		this.data = data;
		this.const_data = const_data;
	}
	
	public int GetRainLevel() {
		return const_data.rain;
	}
	
	public int GetTemperatureLevel() {
		return const_data.temperature;
	}
	
	public int GetRainLevelOrDefault() {
		return data.stateful_data != null ? Math.max(0, Math.min(5, data.stateful_data.rain_level + const_data.rain)) : const_data.rain;
	}
	
	public int GetTemperatureLevelOrDefault() {
		return data.stateful_data != null ? Math.max(1, Math.min(5, data.stateful_data.temperature_level + const_data.temperature)) : const_data.temperature;
	}
	
	public int GetFloodRisk() {
		return const_data.flood;
	}
	
	public int GetLocustRisk() {
		return const_data.locust;
	}
	
	public int GetBarbarianRisk() {
		return const_data.barbarian;
	}
	
	public boolean HasFinalState() {
		return data.stateful_data != null;
	}
	
	public void ResetState() {
		data.stateful_data = null;
	}
	
	public void SetRainAdjustment(int adjustment) {
		GetStatefulData().rain_level += adjustment;
	}
	
	public void SetTemperatureAdjustment(int adjustment) {
		GetStatefulData().temperature_level += adjustment;
	}
	
	public void SetFlood(double severity) {
		GetStatefulData().flood_severity = severity;
	}
	
	public void SetLocust(double severity) {
		GetStatefulData().locust_severity = severity;
	}
	
	public int GetRainAdjustment() {
		return data.stateful_data != null ? data.stateful_data.rain_level : 0;
	}
	
	public int GetTemperatureAdjustment() {
		return data.stateful_data != null ? data.stateful_data.temperature_level : 0;
	}
	
	public double GetFloodSeverity() {
		return data.stateful_data != null ? data.stateful_data.flood_severity : 0;
	}
	
	public double GetLocustSeverity() {
		return data.stateful_data != null ? data.stateful_data.locust_severity : 0;
	}
	
	private StatefulData GetStatefulData() {
		if (data.stateful_data == null) data.stateful_data = data.new StatefulData();
		return data.stateful_data;
	}

	private ConstCityData const_data;
	private CityData data;
}
