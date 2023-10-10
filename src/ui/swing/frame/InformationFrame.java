package ac.ui.swing.frame;

import java.util.ArrayList;
import ac.data.PlayerData.InformationData.InformationType;
import ac.data.constant.Policies;
import ac.data.constant.Texts;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.DataAccessor;
import ac.engine.data.Player.Information;
import ac.engine.data.Treaty;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;
import ac.ui.swing.elements.ScrollListComponent;

public class InformationFrame extends GenericFrame {

	private static final long serialVersionUID = 1L;

	public InformationFrame(Components cmp, DataAccessor data) {
		super(cmp, data, 640, 400);
		setAlwaysOnTop(true);
		setLayout(null);
		setVisible(false);
		
		setTitle(Texts.information);
		info_list = new ScrollListComponent(kColumnWidth, 20);
		info_list.SetColumnHeaders(kColumnNames);
		
		info_list.setBounds(5, 5, 630, 360);
		add(info_list);
	}

	@Override
	protected void Refresh() {
		ArrayList<Information> information = data.GetPlayer().GetInformationList();
		info_list.Resize(information.size());
		for (int i = 0; i < information.size(); ++i) {
			Information info = information.get(i);
			info_list.SetValue(i, 0, info.GetDate().ShortString());
			info_list.SetValue(i, 1, GetContent(info));
			info_list.SetButton(i, 3, Texts.no, () -> data.GetPlayer().RemoveInformation(info));
			if (info.GetType() == InformationType.TREATY_RECEIVED) {
				info_list.SetButton(i, 2, Texts.yes, () -> {
					Action action = new Action(ActionType.RESPOND_TO_TREATY);
					action.object = info.GetTreaty();
					action.quantity = 1;
					cmp.action_consumer.accept(action);
					data.GetPlayer().RemoveInformation(info);
				});
			} else {
				info_list.RemoveButton(i, 2);
			}
		}
	}
	
	public void Show() {
		Refresh();
		repaint();
		setVisible(true);	
	}
	
	private String GetContent(Information info) {
		String patttern = Texts.infoMessages[info.GetType().ordinal()];
		switch (info.GetType()) {
		case CITY_INVADED:
		case CITY_LOST:
			return String.format(patttern, info.GetCity().GetName());
		case CITY_RIOT_INCREASED:
			return String.format(patttern, info.GetCity().GetName(), info.GetCity().Get().riot / 2000 * 2000);
		case MONARCH_DIED:
			return String.format(patttern, info.GetMonarch().GetName());
		case PERSON_DIED:
			return String.format(patttern, info.GetPerson().GetName());
		case POLICY_INVALIDED:
			return String.format(patttern, Texts.policies[info.GetPolicy().ordinal()], Texts.abilities[Policies.GetType(info.GetPolicy()).ordinal()]);
		case POLICY_COMPLETED:
			return String.format(patttern, Texts.policies[info.GetPolicy().ordinal()], Texts.abilities[Policies.GetType(info.GetPolicy()).ordinal()]);
		case TECH_COMPLETED:
			return String.format(patttern, info.GetTech().name);
		case TREATY_RECEIVED:
			Treaty treaty = info.GetTreaty();
			return String.format(patttern, treaty.GetProposer().GetName(), Texts.infoTreatyContent[treaty.GetProposedRelation().ordinal()],
					treaty.GetExpireDate().ShortString(), treaty.UnstableIfReject() ? Texts.infoTreatyUnstableIfReject : "");
		case UNKNOWN:
			break;
		default:
			break;
		}
		return null;
	}

	private ScrollListComponent info_list;
	
	private static final int[] kColumnWidth = { 110, 450, 20, 20};
	private static final String[] kColumnNames = { Texts.time, Texts.content, null, null};
}
