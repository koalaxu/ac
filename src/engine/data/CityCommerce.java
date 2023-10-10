package ac.engine.data;

import java.util.HashMap;

import ac.data.constant.Improvement.ImprovementType;

public class CityCommerce extends Data {

	protected CityCommerce(DataAccessor accessor) {
		super(accessor);
	}
	
	public void Clear() {
		imports.clear();
		exports.clear();
		transfers.clear();
	}
	
	public HashMap<ImprovementType, City> GetImports() {
		return imports;
	}
	
	public int GetNumExports(ImprovementType type) {
		return exports.getOrDefault(type, 0);
	}
	
	public int GetNumTransfers(ImprovementType type) {
		return transfers.getOrDefault(type, 0);
	}	
	
	public void IncreaseExport(ImprovementType type) {
		exports.put(type, GetNumExports(type) + 1);
	}
	
	public void IncreaseTransfer(ImprovementType type) {
		transfers.put(type, GetNumTransfers(type) + 1);
	}

	private HashMap<ImprovementType, City> imports = new HashMap<ImprovementType, City>();
	private HashMap<ImprovementType, Integer> exports = new HashMap<ImprovementType, Integer>();
	private HashMap<ImprovementType, Integer> transfers = new HashMap<ImprovementType, Integer>();
}
