package ac.ui.swing.panel;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

import ac.data.base.Date;
import ac.data.base.Pair;
import ac.data.base.Position;
import ac.data.constant.Texts;
import ac.data.constant.Tile;
import ac.engine.Action;
import ac.engine.Action.ActionType;
import ac.engine.data.Army;
import ac.engine.data.Army.Status;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.GenericPanel;
import ac.ui.swing.dialog.SaveSlotSelectionDialog;
import ac.ui.swing.elements.BadgeElement;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.BarDrawer;
import ac.ui.swing.util.ColorUtil;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;

public class SidePanel extends GenericPanel {

	private static final long serialVersionUID = 1L;

	public SidePanel(Components cmp, DataAccessor data, Runnable pause, Runnable resume) {
		super(cmp, data);
		
		setLayout(null);
		date = new TextElement(new Rectangle(0, 5, 125, 20));
		date.SetHasFrame(false).SetFontSize(13).SetAlignment(Alignment.LEFT);
		buttonResume.setBounds(0, 25, 20, 20);
		buttonSlower.setBounds(25, 25, 20, 20);
		buttonFaster.setBounds(105, 25, 20, 20);
		buttonSave.setBounds(0, 50, 60, 20);
		buttonMenu.setBounds(65, 50, 60, 20);
		
		state = new TextElement(new Rectangle(0, 80, 55, 18));		
		city = new TextElement(new Rectangle(60, 80, 65, 18));
		terrain = new TextElement(new Rectangle(0, 105, 40, 18));
		state.SetAlignment(Alignment.CENTER).SetUseHandCursor(true);
		city.SetAlignment(Alignment.CENTER).SetUseHandCursor(true);
		terrain.SetAlignment(Alignment.CENTER);
		
		army_name = new TextElement(new Rectangle(0, 140, 125, 18));		
		army_name.SetUseHandCursor(true);
		army_target = new TextElement(new Rectangle(0, 165, 125, 18));
		choose_target = new JToggleButton(Texts.choose);
		choose_target.setBounds(0, 190, 40, 18);
		choose_target.setVisible(false);
		pursue_target = new JToggleButton(Texts.pursueAttack);
		pursue_target.setBounds(43, 190, 40, 18);
		pursue_target.setVisible(false);
		effective_soldiers = new TextElement(new Rectangle(0, 240, 125, 18));
		duration = new TextElement(new Rectangle(0, 290, 125, 18));
		
		reset_target = new JButton(Texts.cancel);
		reset_target.setBounds(86, 190, 40, 18);
		reset_target.setVisible(false);
		reset_target.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Action action = new Action(ActionType.RESET_TARGET);
				action.object = selected_army;
				cmp.action_consumer.accept(action);
				Repaint();
			}
		});
		

	    text_area = new JTextArea();
	    text_area.setEditable(false);
	    text_area.setLineWrap(true);
	    text_area.setFont(new Font(text_area.getFont().getFontName(), 0, 10));
	    pane = new JScrollPane(text_area);
	    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    pane.setBounds(0, 330, 122, 160);
	    
		buttonOverview.setBounds(0, 500, 50, 20);
		buttonInformation.setBounds(60, 500, 60, 20);
		comboLayer.setBounds(0, 530, 120, 25);
	    
		add(buttonResume);
		add(buttonSlower);
		add(buttonFaster);
		add(buttonSave);
		add(buttonMenu);
		add(buttonOverview);
		add(buttonInformation);
		add(choose_target);
		add(pursue_target);
		add(reset_target);
		add(comboLayer);
		add(pane);
		
		state.SetVisibility(false);
		city.SetVisibility(false);
		terrain.SetVisibility(false);
		army_name.SetVisibility(false);
		army_target.SetVisibility(false);
		AddVisualElement(date);
		AddVisualElement(state);
		AddVisualElement(city);
		AddVisualElement(terrain);
		AddVisualElement(army_name);
		AddVisualElement(army_target);
		AddVisualElement(effective_soldiers);
		AddVisualElement(duration);
		
		buttonResume.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonResume.setText(paused ? "⏸" : "⏵");
				paused = !paused;
				if (paused) {
					pause.run();
					buttonSave.setEnabled(true);
				} else {
					buttonSave.setEnabled(false);
					resume.run();
				}
			}
		});
		buttonSlower.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (data.GetConfig().speed_scale < kMaxSpeedScale - 1) {
					data.GetConfig().SetSpeedScale(data.GetConfig().speed_scale + 1);
					repaint();
				}
			}
		});
		buttonFaster.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (data.GetConfig().speed_scale > 0) {
					data.GetConfig().SetSpeedScale(data.GetConfig().speed_scale - 1);
					repaint();
				}
			}
		});
		buttonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SaveSlotSelectionDialog(cmp.main, cmp, data, Texts.save, i -> data.Save(i), false);
			}
		});
		buttonMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmp.SetEnabled(false);
				cmp.ShowGameSelection();
