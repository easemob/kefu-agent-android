package com.easemob.helpdesk.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateUtils {
	private static final long INTERVAL_IN_MILLISECONDS = 30 * 1000;

	public static String getDateTimeString(long millonSecond){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
		return dateFormat.format(new Date(millonSecond));
	}

	public static String getStartDateTimeString(long millonSecond){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000'Z'", Locale.getDefault());
		return dateFormat.format(new Date(millonSecond));
	}

	public static String getEndDateTimeString(long millonSecond){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'23:59:59.000'Z'", Locale.getDefault());
		return dateFormat.format(new Date(millonSecond));
	}

	public static TimeInfo getStartAndEndTime(int startDay, int endDay){
		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -startDay);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);

		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.DATE, -endDay);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);

		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	public static String getTimestampWeekForChart(Date messageDate){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("'wk'ww/yy", Locale.getDefault());
		Calendar cal = Calendar.getInstance();
		cal.setTime(messageDate);
		cal.setFirstDayOfWeek(Calendar.SUNDAY); //设置每周的第一天为星期日
		cal.setMinimalDaysInFirstWeek(1); //设置每周最少为1天
		simpleDateFormat.setCalendar(cal);
//
//		int year = cal.get(Calendar.YEAR);
//		int value = cal.get(Calendar.WEEK_OF_YEAR);
		return simpleDateFormat.format(messageDate);
//		return "wk" + value + "/" + year;
	}


	public static String getTimestampMonthForChart(Date messageDate){
		return new SimpleDateFormat("MM/yy", Locale.CHINA).format(messageDate);
	}

	public static String getTimestampStringForChart(Date messageDate){
		String format;
//		long messageTime = messageDate.getTime();
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(messageDate);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if(hour > 0){
			format = "M-d HH";
		}else{
			format = "M-d";
		}
		return new SimpleDateFormat(format, Locale.CHINA).format(messageDate);
	}


	public static String getTimestampString(Date messageDate) {
		String format;
		long messageTime = messageDate.getTime();
		if (isSameDay(messageTime)) {
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(messageDate);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			if (hour > 17) {
				format = "晚上 hh:mm";
			} else if (hour >= 0 && hour <= 6) {
				format = "凌晨 hh:mm";
			} else if (hour > 11 && hour <= 17) {
				format = "下午 hh:mm";
			} else {
				format = "上午 hh:mm";
			}
		} else if (isYesterday(messageTime)) {
			format = "昨天 HH:mm";
		} else {
			format = "M月d日 HH:mm";
		}
		return new SimpleDateFormat(format, Locale.CHINA).format(messageDate);
	}

	public static boolean isCloseEnough(long time1, long time2) {
		// long time1 = date1.getTime();
		// long time2 = date2.getTime();
		long delta = time1 - time2;
		if (delta < 0) {
			delta = -delta;
		}
		return delta < INTERVAL_IN_MILLISECONDS;
	}

	private static boolean isSameDay(long inputTime) {

		TimeInfo tStartAndEndTime = getTodayStartAndEndTime();
		return inputTime > tStartAndEndTime.getStartTime() && inputTime < tStartAndEndTime.getEndTime();
	}

	private static boolean isYesterday(long inputTime) {
		TimeInfo yStartAndEndTime = getYesterdayStartAndEndTime();
		return inputTime > yStartAndEndTime.getStartTime() && inputTime < yStartAndEndTime.getEndTime();
	}

	public static TimeInfo getYesterdayStartAndEndTime() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -1);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);

		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.DATE, -1);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	public static TimeInfo getTodayStartAndEndTime() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S");

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	public static TimeInfo getBeforeYesterdayStartAndEndTime() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -2);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.DATE, -2);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	
	
	public static TimeInfo getTimeInfoByDaysBefore(int numOfDays) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.DAY_OF_MONTH, calendar1.get(Calendar.DAY_OF_MONTH) - numOfDays);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}
	
	public static TimeInfo getTimeInfoByMonthBefore(int numOfMonth) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.MONTH, calendar1.get(Calendar.MONTH)-numOfMonth);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}
	
	public static TimeInfo getTimeInfoByAll() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.YEAR, 2015);
		calendar1.set(Calendar.MONTH, 11);
		calendar1.set(Calendar.HOUR_OF_DAY, 8);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}
	
	
	
	public static TimeInfo getTimeInfoByCurrentWeek() {
		Calendar calendar1 = Calendar.getInstance();
		int dayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);
		calendar1.roll(Calendar.DAY_OF_YEAR, 1 - dayOfWeek);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.roll(Calendar.DAY_OF_YEAR, (7-dayOfWeek));
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		if(startTime > endTime){
			calendar1.roll(Calendar.YEAR, -1);
			startTime = calendar1.getTime().getTime();
		}

		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}
	
	public static TimeInfo getTimeInfoByCurrentMonth() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.DAY_OF_MONTH, 1);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();
		
		Calendar calendar2 = Calendar.getInstance();
		calendar2.roll(Calendar.MONTH, 1);
		calendar2.set(Calendar.DAY_OF_MONTH, 1);
		calendar2.roll(Calendar.DAY_OF_YEAR, -1);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}
	
	public static TimeInfo getTimeInfoByLastMonth() {

		Calendar calendar1 = Calendar.getInstance();
		calendar1.roll(Calendar.MONTH, -1);
		calendar1.set(Calendar.DAY_OF_MONTH, 1);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		if(calendar1.get(Calendar.MONTH) == 11){
			calendar1.roll(Calendar.YEAR, -1);
		}
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();
		
		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(Calendar.DAY_OF_MONTH, 1);
		calendar2.roll(Calendar.DAY_OF_YEAR, -1);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		if(calendar2.get(Calendar.MONTH) == 11){
			calendar2.roll(Calendar.YEAR, -1);
		}
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}


	public static String convertFromSecond(int second) {
		int h = 0;
		int d = 0;
		int s = 0;
		int temp = second % 3600;
		if (second > 3600) {
			h = second / 3600;
			if (temp != 0) {
				if (temp > 60) {
					d = temp / 60;
					if (temp % 60 != 0) {
						s = temp % 60;
					}
				} else {
					s = temp;
				}
			}
		} else {
			d = second / 60;
			if (second % 60 != 0) {
				s = second % 60;
			}
		}
		if (h > 0){
			return h + "小时" + d + "分" + s + "秒";
		}else if (d > 0){
			return d + "分" + s + "秒";
		}else if (s > 0){
			return s + "秒";
		}else{
			return second + "秒";
		}
	}

}
 