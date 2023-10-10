package ac.data.constant;

public class Role {
	public enum RoleType {
		NONE,
		KING,
		MILITARY_OFFICER,
		ADMIN_OFFICER,
		DIPLOMAT_OFFICER,
	}
	
	public enum AssignmentType {
		NONE,
		GOVERNOR,
		GENERAL,
	}
	
	public static RoleType[] kMinisters = {  RoleType.MILITARY_OFFICER, RoleType.ADMIN_OFFICER, RoleType.DIPLOMAT_OFFICER  };
}
