package ac.ui.swing.panel;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JScrollPane;

import ac.data.base.Date;
import ac.data.base.Resource;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Texts;
import ac.engine.data.Army;
import ac.engine.data.City;
import ac.engine.data.CityImprovements;
import ac.engine.data.CityMilitary;
import ac.engine.data.CityMilitary.RecruitmentInfo;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.dialog.ConstructionSelectionDialog;
import ac.ui.swing.dialog.RecruitmentDialog;
import ac.ui.swing.elements.RatioBarElement;
import ac.ui.swing.elements.ScrollListComponent;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;

public class CityConstructionPanel extends TypedDataPanel<City> {
	private static final long serialVersionUID = 1L;
	public CityConstructionPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		construction = new TextElement(new Rectangle(65, 10, 70, 15));
		construction_eta = new TextElement(new Rectangle(225, 10, 100, 15));
		construction_progress = new RatioBarElement(new Rectangle(335, 10, 120, 15));
		recruitment = new TextElement(new Rectangle(65, 40, 70, 15));
		recruitment_eta = new TextElement(new Rectangle(225, 40, 100, 15));
		recruitment_progress = new RatioBarElement(new Rectangle(335, 40, 120, 15));
		AddVisualElement(construction);
		AddVisualElement(construction_eta);
		AddVisualElement(construction_progress);
		AddVisualElement(recruitment);
		AddVisualElement(recruitment_eta);
		AddVisualElement(recruitment_progress);
		
