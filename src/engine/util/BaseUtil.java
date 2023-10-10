package ac.engine.util;

import ac.data.constant.Parameters;
import ac.engine.data.DataAccessor;

public class BaseUtil {
	protected BaseUtil(DataAccessor data, Utils utils) {
		this.data = data;
		param = data.GetParam();
		this.utils = utils;
	}
	
	protected DataAccessor data;
	protected Parameters param;
	protected Utils utils;
}
