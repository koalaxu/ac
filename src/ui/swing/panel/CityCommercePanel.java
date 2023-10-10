package ac.ui.swing.panel;

import java.awt.Graphics;
import java.awt.Rectangle;

import ac.data.CityData.Profession;
import ac.data.constant.Improvement;
import ac.data.constant.Improvement.ImprovementType;
import ac.data.constant.Texts;
import ac.engine.data.City;
import ac.engine.data.CityCommerce;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.elements.TableElement;
import ac.ui.swing.elements.TextElement;
import ac.ui.swing.util.TextWriter;
import ac.util.StringUtil;

public class CityCommercePanel extends TypedDataPanel<City> {
	private static final long serialVersionUID = 1L;
	public CityCommercePanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		table = new TableElement(new Rectangle(5, 5, 64, 25), 9, 5);
		income = new TextElement(new Rectangle(65, 240, 100, 18));
		merchant = new TextElement(new Rectangle(230, 240, 100, 18));
		income_per_merchant = new TextElement(new Rectangle(355, 240, 80, 18));
		tax = new TextElement(new Rectangle(65, 265, 100, 18));
		AddVisualElement(table);
		AddVisualElement(income);
		AddVisualElement(merchant);
		AddVisualElement(income_per_merchant);
		AddVisualElement(tax);
		
		for (int i = 0; i < Improvement.kIndustryImprovements.length; ++i) {
			table.CreateCell(i + 1, 0).SetText(Texts.industryProduce[i]);
		}
		table.CreateCell(8, 0).SetText(Texts.sum);
		table.CreateCell(0, 1).SetText(Texts.imports);
		table.CreateCell(0, 2).SetText(Texts.transfers);
		table.CreateCell(0, 3).SetText(Texts.exports);
		table.CreateCell(0, 4).SetText(Texts.commercePoint);
	}
	
	public void Reset(City city) {
		CityCommerce commerce = city.GetCommerce();
		double total_points = 0.0;
		for (int i = 0; i < Improvement.kIndustryImprovements.length; ++i) {
			ImprovementType type = Improvement.kIndustryImprovements[i];
			City import_city = commerce.GetImports().get(type);
			String tooltip_prefix = "";
			if (import_city != null) {
				table.CreateCell(i + 1, 1).SetText(import_city.GetName());
				tooltip_prefix = String.format(Texts.tooltipCommercePointImport, data.GetParam().import_commerce_point);
			} else {
				table.CleanCell(i + 1, 1);
			}
			int transfers = commerce.GetNumTransfers(type);
			if (transfers > 0) {
				table.CreateCell(i + 1, 2).SetNumber(transfers);
			} else {
				table.CleanCell(i + 1, 2);
			}
			int exports = commerce.GetNumExports(type);
			if (exports > 0) {
				table.CreateCell(i + 1, 3).SetNumber(exports);
				tooltip_prefix = String.format(Texts.tooltipCommercePointExport, data.GetParam().export_commerce_point);
			} else {
				table.CleanCell(i + 1, 3);
			}
			double points = data.GetUtils().prod_util.GetCommercePoints(commerce, type);
			table.CreateCell(i + 1, 4).SetText(String.format("%.1f", points)).SetTooltipText(
				String.format(Texts.tooltipCommercePoint, tooltip_prefix, transfers, data.GetParam().transfer_commerce_point));
			total_points += points;
		}
		table.CreateCell(8, 4).SetText(String.format("%.1f", total_points));
		
		long merchant_num = cmp.utils.city_util.GetProfessionalPopulation(city, Profession.MERCHANT);
		double unit_income = data.GetUtils().prod_util.GetIncomePerMerchant(city, city.GetPopulation().GetProfessionRatio(Profession.MERCHANT));
		merchant.SetNumber(merchant_num);
		income_per_merchant.SetText(StringUtil.Decimal3Digit(unit_income));
		income.SetNumber((long) (merchant_num * unit_income));
		tax.SetNumber((long) (merchant_num * unit_income * cmp.utils.prod_util.GetTaxEfficiency(city)));
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);

		TextWriter text_writer = new TextWriter(g);
		text_writer.SetFontSize(12);
		text_writer.DrawString(5, 240, Texts.estimatedIncome);
		text_writer.DrawString(180, 240, "= " + Texts.professionMerchant);
		text_writer.DrawString(340, 240, "*");
		text_writer.DrawString(5, 265, Texts.estimatedTaxIncome);
	}

	private TableElement table;
	private TextElement income;
	private TextElement tax;
	private TextElement merchant;
	private TextElement income_per_merchant;
}
