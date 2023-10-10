package ac.ui.swing.frame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashMap;
import ac.data.base.Position;
import ac.data.constant.Texts;
import ac.data.constant.Tile;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Improvement.SpecialImprovementType;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.engine.data.State;
import ac.ui.swing.Components;
import ac.ui.swing.GenericFrame;
import ac.ui.swing.GenericPanel;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.elements.VisualElement;
import ac.ui.swing.util.ColorUtil;
import ac.ui.swing.util.ShapeDrawer;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;
import ac.util.StringUtil;

public class BattleFieldFrame extends GenericFrame {
	private static final long serialVersionUID = 1L;
	public BattleFieldFrame(Components cmp, DataAccessor data) {
		super(cmp, data, 480, 400);
		
		top = new TopPanel(cmp, data);
		top.setBounds(5, 0, 470, 55);
		add(top);
		field = new FieldPanel(cmp, data);
		field.setBounds(5, 55, 470, 345);
		add(field);
		
		setAlwaysOnTop(true);
		setLayout(null);
		setVisible(false);
	}
	
	public void Show(Position pos) {
		tile = data.GetConstData().GetTile(pos);
		city = data.GetCityTerritoryByTile(tile);
		setTitle(Texts.battleField);
		
		Reset();
		repaint();
		setVisible(true);
	}
	
