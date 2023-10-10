package ac.ui.swing.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import ac.data.MarketData;
import ac.data.base.Resource.ResourceType;
import ac.data.constant.Texts;
import ac.engine.data.Data;
import ac.engine.data.DataAccessor;
import ac.engine.data.Market;
import ac.ui.swing.Components;
import ac.ui.swing.TypedDataPanel;
import ac.ui.swing.util.ShapeDrawer;
import ac.ui.swing.util.TextWriter;
import ac.ui.swing.util.TextWriter.Alignment;

public class MarketPanel extends TypedDataPanel<Data> {

	private static final long serialVersionUID = 1L;

	public MarketPanel(Components cmp, DataAccessor data) {
		super(cmp, data);
		
		food.setBounds(5, 5, 60, 20);
		horse.setBounds(160, 5, 60, 20);
		iron.setBounds(315, 5, 60, 20);
		
		ButtonGroup rb_group = new ButtonGroup();
		rb_group.add(food);
		rb_group.add(horse);
		rb_group.add(iron);
		
		add(food);
		add(horse);
		add(iron);
		
		for (int i = 0; i < size; ++i) {
			x[i] = i * grid + x_offset;
		}
		market = data.GetMarket();
	}

	@Override
	public void Reset(Data input) {
	}
	
	public void paintComponent(Graphics g)  {
		super.paintComponent(g);
		ResourceType type = ResourceType.FOOD;
		if (horse.isSelected()) type = ResourceType.HORSE;
		else if (iron.isSelected()) type = ResourceType.IRON;
		ShapeDrawer shape_drawer = new ShapeDrawer(g);
		TextWriter text_writer = new TextWriter(g);
		shape_drawer.DrawTable(new Rectangle(x_offset, y_offset, grid * (size - 1), 20), 10, 1);
		text_writer.SetFontSize(10);
		text_writer.SetAlignment(Alignment.CENTER);
		text_writer.DrawString(x_offset, y_offset + 215, data.GetDate().CreateDate(-size * (365 * 3 + 366) / 48).YearString());
		text_writer.DrawString(x_offset+ grid * size, y_offset + 215, data.GetDate().YearString());

		double highest_price = 0;
		long highest_amount = 0L;
		for (int i = 0; i < size; ++i) {
			prices[i] = market.GetHistoricalPrice(type, i);
			highest_price = Math.max(highest_price, prices[i]);
			amounts[i] = market.GetHistoricalAmount(type, i);
			highest_amount = Math.max(highest_amount, amounts[i]);
		}
		highest_price = Math.ceil(highest_price * 1.2);
		highest_amount = (long) (Math.ceil((double)highest_amount * 6 / 5000) * 1000);
		if (highest_amount <= 0) {
			highest_amount = 10;
		}
		for (int i = 0; i < size; ++i) {
			quantized_prices[i] = y_offset + 200 - (int) Math.round(prices[i] / highest_price * 200);
			quantized_amounts[i] = y_offset + 200 - (int) Math.round((double)amounts[i] / highest_amount * 200);
		}
		g.setColor(Color.RED);
		g.drawPolyline(x, quantized_prices, size);
		text_writer.SetAlignment(Alignment.RIGHT_MIDDLE);
		text_writer.SetColor(Color.RED);
		for (int i = 0; i < 10; ++i) {
			text_writer.DrawString(x_offset, y_offset + 20 * i,  String.format("%.1f  ", highest_price / 10 * (10 - i)));
		}
		g.setColor(Color.BLUE);
		g.drawPolyline(x, quantized_amounts, size);
		text_writer.SetAlignment(Alignment.LEFT_MIDDLE);
		text_writer.SetColor(Color.BLUE);
		for (int i = 0; i < 10; ++i) {
			text_writer.DrawString(x_offset + grid * size, y_offset + 20 * i,  String.format("  %,d", highest_amount / 10 * (10 - i)));
		}
	}

	private JRadioButton food = new JRadioButton(Texts.foodIcon, true);
	private JRadioButton horse = new JRadioButton(Texts.horseIcon);
	private JRadioButton iron = new JRadioButton(Texts.ironIcon);
	
	private static int x_offset = 52;
	private static int y_offset = 50;
	private static int grid = 4;
	private static int size = MarketData.kMaxPriceHistory;
	
	private int[] x = new int[size];
	private double[] prices = new double[size];
	private int[] quantized_prices = new int[size];
	private long[] amounts = new long[size];
	private int[] quantized_amounts = new int[size];
	
	private Market market;
}
