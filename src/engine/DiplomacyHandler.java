package ac.engine;

import java.util.ArrayList;

import ac.engine.data.DataAccessor;
import ac.engine.data.Treaty;

public class DiplomacyHandler {
	protected DiplomacyHandler(DataAccessor data) {
		this.data = data;
	}
	
	public void CheckTreatyProposalExpiration() {
		ArrayList<Treaty> treaties_to_close = new ArrayList<Treaty>();
		for (Treaty treaty : data.GetAllTreaties()) {
			if (data.GetDate().compareTo(treaty.GetExpireDate()) >= 0) {
				treaties_to_close.add(treaty);
				data.GetUtils().diplomacy_util.HandleTreatyResponse(treaty, false);
			}
		}
		for (Treaty treaty : treaties_to_close) {
			data.CloseTreaty(treaty);
		}
	}
	
	private DataAccessor data;
}
