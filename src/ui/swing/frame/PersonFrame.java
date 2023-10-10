package ac.ui.swing.frame;

import java.awt.Graphics;
import java.awt.Rectangle;

import ac.data.constant.Ability;
import ac.data.constant.Texts;
import ac.data.constant.Ability.AbilityType;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.Person;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;
import ac.ui.swing.GenericPanel;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.BarDrawer;
import ac.ui.swing.util.ColorUtil;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;

public class PersonFrame extends GenericFrame {

	private static final long serialVersionUID = 1L;

	public PersonFrame(Components cmp, DataAccessor data) {
		super(cmp, data, 350, 210);
		setAlwaysOnTop(true);
		setLayout(null);
		setVisible(false);
		
		panel = new PersonPanel(cmp, data);
		add(panel);
		
	}
	
	public void Show(Person person) {
		this.person = person;
		setTitle(person.GetName());
		Person f = person.GetFather();
		if (f != null) {
			father.SetUseHandCursor(true);
			father.SetText(f.GetName()).SetClickCallback(() -> Show(f));
		} else {
			father.SetUseHandCursor(false);
			father.SetText("").SetClickCallback(null);
		}
		
		City city = person.GetHometown();
		if (city != null) {
			hometown.SetUseHandCursor(true);
			hometown.SetText(city.GetName()).SetClickCallback(() -> { cmp.city.Show(person.GetHometown()); });
		} else {
			hometown.SetUseHandCursor(false);
			hometown.SetText("").SetClickCallback(null);
		}
		
		State owner = person.GetOwner();
		
		if (owner != null) {
			state.SetText(owner.GetName());
			state.SetTextColor(ColorUtil.GetStateForegroundColor(owner.ColorIndex()));
			state.SetBackgroundColor(ColorUtil.GetStateBackgroundColor(owner.ColorIndex()));
			state.SetClickCallback(() -> {cmp.state.Show(owner);});
			state.SetUseHandCursor(true);
		} else {
			state.SetText(Texts.none);
			state.SetBackgroundColor(ColorUtil.GetStateBackgroundColor(0));
			state.SetClickCallback(null);
			state.SetUseHandCursor(false);
		}
		
		repaint();
		setVisible(true);
	}

	private class PersonPanel extends GenericPanel {
		private static final long serialVersionUID = 1L;

		protected PersonPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			state.SetFontSize(14).SetAlignment(Alignment.CENTER).SetUseHandCursor(true);
			AddVisualElement(state);
			AddVisualElement(father);
			AddVisualElement(hometown);
		}

		public void paintComponent(Graphics g)  {
			super.paintComponent(g);

			BarDrawer bar_drawer = new BarDrawer(g);
			bar_drawer.SetColors(BarDrawer.kThresholdSpectrum);
			bar_drawer.SetUseSpectrum(true);
			TextWriter text_writer = new TextWriter(g);
			text_writer.SetFontSize(12);
			
			for (int i = 0; i < Ability.kMaxTypes; ++i) {
				text_writer.DrawString(10, 10 + i * 20, Texts.abilities[i]);
				int ability = person.GetAbility(AbilityType.values()[i]);
				bar_drawer.DrawBar(abilities[i], Ability.kMaxAbility, ability);
			}
			text_writer.DrawString(10, 120, Texts.employedBy);
			
			if (person.IsFake()) {
				text_writer.DrawString(185, 10, Texts.fakePerson);
			} else {
				text_writer.DrawString(185, 10, Texts.familyName + ": " + person.GetFamilyName());
				text_writer.DrawString(185, 30, Texts.surname + ": " + person.GetSurname());
				text_writer.DrawString(185, 50, Texts.givenName + ": " + person.GetGivenName());
				text_writer.DrawString(185, 70, Texts.courtesyName + ": " + person.GetCourtesyName());
				text_writer.DrawString(185, 90, Texts.posthumousName + ": " + person.GetPosthumousName());
				text_writer.DrawString(185, 120, Texts.father);
				text_writer.DrawString(185, 150, Texts.hometown);
			}
		}
	}
	

	@Override
	protected void Refresh() {
		repaint();
	}

	private PersonPanel panel;
	private Person person;
	private TextElement father = new TextElement(new Rectangle(220, 120, 80, 18));
	private TextElement hometown = new TextElement(new Rectangle(220, 150, 80, 18));
	private TextElement state = new TextElement(new Rectangle(85, 120, 60, 18));

	private Rectangle[] abilities = { new Rectangle(45, 12, 100, 14), new Rectangle(45, 32, 100, 14), new Rectangle(45, 52, 100, 14) };
}
