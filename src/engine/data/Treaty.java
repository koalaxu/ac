package ac.engine.data;

import ac.data.TreatyData;
import ac.data.TreatyData.Relationship;
import ac.data.base.Date;
import ac.data.constant.Texts;

public class Treaty extends IdKeyedData {

	protected Treaty(DataAccessor accessor, TreatyData data) {
		super(accessor, data.uid);
		this.data = data;
	}
	
	@Override
	public String GetName() {
		return Texts.treaty + id;
	}
	
	public State GetProposer() {
		return accessor.GetState(data.proposer_state);
	}
	
	public State GetTargetState() {
		return accessor.GetState(data.target_state);
	}
	
	public Relationship GetProposedRelation() {
		return data.proposed_relation;
	}
	
	public Date GetExpireDate() {
		return data.expire_date;
	}
	
	public boolean UnstableIfReject() {
		return data.rejection_leads_to_unstable;
	}

	protected TreatyData data;
}
