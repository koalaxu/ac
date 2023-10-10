package ac.data.base;

import ac.data.constant.Texts;

public class Date implements Comparable<Date> {
	public Date(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
		
		if (year > 0) year--;
		year += kYearOffset;
		
		int leaps = year / 4 - year / 100 + year / 400;
		
		value = year * 365 + leaps + kCumulatedDaysOfMonth[month - 1] + day - 1 ;
		if (month > 2 && IsLeapYear()) value++;
		updated = true;
	}
	
	private Date(long value) {
		this.value = value;
		updated = false;
	}
	
	public int GetYear() {
		Update();
		return year;
	}
	
	public int GetMonth() {
		Update();
		return month;
	}
	
	public int GetDay() {
		Update();
		return day;
	}
	
	public void Increment() {
		value++;
		day++;
		if (DayExceeded()) {
			day = 1;
			month++;
			if (month > 12) {
				month = 1;
				year++;
			}
		}
	}
	
	public void IncrementBy(int days) {
		value += days;
		updated = false;
	}
	
	public Date CreateDate(int delta) {
		Update();
		Date new_date = new Date(value);
		new_date.IncrementBy(delta);
		return new_date;
	}
	
	public int GetDifference(Date from) {
		return (int) (this.value - from.value);
	}
	
	public String toString() {
		Update();
		return String.format("%s %4d%s %2d%s %2d%s", year < 0 ? Texts.bc : "　",
				Math.abs(year), Texts.year, month, Texts.month, day, Texts.day);
	}
	
	public String ShortString() {
		Update();
		return String.format("%s%d%s%d%s%d%s", year < 0 ? Texts.bc : "",
				Math.abs(year), Texts.year, month, Texts.month, day, Texts.day);
	}
	
	public String YearString() {
		Update();
		return String.format("%s%d", year < 0 ? Texts.bc : "", Math.abs(year));
	}
	
	public String MonthDayString() {
		Update();
		return String.format("%d%s%d%s", month, Texts.month, day, Texts.day);
	}
	
	public String FormatString() {
		Update();
		return String.format("%d/%d/%d", year, month, day);
	}
	
	public static String YearString(int year) {
		return String.format("%s%d", year < 0 ? Texts.bc : "", Math.abs(year));
	}
	
	private void Update() {
		if (!updated) {
			year = (int) ((value / kFourHundredYear) * 400);
			int reminder = (int) (value % kFourHundredYear);
			year += (reminder / kOneHundredYear) * 100;
			reminder = reminder % kOneHundredYear;
			year += (reminder / kFourYear) * 4;
			reminder = reminder % kFourYear;
			int additional_years = Math.min(reminder / kYear, 3);
			year += additional_years;
			reminder -= additional_years * kYear;
			year -= kYearOffset;
			if (year >= 0) year++;
			int[] cumulated_days = IsLeapYear() ? kCumulatedDaysOfMonthLeaped : kCumulatedDaysOfMonth;
			for (month = 1; month <= 12; month++) {
				if (reminder < cumulated_days[month]) {
					day = reminder + 1 - cumulated_days[month - 1];
					break;
				}
			}
			updated = true;
		}
	}
	
	private boolean DayExceeded() {
		return day > (IsLeapYear() ? kDaysOfMonthLeaped : kDaysOfMonth)[month];
	}
	
	private boolean IsLeapYear() {
		int absolute_year = (year > 0) ? year : - (year + 1);
		return (absolute_year % 4) == 0 && ((absolute_year % 100) != 0 || (absolute_year % 400) == 0);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return value == ((Date)obj).value;
	}
	
	@Override
	public int compareTo(Date o) {
		if (value < o.value) return -1;
		if (value > o.value) return 1;
		return 0;
	}

	private static final int kYearOffset = 800;
	private static final int kFourHundredYear = 365 * 400 + 100 - 3;
	private static final int kOneHundredYear = 365 * 100 + 25 - 1;
	private static final int kFourYear = 365 * 4 + 1;
	private static final int kYear = 365;
	private static final int[] kDaysOfMonth = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private static final int[] kDaysOfMonthLeaped = { 0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private static final int[] kCumulatedDaysOfMonth = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365 };
	private static final int[] kCumulatedDaysOfMonthLeaped = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366 };
	private transient int year;
	private transient int month;
	private transient int day;
	public long value;
	private transient boolean updated = false;
}
