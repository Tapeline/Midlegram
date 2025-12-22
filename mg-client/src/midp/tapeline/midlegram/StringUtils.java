package midp.tapeline.midlegram;

import java.util.Calendar;
import java.util.Date;

public class StringUtils {
	
	public static final boolean contains(String needle, String haystack) {
		return haystack.indexOf(needle) != -1;
	}

	public static final String trunc(String str, int limit) {
		if (str.length() <= limit - 3) return str;
		return str.substring(0, limit - 3) + "...";
	}
	
	public static final String dateToString(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return "" + cal.get(Calendar.DAY_OF_MONTH) + "." + cal.get(Calendar.MONTH) + "." + (cal.get(Calendar.YEAR)%100)
				+ " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
	}
	
}
