package ac.data;

import ac.data.base.Date;

public class TreatyData {
	public int uid;
	public Date expire_date;
	public int proposer_state;
	public int target_state;
	public static enum Relationship {
		ALLY,
		OPEN_BORDER,
		SUZERAINTY,
		VASSAL,
		ALLIANCE,
	}
	public Relationship proposed_relation;
	public boolean rejection_leads_to_unstable;
}
