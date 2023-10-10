package ac.ui.swing.frame;

import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.TabbedFrame;
import ac.ui.swing.panel.CityAgriculturePanel;
import ac.ui.swing.panel.CityBuildingPanel;
import ac.ui.swing.panel.CityCommercePanel;
import ac.ui.swing.panel.CityConstructionPanel;
import ac.ui.swing.panel.CityIndustryPanel;
import ac.ui.swing.panel.CityInfoPanel;
import ac.ui.swing.panel.CityMilitaryPanel;
import ac.ui.swing.panel.CityOverviewPanel;
import ac.ui.swing.panel.CityTopBanner;

public class CityFrame extends TabbedFrame<City> {
	private static final long serialVersionUID = 1L;

	public CityFrame(Components cmp, DataAccessor data) {
		super(cmp, data, new CityTopBanner(cmp, data));
		
		overview = new CityOverviewPanel(cmp, data);
		agriculture = new CityAgriculturePanel(cmp, data);
		industry = new CityIndustryPanel(cmp, data);
		commerce = new CityCommercePanel(cmp, data);
		military = new CityMilitaryPanel(cmp, data);
		building = new CityBuildingPanel(cmp, data);
		construction = new CityConstructionPanel(cmp, data);
		
		info = new CityInfoPanel(cmp, data);		
	}
	
	@Override
	protected void ShowInternal(City t) {
		setTitle(t.GetName());
		ClearTabs();
		boolean playable = t.GetOwner().Playable();
		if (playable) {
			AddTab(Texts.overivew, overview);
			AddTab(Texts.agriculture, agriculture);
			AddTab(Texts.industry, industry);
			AddTab(Texts.commerce, commerce);
			AddTab(Texts.military, military);
			AddTab(Texts.building, building);
			AddTab(Texts.construction, construction);
		} else {
			AddTab(Texts.military, military);
		}
		AddTab(Texts.information, info);
	}
	
	@Override
	protected boolean IsPlayerElementVisible(City t) {
		return t.GetOwner() == data.GetPlayer().GetState();
	}

	private CityOverviewPanel overview;
	private CityAgriculturePanel agriculture;
	private CityIndustryPanel industry;
	private CityCommercePanel commerce;
	private CityMilitaryPanel military;
	private CityBuildingPanel building;
	private CityConstructionPanel construction;
	private CityInfoPanel info;
}
