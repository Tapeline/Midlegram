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
	
	public static String urlEncode(String sUrl) {
	     StringBuffer urlOK = new StringBuffer();
	     for (int i = 0; i < sUrl.length(); i++) {
	         char ch = sUrl.charAt(i);
	         switch (ch) {
	             case '<': urlOK.append("%3C"); break;
	             case '>': urlOK.append("%3E"); break;
	             case '/': urlOK.append("%2F"); break;
	             case ' ': urlOK.append("%20"); break;
	             case ':': urlOK.append("%3A"); break;
	             case '-': urlOK.append("%2D"); break;
	             default: urlOK.append(ch); break;
	         } 
	     }
	     return urlOK.toString();
	}
	
	public static String toMMSS(int seconds) {
		int minutes = seconds / 60;
		int sec = seconds % 60;
		return "" + minutes + ":" + (sec < 10? "0" + sec : "" + sec);
	}
}
