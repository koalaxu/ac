package ac.util;

public class StringUtil {
	public static String IfNull(String input, String default_value) {
		return input != null && !input.isEmpty() ? input : default_value;
	}
	
	public static String IfNullEmpty(Object input) {
		return input != null ? input.toString() : "";
	}
	
	public static String nOf(String e, int n) {
		String ret = "";
		for (int i = 0; i < n; ++i) {
			ret += e;
		}
		return ret;
	}
	
	public static String LongNumber(long number) {
		return String.format("%,d", number);
	}
	
	public static String Number(int number) {
		return String.format("%,d", number);
	}
	
	public static String Percentage(double ratio) {
		return Percentage((int)Math.round(ratio * 100));
	}
	
	public static String Percentage(int percent) {
		return String.format("%d%%", percent);
	}
	
	public static String AccuratePercentage(double ratio, boolean signed) {
	    return String.format(signed ? "%+.2f%%" : "%.2f%%", ratio * 100);
	}
	
	public static String Decimal3Digit(double number) {
		return String.format("%.3f", (double)Math.round(number * 1000) / 1000);
	}
	
	public static String JoinLongNumber(CharSequence delimeter, long... values) {
		String[] string_values = new String[values.length];
		for (int i = 0; i < values.length; ++i) {
			string_values[i] = LongNumber(values[i]);
		}
		return String.join(delimeter, string_values);
	}
	
	public static String ConvertToHTML(String text) {
		return "<html>" + text.replaceAll("\n", "<br>") + "</html>";
	}
	public static String ConvertToHTML(String... texts) {
		return "<html>" + String.join("<br>", texts) + "</html>";
	}
}