		construction_list = new ScrollListComponent(kConstructionColumnWidth, 18);
		construction_list.SetColumnHeaders(kConstructionColumnName);
		construction_list.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		construction_list.setBounds(5, 70, 140, 150);
		construct_cost_label = new TextElement(new Rectangle(5, 235, 30, 15));
		construct_cost_label.SetHasFrame(false).SetText(Texts.requirement);
		construct_cost = new TextElement(new Rectangle(45, 235, 180, 15));
		construct.setBounds(5, 265, 60, 15);
		construct.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ConstructionSelectionDialog(cmp.city, cmp, data, () -> GetData());
			}
		});
		construct_cancel.setBounds(85, 265, 60, 15);
		construct_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				data.GetPlayer().CancelLastConstructionImprovement(GetData());
				cmp.Repaint();
			}
		});
		recruitment_list = new ScrollListComponent(kRecruitmentColumnWidth, 18);
		recruitment_list.SetColumnHeaders(kRecruitmentColumnName);
		recruitment_list.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		recruitment_list.setBounds(185, 70, 270, 150);
		recruit_cost_label = new TextElement(new Rectangle(235, 235, 30, 15));
		recruit_cost_label.SetHasFrame(false).SetText(Texts.requirement);
		recruit_cost = new TextElement(new Rectangle(275, 235, 180, 15));
		recruit.setBounds(315, 265, 60, 15);
		recruit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new RecruitmentDialog(cmp.city, cmp, data, () -> GetData());
			}
		});
		recruit_cancel.setBounds(395, 265, 60, 15);
		recruit_cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				data.GetPlayer().CancelLastRecruitment(GetData());
				cmp.Repaint();
			}
		});
		AddPlayerElement(construction_list);
		AddPlayerElement(construct_cost_label);
		AddPlayerElement(construct_cost);
		AddPlayerElement(construct);
		AddPlayerElement(construct_cancel);
		AddPlayerElement(recruitment_list);
		AddPlayerElement(recruit_cost_label);
		AddPlayerElement(recruit_cost);
		AddPlayerElement(recruit);
		AddPlayerElement(recruit_cancel);
	}

	public void Reset(City city) {
		// Impr
		CityImprovements city_impr = city.GetImprovements();
		ImprovementType impr_type = city_impr.GetCurrentConstruction();
		if (impr_type == null) {
			construction.SetText("");
			construction_eta.SetText("");
			construction_progress.SetMax(0).SetValue(0);
		} else {
			int impr_index = impr_type.ordinal();
			Date eta_date = city_impr.GetConsturctionCompleteDate();
			construction.SetText(Texts.improvements[impr_index]);
			construction_eta.SetText(eta_date.ShortString());
			int construction_days = data.GetParam().improvement_construction_days[impr_index];
			construction_progress.SetMax(construction_days).SetValue(construction_days - eta_date.GetDifference(data.GetDate()));
		}
		
		// Recruit
		CityMilitary military = city.GetMilitary();
		RecruitmentInfo info = military.GetRecruitmentInfo();
		if (info == null) {
			recruitment.SetText("");
			recruitment_eta.SetText("");
			recruitment_progress.SetMax(0).SetValue(0);
		} else {
			Army army = info.army;
			recruitment.SetText(army.GetName());
			Date eta_date = military.GetRecruitmentCompleteDate();
			recruitment_eta.SetText(eta_date.ShortString());
			int recruitment_days = data.GetParam().base_recruitment_days;
			recruitment_progress.SetMax(recruitment_days).SetValue(recruitment_days - eta_date.GetDifference(data.GetDate()));
		}
		
		// Player Element
		ArrayList<ImprovementType> construction_queue = data.GetPlayer().GetConstructionQueue(city);
		if (construction_queue == null || construction_queue.isEmpty()) {
			construction_list.Resize(0);
			construct_cost.SetText("");
		} else {
			construction_list.Resize(construction_queue.size());
			for (int i = 0; i < construction_queue.size(); ++i) {
				int index = construction_queue.get(i).ordinal();
				construction_list.SetValue(i, 0, Texts.improvements[index]);
			}
			construct_cost.SetText(data.GetParam().GetImprovementCost(construction_queue.get(0)).toSimpleString());
		}
		ArrayList<RecruitmentInfo> recruitment_queue = data.GetPlayer().GetRecruitmentQueue(city);
		if (recruitment_queue == null || recruitment_queue.isEmpty()) {
			recruitment_list.Resize(0);
			recruit_cost.SetText("");
		} else {
			recruitment_list.Resize(recruitment_queue.size());
			for (int i = 0; i < recruitment_queue.size(); ++i) {
				info = recruitment_queue.get(i);
				recruitment_list.SetValue(i, 0, info.army.GetName());
				recruitment_list.SetValue(i, 1, Texts.soldierType[info.type.ordinal()]);
				recruitment_list.SetValue(i, 2, info.unit.name);
			}
			Resource<Long> cost = new Resource<Long>(0L);
			Resource.AddResource(cost, recruitment_queue.get(0).unit.cost, data.GetParam().base_recruitment);
			recruit_cost.SetText(cost.toSimpleString());
		}
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);

		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		text_writer.DrawString(5, 10, Texts.currentConstruction);
		text_writer.DrawString(145, 10, Texts.eta);
		text_writer.DrawString(5, 40, Texts.currentRecruitment);
		text_writer.DrawString(145, 40, Texts.eta);
	}
	
	private TextElement construction;
	private TextElement construction_eta;
	private RatioBarElement construction_progress;
	private TextElement recruitment;
	private TextElement recruitment_eta;
	private RatioBarElement recruitment_progress;
	
	// Player Element
	private ScrollListComponent construction_list;
	private static int[] kConstructionColumnWidth = { 120 };
	private static String[] kConstructionColumnName = { Texts.construction + Texts.queue };
	private ScrollListComponent recruitment_list;
	private static int[] kRecruitmentColumnWidth = { 100, 60, 90 };
	private static String[] kRecruitmentColumnName = { Texts.recruitment + Texts.queue, Texts.soldierSource, Texts.recruitment + Texts.unit };
	private TextElement construct_cost_label;
	private TextElement construct_cost;
	private TextElement recruit_cost_label;
	private TextElement recruit_cost;
	private JButton construct = new JButton(Texts.construction);
	private JButton construct_cancel = new JButton(Texts.withdraw);
	private JButton recruit = new JButton(Texts.recruitment);
	private JButton recruit_cancel = new JButton(Texts.withdraw);
}
