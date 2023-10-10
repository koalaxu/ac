package ac.engine.util;

import java.util.ArrayList;

import ac.data.constant.ConstPersonData;
import ac.engine.data.DataAccessor;
import ac.util.RandomUtil;

public class NameUtil extends BaseUtil {

	protected NameUtil(DataAccessor data, Utils utils) {
		super(data, utils);
		
		for (ConstPersonData person : data.GetConstData().persons) {
			if (person.surname != null && !person.surname.isEmpty()) {
				family_name_list.add(person.surname);
			} else if (person.family_name != null && !person.family_name.isEmpty()) {
				family_name_list.add(person.family_name);
			} else {
				continue;
			}
			if (person.given_name != null && !person.given_name.isEmpty()) {
				given_name_list.add(person.given_name);
			}
		}
		for (ConstPersonData person : data.GetConstData().monarchs) {
			if (person.given_name != null && !person.given_name.isEmpty()) {
				given_name_list.add(person.given_name);
			}
		}
	}
	
	public String CreateGivenName() {
		return RandomUtil.Sample(given_name_list, data.GetRandom());
	}
	
	public String CreateFamilyName() {
		return RandomUtil.Sample(family_name_list, data.GetRandom());
	}

	private ArrayList<String> given_name_list = new ArrayList<String>();
	private ArrayList<String> family_name_list = new ArrayList<String>();
}
