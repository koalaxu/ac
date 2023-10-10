package ac.data;

import java.util.Random;

import ac.data.base.Date;
import ac.data.constant.Ability;
import ac.data.constant.ConstPersonData;
import ac.data.constant.Role.AssignmentType;
import ac.data.constant.Role.RoleType;
import ac.util.RandomUtil;

public class PersonData {
	public boolean hired;
	public Date available_time;
	public Date die_time;
	public String family_name;
	public String given_name;
	public int original_state;
	protected PersonData(ConstPersonData const_data) {
		//available_time = new Date(const_data.available, 1, 1);
		available_time = new Date(const_data.available, 1, 1);
		die_time = new Date(const_data.death, 1, 1);
		die_time.IncrementBy(RandomUtil.SampleFromUniformDistribution(0, 1 * 365, new Random()));
		if (const_data.state > 0) {
			original_state = const_data.state;
		}
	}
	public PersonData() {
		ability = new int[Ability.kMaxTypes];
	}
	public int[] ability;
	
	public int owner_state;
	public RoleType role_type;
	public AssignmentType assignment_role_type;
	public int assignment;
}