	public void Reset() {
		territory_city.SetText(city.GetName());
		territory_city.SetClickCallback(() -> cmp.city.Show(city));
		State owner = city.GetOwner();
		
		state.SetText(owner.GetName());
		state.SetTextColor(ColorUtil.GetStateForegroundColor(owner.ColorIndex()));
		state.SetBackgroundColor(ColorUtil.GetStateBackgroundColor(owner.ColorIndex()));
		state.SetClickCallback(() -> {cmp.state.Show(owner);});
		terrain.SetText(Texts.terrains[tile.terrain.ordinal()]);
		terrain.SetBackgroundColor(ColorUtil.GetTerrainColor(tile.terrain));
		river.SetText(cmp.utils.army_util.TileHasRiver(tile) ? Texts.river : "");
		
		if (city.GetPosition().equals(tile.coordinate)) {
			garrison.SetVisibility(true);
			garrison.SetArmy(city.GetMilitary().GetGarrison());
			wall.SetVisibility(true);
			String fort_info = "";
			if (city.GetImprovements().GetFinishedSpecialImprovement() == SpecialImprovementType.FORT) {
				fort_info = " + " + Texts.specialImprovements[SpecialImprovementType.FORT.ordinal()];
			}
			wall.SetText(Texts.cityIcon + ": " + 
					StringUtil.Number(city.GetImprovements().GetCount(ImprovementType.WALL)) + fort_info);
			wall.SetTooltipText(Texts.defenceBuffer + ": " + StringUtil.Percentage(cmp.utils.army_util.GetCityDefenceBuffer(city)));
		} else {
			garrison.SetVisibility(false);
			wall.SetVisibility(false);
			wall.SetTooltipText(null);
		}
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < army_frames[i].length; ++j) {
				army_frames[i][j].SetVisibility(false);
				armies[i][j] = null;
			}
		}
		army_pos.clear();
		army_pos.put(city.GetMilitary().GetGarrison(), new Position(0, -1));
		int self = 0;
		int enemy = 0;
		int friend = 0;
		for (Army army : data.GetArmiesByPosition(tile.coordinate)) {
			if (army.GetState() == owner) {
				army_pos.put(army, new Position(0, self));
				armies[0][self] = army;
				army_frames[0][self++].SetArmy(army);
				
				continue;
			}
			boolean is_enemy = false;
			if (army.GetTarget() != null && army.GetTarget().GetState() == owner) {
				is_enemy = true;
			} else {
				for (Army self_army : owner.GetMilitary().GetArmies()) {
					if (self_army.GetTarget() != null && self_army.GetTarget().GetState() == army.GetState()) {
						is_enemy = true;
						break;
					}
				}
			}
			if (is_enemy) {
				army_pos.put(army, new Position(1, enemy));
				armies[1][enemy] = army;
				army_frames[1][enemy++].SetArmy(army);
			} else {
				army_pos.put(army, new Position(2, friend));
				armies[2][friend] = army;
				army_frames[2][friend++].SetArmy(army);
			}
		}
	}

	@Override
	protected void Refresh() {
		Reset();
		repaint();
	}
	
	private class TopPanel extends GenericPanel {
		private static final long serialVersionUID = 1L;

		protected TopPanel(Components cmp, DataAccessor data) {
			super(cmp, data);
			territory_city.SetUseHandCursor(true);
			river.SetTextColor(ColorUtil.GetRiverColor());
			state.SetAlignment(Alignment.CENTER_TOP).SetUseHandCursor(true);
			
			AddVisualElement(territory_city);
			AddVisualElement(state);
			AddVisualElement(terrain);
			AddVisualElement(river);
			AddVisualElement(wall);
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			
			TextWriter text_writer = new TextWriter(g);
			text_writer.SetFontSize(15);
			text_writer.DrawString(5, 5, Texts.city);
			text_writer.DrawString(245, 5, Texts.mapLayerTerrain);
		}
	}
	
	private class FieldPanel extends GenericPanel {
		private static final long serialVersionUID = 1L;

		protected FieldPanel(Components cmp, DataAccessor data) {
			super(cmp, data);

			AddVisualElement(garrison);
			for (int i = 0; i < 3; ++i) {
				for (int j = 0; j < army_frames[i].length; ++j) {
					army_frames[i][j] = new ArmyElement(new Rectangle(
							5 + kArmySpaceWidth * i, 5 + kArmySpaceHeight * (j + 1),
							kArmyBoxWidth, kArmyBoxHeight));
					AddVisualElement(army_frames[i][j]);
				}
			}
		}
		
		public void paintComponent(Graphics g)  {
			super.paintComponent(g);
			
			ShapeDrawer shape_drawer = new ShapeDrawer(g);
			g.setColor(Color.RED);
			for (int i = 0; i < 3; ++i) {
				for (int j = 0; j < armies[i].length; ++j) {
					Army army = armies[i][j];
					if (army == null) continue;
					Army target = army.GetCombat().GetTarget();
					if (target == null) continue;
					if (target.GetState() == army.GetState()) {
						// City ownership swapped
						return;
					}
					Position attacker_pos = army_pos.get(army);
					Position defender_pos = army_pos.get(target);
					int y1 = 5 + kArmySpaceHeight * (attacker_pos.y + 1) + kArmyBoxHeight / 2;
					int y2 = 5 + kArmySpaceHeight * (defender_pos.y + 1) + kArmyBoxHeight / 2;
					if (attacker_pos.x < defender_pos.x) {
						shape_drawer.DrawArrow(5 + attacker_pos.x * kArmySpaceWidth + kArmyBoxWidth + 5, y1,
								5 + defender_pos.x * kArmySpaceWidth - 5, y2);
					} else {
						shape_drawer.DrawArrow(5 + attacker_pos.x * kArmySpaceWidth - 5, y1,
								5 + defender_pos.x * kArmySpaceWidth + kArmyBoxWidth + 5, y2);
					}
				}
			}
			
		}
	}
	
	private class ArmyElement extends VisualElement {

		public ArmyElement(Rectangle area) {
			super(area);
			this.SetUseHandCursor(true);
		}
		
		public void SetArmy(Army army) {
			this.army = army;
			this.SetClickCallback(() -> cmp.army.Show(army));
			this.SetVisibility(true);
		}

		@Override
		public void Draw(Graphics g) {
			ShapeDrawer shape_drawer = new ShapeDrawer(g);
			shape_drawer.DrawField(area, null);
			TextWriter text_writer = new TextWriter(g);
			text_writer.SetFontSize(12);
			if (army != null) {
				text_writer.DrawString(area.x, area.y, army.GetName());
				 
				if (army.IsGarrison()) {
					text_writer.DrawString(area.x, area.y + 16, Texts.soldierIcon);
				} else {
					g.setColor(Color.LIGHT_GRAY);
					shape_drawer.DrawCircle(area.x + 6, area.y + 22, 6);
					g.setColor(ColorUtil.GetStateBackgroundColor(army.GetState().ColorIndex()));
					shape_drawer.DrawCircle(area.x + 6, area.y + 22, 5);
					text_writer.SetAlignment(Alignment.CENTER);
					text_writer.SetFontSize(9);
					text_writer.SetColor(ColorUtil.GetStateForegroundColor(army.GetState().ColorIndex()));
					text_writer.DrawString(area.x + 6, area.y + 22, army.GetId());
				}
				text_writer.SetFontSize(12);
				text_writer.SetColor(Color.BLACK);
				text_writer.SetAlignment(Alignment.LEFT);
				text_writer.DrawString(area.x + 16, area.y + 16, StringUtil.LongNumber(army.GetTotalSoldier()));
				text_writer.SetAlignment(Alignment.RIGHT);
				text_writer.DrawString(area.x + area.width, area.y,
						String.format("(%d) ", (int)(army.GetMorale() * 100)));
				
				
				long killed = army.GetCombat().GetKilled();
				if (killed > 0) {
					text_writer.SetColor(Color.RED);
					text_writer.DrawString(area.x + area.width, area.y + 16, "-" + StringUtil.LongNumber(killed));
				}
			}
		}
		
		private Army army;
	}

	private Tile tile;
	private City city;
	private TopPanel top;
	private FieldPanel field;
	
	private TextElement territory_city = new TextElement(new Rectangle(60, 5, 90, 20));
	private TextElement state = new TextElement(new Rectangle(165, 5, 60, 20));
	private TextElement terrain = new TextElement(new Rectangle(300, 5, 90, 20));
	private TextElement river = new TextElement(new Rectangle(405, 5, 20, 20));
	private TextElement wall = new TextElement(new Rectangle(5, 40, 120, 15));
	
	private static final int kArmyBoxWidth = 120;
	private static final int kArmyBoxHeight = 30;
	private static final int kArmySpaceWidth = kArmyBoxWidth + 50;
	private static final int kArmySpaceHeight = kArmyBoxHeight + 15;
	private ArmyElement garrison = new ArmyElement(new Rectangle(5, 5, kArmyBoxWidth, kArmyBoxHeight));
	private ArmyElement[][] army_frames = new ArmyElement[3][Tile.kMaxArmies];
	private Army[][] armies = new Army[3][Tile.kMaxArmies];
	private HashMap<Army, Position> army_pos = new HashMap<Army, Position>();  // Field position
}
