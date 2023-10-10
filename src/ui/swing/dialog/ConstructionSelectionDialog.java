package ac.ui.swing.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.function.Supplier;

import javax.swing.JButton;

import ac.data.base.Pair;
import ac.data.constant.Improvement;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;

public class ConstructionSelectionDialog extends CityDialog {
	private static final long serialVersionUID = 1L;

	public ConstructionSelectionDialog(GenericFrame parent, Components cmp, DataAccessor data, Supplier<City> city_getter) {
		super(parent, cmp, data, city_getter, Texts.select + Texts.construction, 600, 400, false);
		for (Pair<JButton, ImprovementType> pair : constructions) {
			pair.first.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					data.GetPlayer().AppendConstructionImprovement(city, pair.second);
					cmp.Repaint();
				}
			});
			add(pair.first);
		}
		
		InitDone();
		Refresh();
	}

	@Override
	protected void Refresh() {
		State state = city.GetOwner();
		for (Pair<JButton, ImprovementType> pair : constructions) {
			pair.first.setEnabled(cmp.utils.state_util.IsImprovementAvailable(state, pair.second));
			//pair.first.setEnabled(cmp.utils.state_util.IsImprovementAffordable(state, city, pair.second));
		}
	}

	@Override
	protected void Confirm() {
	}

	private ArrayList<Pair<JButton, ImprovementType>> constructions = new ArrayList<Pair<JButton, ImprovementType>>() { private static final long serialVersionUID = 1L; {
		int x = 0; int y = 0;
		for (ImprovementType type : Improvement.kAgricultureImprovements) {
			if (type == ImprovementType.IRRIGATED_FARM) continue;
			JButton button = new JButton(Texts.improvements[type.ordinal()]);
			button.setBounds(10 + x * 150, 40 + y * 50, 100, 20);
			add(new Pair<JButton, ImprovementType>(button, type));
			y++;
		}
		x++; y = 0;
		for (ImprovementType type : Improvement.kIndustryImprovements) {
			if (type == ImprovementType.WORKSHOP) continue;
			JButton button = new JButton(Texts.improvements[type.ordinal()]);
			button.setBounds(10 + x * 150, 40 + y * 50, 100, 20);
			add(new Pair<JButton, ImprovementType>(button, type));
			y++;
		}
		x++; y = 0;
		for (ImprovementType type : Improvement.kAuxiliaryImprovements) {
			JButton button = new JButton(Texts.improvements[type.ordinal()]);
			button.setBounds(10 + x * 150, 40 + y * 50, 100, 20);
			add(new Pair<JButton, ImprovementType>(button, type));
			y++;
		}
	}};
}