//				ConfigData config = data.GetConfig();
//				data.Load(0);
//				data.SetConfig(config);
//				cmp.Repaint();
			}
		});
		buttonOverview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmp.overview.Show(null);
			}
		});
		buttonInformation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmp.information.Show();
			}
		});
		comboLayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				data.GetConfig().map_layer = comboLayer.getSelectedIndex();
				cmp.map_panel.ChangeLayer();
			}
		});
	}

	public void paintComponent(Graphics g)  {
		super.paintComponent(g);
		BarDrawer bar_drawer = new BarDrawer(g);
		final Rectangle speed_rect = new Rectangle(50, 25, 50, 20);
		bar_drawer.SetWriteText(false);
		bar_drawer.DrawRatio(speed_rect, kMaxSpeedScale - data.GetConfig().speed_scale, kMaxSpeedScale);
		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(13);
		if (IsSelectingTarget() && effective_soldiers.GetVisibility()) {
			text_writer.DrawString(0, 215, Texts.estimatedEffectiveSoldiers + ":");
			text_writer.DrawString(0, 265, Texts.estimatedDuration + ":");
		}
	}
	
	public void ShowTile(Position pos) {
		selected_pos = pos;
		Repaint();
	}
	
	public void ShowArmy(Army army) {
		if (army.GetState() == data.GetPlayer().GetState() && !army.IsGarrison()) {
			selected_army = army;
		} else {
			selected_army = null;
		}
		Repaint();
	}
	
	public void Repaint() {
		ConsumeMessages();
		Reset();
		repaint();
	}
	private void Reset() {
		date.SetText(data.GetDate().toString());
		buttonInformation.setText(String.format("%s (%d)", Texts.exclamationIcon, data.GetPlayer().GetInformationList().size()));
		Tile tile = data.GetConstData().GetTile(selected_pos);
		if (tile != null) {
			terrain.SetText(Texts.terrains[tile.terrain.ordinal()]);
			terrain.SetBackgroundColor(ColorUtil.GetTerrainColor(tile.terrain));
			terrain.SetVisibility(true);
		} else {
			terrain.SetText("");
			terrain.SetBackgroundColor(getBackground());
			terrain.SetVisibility(false);
		}
		City selected_city = data.GetCityTerritoryByTile(tile);
		effective_soldiers.SetVisibility(false);
		duration.SetVisibility(false);
		if (selected_city != null) {
			city.SetText(selected_city.GetName());
			
			State owner = selected_city.GetOwner();
			state.SetText(owner.GetName());
			state.SetTextColor(ColorUtil.GetStateForegroundColor(owner.ColorIndex()));
			state.SetBackgroundColor(ColorUtil.GetStateBackgroundColor(owner.ColorIndex()));
			state.SetClickCallback(() -> {cmp.state.Show(owner);});
			
			terrain.SetClickCallback(() -> cmp.battle_field.Show(selected_pos));
			terrain.SetUseHandCursor(true);

			State player_state = data.GetPlayer().GetState();
			City nearest_city = cmp.utils.trans_util.GetNearestCity(player_state, selected_city);
			
			if (IsSelectingTarget() && nearest_city != null) {
				city.SetClickCallback(() -> {
					Action action = new Action(ActionType.TARGET_CITY);
					action.object = selected_army;
					action.object2 = selected_city;
					cmp.action_consumer.accept(action);
					choose_target.setSelected(false);
					cmp.Repaint();
				});
				city.SetUseTargetCursor(true);
				if (choose_target.isSelected() && selected_army != null) {
					long mobilized_labor = 0L;
					for (City city : nearest_city.GetNeighbors()) {
						if (city.GetOwner() != player_state) continue;
						mobilized_labor += cmp.utils.army_util.GetMoblizableLabor(city);
					}
					effective_soldiers.SetNumber((long)((double)cmp.utils.army_util.GetEffectiveSoldierBySupply(selected_army,
							nearest_city.GetTransportation(selected_city), mobilized_labor)));
					effective_soldiers.SetVisibility(true);
					long daily_consumption = cmp.utils.army_util.GetDailyFoodConsumption(selected_army, mobilized_labor, false);
					daily_consumption += cmp.utils.state_util.GetFoodExpense(selected_army.GetState()) / 30;
					int estimated_days = (int) (player_state.GetResource().food / daily_consumption);
					duration.SetText(estimated_days + Texts.day);
					duration.SetVisibility(true);
				}
			} else {
				city.SetClickCallback(() -> cmp.city.Show(selected_city));
				city.SetUseHandCursor(true);
			}
			city.SetVisibility(true);
			state.SetVisibility(true);
		} else {
			city.SetVisibility(false);
			state.SetVisibility(false);
			terrain.SetClickCallback(null);
			terrain.SetUseHandCursor(false);
		}
		
		for (BadgeElement badge : army_badges) {
			badge.SetVisibility(false);
		}
		
		ArrayList<Army> armies = new ArrayList<Army>(data.GetArmiesByPosition(selected_pos));
		if (!armies.isEmpty()) {
			float width = 82 / armies.size();
			for (int i = 0; i < armies.size(); ++i) {
				BadgeElement badge = null;
				Rectangle badge_area = new Rectangle(Math.round(50 + i * width + width / 2 - 7), 107, 14, 14);
				if (i < army_badges.size()) {
					badge = army_badges.get(i);
					badge.Resize(badge_area);
					
				} else {
					badge = new BadgeElement(badge_area, 10);
					army_badges.add(badge);
					AddVisualElement(badge);
				}
				Army army = armies.get(i);
				badge.SetForegroundColor(ColorUtil.GetStateForegroundColor(army.GetState().ColorIndex()));
				badge.SetBackgroundColor(ColorUtil.GetStateBackgroundColor(army.GetState().ColorIndex()));
				badge.SetText(army.GetId()).SetUseHandCursor(true);
				
				if (IsSelectingTarget() && army.GetState() != data.GetPlayer().GetState()) {
					badge.SetClickCallback(() -> {
						Action action = new Action(ActionType.TARGET_ARMY);
						action.object = selected_army;
						action.object2 = army;
						action.quantity = pursue_target.isSelected() ? 1 : 0;
						cmp.action_consumer.accept(action);
						choose_target.setSelected(false);
						cmp.Repaint();
					});
					badge.SetUseTargetCursor(true);
				} else {
					badge.SetClickCallback(() -> cmp.army.Show(army));
					badge.SetUseHandCursor(true);
				}
				badge.SetVisibility(true);
			}
		}
		
		if (selected_army != null) {
			army_name.SetText(Texts.horseIcon + " " + selected_army.GetFullName());
			army_name.SetClickCallback(() -> cmp.army.Show(selected_army));
			Army target_army = selected_army.GetTarget();
			Status status = selected_army.GetStatus();
			if (status != Status.IDLE && status != Status.RETREAT && target_army != null) {
				army_target.SetText(Texts.targetIcon + " " + target_army.GetFullName());
				army_target.SetClickCallback(() -> cmp.army.Show(target_army));
				army_target.SetUseHandCursor(true);
			} else {
				army_target.SetText(Texts.targetIcon + " " + Texts.none);
				army_target.SetClickCallback(null);
				army_target.SetUseHandCursor(false);
			}
			army_name.SetVisibility(true);
			army_target.SetVisibility(true);
			choose_target.setVisible(true);
			reset_target.setVisible(true);
			pursue_target.setVisible(true);
		} else {
			army_name.SetVisibility(false);
			army_target.SetVisibility(false);
			choose_target.setVisible(false);
			reset_target.setVisible(false);
			pursue_target.setVisible(false);
		}
	}
	
	public boolean IsSelectingTarget() {
		return choose_target.isSelected();
	};
	
	private void ConsumeMessages() {
		boolean changed = false;
		for (;;) {
			Pair<Date, String> message = data.GetMessages().PullMessage();
			if (message == null) break;
			message_lines.addFirst(String.format("%s : %s", message.first.ShortString(), message.second));
			if (message_lines.size() > kMaxMessages) message_lines.removeLast();
			changed = true;
		}
		if (!changed) return;
		text_area.grabFocus();
		text_area.setText(String.join("\n", message_lines));
		text_area.scrollRectToVisible(new Rectangle(0, 0, 120, 210));
	}
	
	private JButton buttonResume = new JButton(Texts.resumeIcon);
	private JButton buttonSlower = new JButton(Texts.slowerIcon);
	private JButton buttonFaster = new JButton(Texts.fasterIcon);
	private JButton buttonSave = new JButton(Texts.save);
	private JButton buttonMenu = new JButton(Texts.menu);
	private JButton buttonOverview = new JButton(Texts.overivew);
	private JButton buttonInformation = new JButton(Texts.exclamationIcon + " (0)");
	private JComboBox<String> comboLayer = new JComboBox<String>(mapLayers);
	public static final String[] mapLayers = { Texts.mapLayerTerritory, Texts.mapLayerTerrain, Texts.rain, Texts.temperature, Texts.disaster };
	private boolean paused = true;
	private static final int kMaxSpeedScale = 6;
	
	private TextElement date;
	private TextElement city;
	private TextElement state;
	private TextElement terrain;
	private ArrayList<BadgeElement> army_badges = new ArrayList<BadgeElement>();
	private TextElement army_name;
	private TextElement army_target;
	private TextElement effective_soldiers;
	private TextElement duration;
	private JToggleButton choose_target;
	private JToggleButton pursue_target;
	private JButton reset_target;
	
	private JTextArea text_area;
	private JScrollPane pane;
	private LinkedList<String> message_lines = new LinkedList<String>();
	private static final int kMaxMessages = 50;
	
	protected Position selected_pos;
	private Army selected_army;
}
 