package ac.ui.swing.panel;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.dialog.PersonSelectionDialog;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.ColorUtil;
import ac.ui.swing.util.TextWriter.Alignment;
import ac.util.StringUtil;

public class CityTopBanner extends TypedDataPanel<City> {

	private static final long serialVersionUID = 1L;

	public CityTopBanner(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		country = new TextElement(new Rectangle(10, 5, 60, 20));
		country.SetFontSize(14)
			.SetAlignment(Alignment.CENTER)
			.SetUseHandCursor(true);
		capital = new TextElement(new Rectangle(80, 5, 40, 20));
		capital.SetFontSize(14).SetHasFrame(false);
		pop = new TextElement(new Rectangle(110, 5, 125, 20));
		pop.SetFontSize(14).SetAlignment(Alignment.LEFT_MIDDLE);
		governor = new TextElement(new Rectangle(245, 5, 100, 20));
		governor.SetFontSize(14).SetAlignment(Alignment.LEFT_MIDDLE);
		riot = new TextElement(new Rectangle(380, 5, 90, 20));
		riot.SetFontSize(14).SetAlignment(Alignment.LEFT_MIDDLE);
		AddVisualElement(country);
		AddVisualElement(capital);
		AddVisualElement(pop);
		AddVisualElement(governor);
		AddVisualElement(riot);
		
		assign_governer.setBounds(350, 8, 15, 15);
		assign_governer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PersonSelectionDialog(cmp.city, cmp, data, () -> GetData(), PersonSelectionDialog.AssignCityGovernor(() -> GetData()));
			}
		});
		AddPlayerElement(assign_governer);
	}
	
	public void Reset(City city) {
		State owner = city.GetOwner();
		
		country.SetText(owner.GetName());
		country.SetTextColor(ColorUtil.GetStateForegroundColor(owner.ColorIndex()));
		country.SetBackgroundColor(ColorUtil.GetStateBackgroundColor(owner.ColorIndex()));
		country.SetClickCallback(() -> {cmp.state.Show(owner);});
		
		pop.SetText(Texts.population + ": " + StringUtil.LongNumber(city.GetTotalPopulation()));
		
		capital.SetText(Texts.cityTypeSymbol[city.GetType().ordinal()]);
		
		Person governor_person = city.GetGovernor();
		if (governor_person != null) {
			governor.SetText(Texts.governor + ": " + governor_person.GetName()).SetClickCallback(() -> cmp.person.Show(governor_person));
			governor.SetUseHandCursor(true);
		} else {
			governor.SetText(Texts.governor + ": " + Texts.none).SetClickCallback(null);
			governor.SetUseHandCursor(false);
		}

		
		if (owner.Playable() && data.GetParam().riot_point_multiplier_by_city_type[city.GetType().ordinal()] > 0) {
			int increase = cmp.utils.city_util.GetRiotPoint(city);
			riot.SetText(String.format("%s: %.2f%%", Texts.riot, (double)city.Get().riot / 100));
			riot.SetTooltipText(increase > 0 ? String.format("%s + %.2f / %s",  Texts.riot, (double)increase / 100, Texts.day) : null);
		} else {
			riot.SetText("");
			riot.SetTooltipText(null);
		}
		
		assign_governer.setEnabled(cmp.utils.person_util.CityAllowsGovernor(city));
	}
	
	private TextElement country;
	private TextElement capital;
	private TextElement pop;
	private TextElement governor;
	private TextElement riot;
	
	// Player Element
	private JButton assign_governer = new JButton(Texts.settingIcon);
}
