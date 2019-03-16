package com.mossle.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.util.Assert;

public class DateUtil {

	public static final String PATTERN_STANDARD = "yyyy-MM-dd HH:mm:ss";

	public static final String PATTERN_DATE = "yyyy-MM-dd";

	public static final String FORMAT_SECONDS = "yyyy-MM-dd HH:mm:ss";

	public static final String FORMAT_DAY = "yyyy-MM-dd";

	public static final long ONE_DAY_MILLISECOND = 24 * 60 * 60 * 1000;

	/**
	 * 获取指定日期的上一年月或下一年月
	 * @param dateStr
	 * @param addYear
	 * @param addMonth
	 * @param addDate
	 * @return
	 * @throws Exception
	 */
	public static String getLastMonth(String dateStr,int addYear, int addMonth, int addDate) throws Exception {
		try {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM");
			java.util.Date sourceDate = sdf.parse(dateStr);
			Calendar cal = Calendar.getInstance();
			cal.setTime(sourceDate);
			cal.add(Calendar.YEAR,addYear);
			cal.add(Calendar.MONTH, addMonth);
			cal.add(Calendar.DATE, addDate);
			java.text.SimpleDateFormat returnSdf = new java.text.SimpleDateFormat("yyyy-MM");
			String dateTmp = returnSdf.format(cal.getTime());
			java.util.Date returnDate = returnSdf.parse(dateTmp);
			return dateTmp;
		} catch (Exception e) {
		e.printStackTrace();
		throw new Exception(e.getMessage());
		} 
	}	
	public static String getMonthFrist(){
		 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd"); 
		         
	      //获取前月的第一天
	      Calendar   cal_1=Calendar.getInstance();//获取当前日期 
	      cal_1.add(Calendar.MONTH, 0);
	      cal_1.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天 
	      String format2 = format.format(cal_1.getTime());
	      return format2+" 00:00:00";
	    //获取前月的最后一天
	    /* Calendar cale = Calendar.getInstance();   
	      cale.set(Calendar.DAY_OF_MONTH,0);//设置为1号,当前日期既为本月第一天 
	      lastDay = format.format(cale.getTime());
	     System.out.println("-----2------lastDay:"+lastDay);*/
	
	}
	
	
	/**
	 * 
	 * @Function: DateUtil.java
	 * @Description: 该函数的功能描述
	 *		比较两个日期   time1  早于 time2   返回true
	 * @param time1
	 * @param time2
	 * @return
	 * @throws ParseException
	 *
	 * @version: v1.0.0
	 * @author: ckx
	 * @date: 
	 *
	 */
	public static boolean compare(String time1,String time2) throws ParseException{
		//如果想比较日期则写成"yyyy-MM-dd"就可以了
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm");
		//将字符串形式的时间转化为Date类型的时间
		Date a=sdf.parse(time1);
		Date b=sdf.parse(time2);
		//Date类的一个方法，如果a早于b返回true，否则返回false
		if(a.before(b))
			return true;
		else
			return false;
		/*
		 * 如果你不喜欢用上面这个太流氓的方法，也可以根据将Date转换成毫秒
		if(a.getTime()-b.getTime()<0)
			return true;
		else
			return false;
		*/
	}
	
	
    /*** 
     * @comments 计算两个时间的时间差 ，返回的是相差秒数
     * @param strTime1 
     * @param strTime2 
     * @author sjx
     */  
	public static long getTimeDifference(String strTime1,String strTime2) {
        //格式日期格式，在此我用的是"2018-01-24 19:49:50"这种格式  
        //可以更改为自己使用的格式，例如：yyyy/MM/dd HH:mm:ss 。。。 
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long seconds = 0;
           try{  
               Date now = df.parse(strTime1);  
               Date date=df.parse(strTime2);  
               long l=now.getTime()-date.getTime();
               seconds=(l/1000);  
           }catch(Exception e){  
               e.printStackTrace();  
           } 
           return seconds;
    } 
	
	/**
	 * @comments 通过秒数返回    00:00:00 时分秒
	 * @param seconds
	 * @return
	 * @author sjx 18.01.10
	 */
	public static String secondsToTime(long seconds) {
		String result = "";
		long hours = seconds / 3600;//转换小时
		seconds = seconds % 3600;//剩余秒数
		long minutes = seconds / 60;//转换分钟数
		seconds = seconds % 60;//剩余秒数
		if (hours > 0){
			result = hours+":"+minutes+":"+seconds;
			String[] split = result.split(":");
			result = "";
			for(String str : split){
				if(str.length() == 1){
					result += "0"+str+":";
				}else{
					result += str+":";
				}
			}
			result = result.substring(0, result.length()-1);
			//System.out.println(result);
		}else {
			result = "00:"+minutes+":"+seconds;
			String[] split = result.split(":");
			result = "";
			for(String str : split){
				if(str.length() == 1){
					result += "0"+str+":";
				}else{
					result += str+":";
				}
			}
			result = result.substring(0, result.length()-1);
			//System.out.println(result);
		}
		return result;
	}

	public static void main(String[] args) {
		long timeDifference = getTimeDifference("2019-01-12 09:00:00","2019-01-11 21:00:59");
		System.out.println(timeDifference);
		//secondsToTime(-1);
	}
	
	
	public static Date parseDate(String s) {
		DateFormat df = DateFormat.getDateInstance();
		Date myDate = null;
		try {
			myDate = df.parse(s);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return myDate;
	}

	/**
	 * 获取指定日期的当前星期（中国, 如：星期日,星期一,星期二）
	 * 
	 * @param time
	 *            要格式化的时间，如果为空则取当前日期
	 */
	public static String getWeekCS(String sDate) {
		Calendar c = Calendar.getInstance();
		if (!(sDate == null || sDate.equals(""))) {
			Date date = formatDateStr(sDate, "yyyy-MM-dd");
			c.setTime(date);
		}
		c.setFirstDayOfWeek(Calendar.SUNDAY);
		String[] s = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		return s[c.get(Calendar.DAY_OF_WEEK) - 1];
	}

	/**
	 * 对传入的时间进行格式化
	 * 
	 * @param time
	 *            要格式化的时间，如果为null则取当前时间
	 * @param format
	 *            返回值的形式，为空则按"yyyy-MM-dd HH:mm:ss"格式化时间
	 * @return 格式化后的时间字串
	 */
	public static String formatDate(Date time, String format) {
		if (time == null) {
			time = new Date();
		}
		// 获取当前time
		if (format == null || format.equals("")) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		String dateString = formatter.format(time);
		return dateString;
	}

	/**
	 * 对传入的时间字串进行格式化，并返回一个Date对象
	 * 
	 * @param time
	 *            要格式化的时间，如果为空则取当前时间
	 * @param format
	 *            返回值的形式，为空则按"yyyy-MM-dd HH:mm:ss"格式化时间
	 * @return 格式化后的时间
	 */
	public static Date formatDateStr(String time, String format) {
		if (time == null || time.equals("")) {
			// 获取当前time
			time = formatDate(null, null);
		}
		if (format == null || format.equals("")) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		ParsePosition pos = new ParsePosition(0);// 从0开始解析
		Date strtodate = formatter.parse(time, pos);
		return strtodate;
	}

	/**
	 * 对传入的时间字串进行格式化
	 * 
	 * @param time
	 *            要格式化的时间，如果为空则取当前时间
	 * @param format
	 *            返回值的形式，为空则按"yyyy-MM-dd HH:mm:ss"格式化时间
	 * @return 格式化后的时间字串
	 */
	public static String formatDateStrToStr(String time, String format) {
		return formatDate(formatDateStr(time, format), format);
	}

	/**
	 * 获取指定日期的中国日期（yyyy年MM月dd日）
	 * 
	 * @param time
	 *            要格式化的时间，如果为null则取当前时间
	 * @return 格式化后的日期字串
	 */
	public static String getDateCS(Date time) {
		return formatDate(time, "yyyy年MM月dd日");
	}

	/**
	 * 获取指定时间的长字符串形式 "yyyy-MM-dd HH:mm:ss"
	 * 
	 * @param time
	 *            要格式化的时间，如果为null则取当前时间
	 * @return 格式化后的时间字串
	 */
	public static String getLongStr(Date time) {
		return formatDate(time, null);
	}
	
	/** 
	   * 得到几天前的时间 
	   * @param d 
	   * @param day 
	   * @return 
	   */  
	  public static String getStrBefore(Date d,int day){  
	   Calendar now =Calendar.getInstance();  
	   now.setTime(d);  
	   now.set(Calendar.DATE,now.get(Calendar.DATE)-day);  
	   return formatDate(now.getTime(),null);  
	  } 
	

	/**
	 * 获得d天后的现在时刻；长字符串形式 "yyyy-MM-dd HH:mm:ss"
	 * 
	 * @param d
	 *            //d天后
	 * @return 格式化后的时间字串
	 */
	public static String getLongStr(int d) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, d);// 获得d天后的现在时刻
		Date time = c.getTime();
		return formatDate(time, null);
	}
	/** 
	   * 得到几天前的时间 
	   * @param d 
	   * @param day 
	   * @return 
	   */  
	  public static Date getDateBefore(Date d,int day){  
	   Calendar now =Calendar.getInstance();  
	   now.setTime(d);  
	   now.set(Calendar.DATE,now.get(Calendar.DATE)-day);  
	   return now.getTime();  
	  } 
	

	/**
	 * 获得d天后的现在时刻
	 * 
	 * @param d
	 *            //d天后
	 * @return 格式化后的时间字串
	 */
	public static Date getAfterDate(int d) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, d);// 获得d天后的现在时刻
		Date time = c.getTime();
		return time;
	}
	
	 

	/**
	 * 获取指定时间的短字符串形式 "yyyy-MM-dd"
	 * 
	 * @param time
	 *            要格式化的时间，如果为null则取当前时间
	 * @return 格式化后的时间字串
	 */
	public static String getShortStr(Date time) {
		return formatDate(time, "yyyy-MM-dd");
	}

	/**
	 * 获取指定时间的短字符串形式
	 * 
	 * @param time
	 *            要格式化的时间，如果为空则取当前时间
	 * @param format
	 *            返回值的形式，为空则按"yyyyMMdd"格式化时间
	 * @return 格式化后的时间字串
	 */
	public static String getDateStr(Date time, String format) {
		if (time == null) {
			time = new Date();
		}
		if (format == null || format.equals("")) {
			format = "yyyyMMdd";
		}
		return formatDate(time, format);
	}

	/**
	 * 将字符串转换为一般时间的长格式:yyyy-MM-dd HH:mm:ss
	 * 
	 * @param strDate
	 *            要格式化的时间，如果为空则取当前时间
	 * @return 格式化后的时间
	 */
	public static Date getLongDate(String strDate) {
		return formatDateStr(strDate, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 将字符串转换为一般时间的短格式;yyyy-MM-dd
	 * 
	 * @param strDate
	 *            要格式化的时间，如果为空则取当前时间
	 * @return 格式化后的时间
	 */
	public static Date getShortDate(String strDate) {
		return formatDateStr(strDate, "yyyy-MM-dd");
	}

	/**
	 * 将字符串转换为一般时间的短格式;yyyy-MM-dd
	 * 
	 * @param strDate
	 *            要格式化的时间，如果为空则取当前时间
	 * @return 格式化后的时间
	 */
	public static Date getDate(String strDate, String format) {
		return formatDateStr(strDate, format);
	}

	/**
	 * 获取时间 小时:分;秒 HH:mm:ss
	 * 
	 * @param strDate
	 *            要格式化的时间，如果为空则取当前时间
	 * @return 格式化后的时间
	 */
	public static String getStrTimeShort(String strDate) {
		return formatDateStrToStr(strDate, "HH:mm:ss");
	}

	/**
	 * 将日期字符串加天数转换成新日期字符串
	 * 
	 * @param strDate
	 *            原日期字符串:
	 * @param days
	 *            增加的天数
	 * @return (yyyy-M-d)添加后的时间
	 */
	public static String addDate(String strDate, int days) {
		String tmpDate = formatDateStrToStr(strDate, "yyyy-MM-dd");
		String[] date = tmpDate.split("-"); // 将要转换的日期字符串拆分成年月日
		int year, month, day;
		year = Integer.parseInt(date[0]);
		month = Integer.parseInt(date[1]) - 1;
		day = Integer.parseInt(date[2]);
		GregorianCalendar d = new GregorianCalendar(year, month, day);
		d.add(Calendar.DATE, days);
		Date dd = d.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String adddate = df.format(dd);
		return adddate;
	}

	/**
	 * //获取当前的时间
	 */
	public static Date getNow() {
		Date currentTime = new Date();
		return currentTime;
	}

	/**
	 * //获取昨日日期
	 */
	public static String getYesterdayStr() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return getDateStr(cal.getTime(), FORMAT_DAY);
	}

	/**
	 * //获取指定日期的下一日
	 */
	public static Date getNextDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.getTime() + ONE_DAY_MILLISECOND);
		return cal.getTime();
	}

	/**
	 * 用当前日期作为文件名,一般不会重名 取到的值是从当前时间的字符串格式,带有毫秒,建议作为记录id 。一秒是一千毫秒。
	 * 如：20080403212345508
	 */
	public static String getDateId() {
		return formatDate(null, "yyyyMMddHHmmssSSS");
	}

	/**
	 * 用当前日期作为文件名,一般不会重名 取到的值是从当前时间的字符串格式。 如：20080403212345508
	 */
	public static String getDateDir() {
		return formatDate(null, "yyyyMMddHHmmss");
	}

	/**
	 * 提取一个月中的最后一天
	 * 
	 * @param day
	 * @return
	 */
	public static Date getLastDate(long day) {
		Date date = new Date();
		long date_3_hm = date.getTime() - 3600000 * 34 * day;
		Date date_3_hm_date = new Date(date_3_hm);
		return date_3_hm_date;
	}

	/**
	 * 得到现在时间
	 * 
	 * @return 字符串 yyyy-MM-dd
	 */
	public static String getStringToday() {
		return formatDate(null, "yyyy-MM-dd");
	}

	/**
	 * 得到现在小时
	 */
	public static String getHour() {
		String dateString = formatDate(null, "yyyy-MM-dd HH:mm:ss");
		String hour = dateString.substring(11, 13);
		return hour;
	}

	/**
	 * 得到现在分钟
	 * 
	 * @return
	 */
	public static String getMinute() {
		String dateString = formatDate(null, "yyyy-MM-dd HH:mm:ss");
		String min = dateString.substring(14, 16);
		return min;
	}

	/**
	 * 得到现在分秒
	 * 
	 * @return
	 */
	public static String getMinMouse() {
		String dateString = formatDate(null, "yyyyMMddHHmmss");
		String minmouse = dateString.substring(10, 14);
		return minmouse;
	}

	/**
	 * 得到现在时分秒
	 * 
	 * @return
	 */
	public static String getHourMinMouse() {
		String dateString = formatDate(null, "yyyyMMddHHmmss");
		String minmouse = dateString.substring(8, 14);
		return minmouse;
	}

	/**
	 * 二个小时时间间的差值,必须保证二个时间都是"HH:mm"的格式，返回字符型的分钟
	 */
	public static String getTwoHour(String st1, String st2) {
		String[] kk = null;
		String[] jj = null;
		kk = st1.split(":");
		jj = st2.split(":");
		if (Integer.parseInt(kk[0]) < Integer.parseInt(jj[0])) {
			return "0";
		} else {
			double y = Double.parseDouble(kk[0]) + Double.parseDouble(kk[1]) / 60;
			double u = Double.parseDouble(jj[0]) + Double.parseDouble(jj[1]) / 60;
			if ((y - u) > 0) {
				return y - u + "";
			} else {
				return "0";
			}
		}
	}

	/**
	 * 得到二个日期间的间隔天数
	 */
	public static String getTwoDay(String sj1, String sj2) {
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		long day = 0;
		try {
			java.util.Date date = myFormatter.parse(sj1);
			java.util.Date mydate = myFormatter.parse(sj2);
			day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		} catch (Exception e) {
			return "";
		}
		return day + "";
	}

	/**
	 * 时间前推或后推分钟,其中JJ表示分钟.
	 * 
	 * @param sj1
	 *            指定时间
	 * @param jj
	 *            前推或后推分钟,其中JJ表示分钟
	 * @return
	 */
	public static String getPreTime(String sj1, String jj) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String mydate1 = "";
		try {
			Date date1 = format.parse(sj1);
			long Time = (date1.getTime() / 1000) + Integer.parseInt(jj) * 60;
			date1.setTime(Time * 1000);
			mydate1 = format.format(date1);
		} catch (Exception e) {
		}
		return mydate1;
	}

	/**
	 * 判断是否润年
	 * 
	 * @param ddate
	 * @return
	 */
	public static boolean isLeapYear(String ddate) {
		/**
		 * 详细设计： 1.被400整除是闰年，否则： 2.不能被4整除则不是闰年 3.能被4整除同时不能被100整除则是闰年
		 * 3.能被4整除同时能被100整除则不是闰年
		 */
		Date d = formatDateStr(ddate, "yyyy-MM-dd");
		GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
		gc.setTime(d);
		int year = gc.get(Calendar.YEAR);
		if ((year % 400) == 0) {
			return true;
		} else if ((year % 4) == 0) {
			if ((year % 100) == 0) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * 获取一个月的最后一天
	 * 
	 * @param date
	 * @return
	 */
	public static String getEndDateOfMonth(String date) {
		String str = formatDateStrToStr(date, "yyyy-MM-dd");
		str = date.substring(0, 8);
		String month = date.substring(5, 7);
		int mon = Integer.parseInt(month);
		if (mon == 1 || mon == 3 || mon == 5 || mon == 7 || mon == 8 || mon == 10 || mon == 12) {
			str += "31";
		} else if (mon == 4 || mon == 6 || mon == 9 || mon == 11) {
			str += "30";
		} else {
			if (isLeapYear(date)) {
				str += "29";
			} else {
				str += "28";
			}
		}
		return str;
	}

	/**
	 * 判断二个时间是否在同一个周
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameWeekDates(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
		if (0 == subYear) {
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
				return true;
			}
		} else if (1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
			// 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
				return true;
			}
		} else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 产生周序列,即得到当前时间所在的年度是第几周
	 * 
	 * @return
	 */
	public static String getYearWeek() {
		Calendar c = Calendar.getInstance();
		// Calendar c = Calendar.getInstance(Locale.CHINA);
		String week = Integer.toString(c.get(Calendar.WEEK_OF_YEAR));
		if (week.length() == 1) {
			week = "0" + week;
		}
		String year = Integer.toString(c.get(Calendar.YEAR));
		return year + week;
	}

	/**
	 * 获得一个日期所在的周的星期几的日期，如要找出2002年2月3日所在周的星期一是几号
	 * 
	 * @param sdate
	 * @param num
	 *            0-6，分别表示周一到周六
	 * @return
	 */
	public static String getWeek(String sdate, String num) {
		// 再转换为时间
		Date dd = formatDateStr(sdate, "yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(dd);
		if (num.equals("1")) // 返回星期一所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		} else if (num.equals("2")) // 返回星期二所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
		} else if (num.equals("3")) // 返回星期三所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		} else if (num.equals("4")) // 返回星期四所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
		} else if (num.equals("5")) // 返回星期五所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		} else if (num.equals("6")) // 返回星期六所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		} else if (num.equals("0")) // 返回星期日所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		}
		return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
	}

	/*public static void main(String[] args) throws Exception {
		String weekCS = getWeekCS("2018-07-31");
		System.out.println(weekCS);
		
		String time = getTime("2018/7/5 8:56");
		System.out.println(time);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date parse = simpleDateFormat.parse("2018/7/5 8:56");
		String formatDate = formatDate(parse, "yyyy-MM-dd");
		System.out.println(formatDate);
		String formatTime = formatTime("2018/7/5 8:56");
		System.out.println(formatTime);
		
		Calendar calendar = Calendar.getInstance();
		int i = calendar.get(Calendar.MONTH);
		
		System.out.println(i);
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
		//将字符串形式的时间转化为Date类型的时间
		Date a=sdf.parse("2017-07");
		Date b=sdf.parse("2018-08");
		//Date类的一个方法，如果a早于b返回true，否则返回false
		if(a.before(b) || a.equals(b))
			System.out.println("true");
		else
			System.out.println("false");
		
		String str = "120";  
		String newStr = str.replaceFirst("^0*", "");  
		System.out.println(newStr);  
		
		boolean comparTime = DateUtil.comparTime("18:30", "18:30");
		System.out.println(comparTime);
		
		Date quitDate = DateUtil.formatDateStr("2018-08-09 18:00:00", "");
		System.out.println(quitDate);
		
		String orderInfoId = "10180926141240";
		
		String phoneNum = "13581704123";
		String md5Key = "qwertyuiop789456";
    	String desKey = "qwertyuiopasdfghjk789456";
    	
		HttpRequester http = new HttpRequester();
    	String urlString = "http://api.dwcx-tech.com/mall-api/api/rolmex_order_all_Detail.json";
    	HashMap<String, Object> md5Map = new HashMap<String, Object>();
    	md5Map.put("orderIds", orderInfoId);
    	md5Map.put("mobile", phoneNum);
    	String sign = com.mossle.ws.online.MD5Util.getMd5(md5Map, md5Key);
    	System.out.println(sign);
    	HashMap<String, String> params = new HashMap<String, String>();
    	params.put("mobile", phoneNum);
    	params.put("orderIds", orderInfoId);
    	
    	params.put("str_sign", sign);
    	String encryptMode = LogisticsUtil.encryptThreeDESECB(JSONObject.toJSONString(params), desKey);
    	System.out.println("====////"+encryptMode);
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("args", encryptMode);
    	HttpRespons sendPost = http.sendPost(urlString, map);
    	
    	String content = sendPost.getContent();
    	System.out.println(content);
    	JSONObject parseObject = JSONObject.parseObject(content);
    	String object = parseObject.get("args").toString();
    	object = URLDecoder.decode(object,"utf-8");
    	System.out.println(object.length());
    	String decryptMode = LogisticsUtil.decryptThreeDESECB(object, desKey);
    	System.out.println(decryptMode);
    	List<Map> resultList = JSONObject.parseArray(decryptMode, Map.class);
    	ArrayList<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
		for (Map map2 : resultList) {
			HashMap<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("proNo", map2.get("c_no"));
			dataMap.put("shopPVNum", map2.get("goods_count"));
			dataMap.put("proName", map2.get("name"));
			dataMap.put("proPrice", map2.get("price"));
			dataMap.put("proPV", map2.get("pv"));
			
			dataMap.put("shopName", "0");
			dataMap.put("shopTel", "0");
			dataMap.put("shopRewardNum", "0");
			dataMap.put("shopRewardPV", "0");
			dataMap.put("shopWalletNum", "0");
			dataMap.put("shopWalletPV", "0");
			dataList.add(dataMap);
		}
    	
    	
    	HttpRequester http1 = new HttpRequester();
    	String url = "http://api.dwcx-tech.com/mall-api/api/rolmex_product_list.json";
    	HttpRespons sendPost1 = http1.sendPost(url);
    	String content1 = sendPost1.getContent();
    	JSONObject parseObject = JSONObject.parseObject(content1);
    	String object = parseObject.get("args").toString();
    	object = URLDecoder.decode(object,"utf-8");
    	System.out.println(object.length());
    	String decryptMode1 = LogisticsUtil.decryptThreeDESECB(object, desKey);
    	System.out.println(decryptMode1);
    	
    	List<Map> resultList = JSONObject.parseArray(decryptMode1, Map.class);
    	ArrayList<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
		for (Map map2 : resultList) {
			HashMap<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("varStoCode", map2.get("c_no"));
			dataMap.put("nvrStoName", map2.get("name"));
			dataList.add(dataMap);
		}
    	
    	Calendar c = Calendar.getInstance();
		c.set(2018, 5, 1);// year年的3月1日
		c.add(Calendar.DAY_OF_MONTH, -1);//将3月1日往左偏移一天结果是2月的天数
		System.out.println(c.get(Calendar.DAY_OF_MONTH)); 
    	
    	String s = "1,2,3, , , ";
    	
    	String[] split = s.split(",");
    	System.out.println(split.length);
    	System.out.println(split[0]);
    	System.out.println(split[1]);
    	System.out.println(split[2]);
    	
    	for (String string : split) {
			System.out.println(string);
		}
    	
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append("1"+",");
    	sb.append("2"+",");
    	
    	System.out.println(sb.toString());
    	
    	String decode = DeEnCode.decode("SPZ[ZSSQUQPSSTV[");
    	
    	System.out.println(decode);
    	
        String ml =
                "CiAgICAgICAgICAgICAgICAgICBfb29Pb29fCiAgICAgICAgICAgICAgICAgIG84ODg4ODg4bwogICAgICAgICAgICAgICAgICA4OCIgLiAiODgKICAgICAgICAgICAgICAgICAgKHwgLV8tIHwpCiAgICAgICAgICAgICAgICAgIE9cICA9ICAvTwogICAgICAgICAgICAgICBfX19fL2AtLS0nXF9fX18KICAgICAgICAgICAgIC4nICBcXHwgICAgIHwvLyAgYC4KICAgICAgICAgICAgLyAgXFx8fHwgIDogIHx8fC8vICBcCiAgICAgICAgICAgLyAgX3x8fHx8IC06LSB8fHx8fC0gIFwKICAgICAgICAgICB8ICAgfCBcXFwgIC0gIC8vLyB8ICAgfAogICAgICAgICAgIHwgXF98ICAnJ1wtLS0vJycgIHwgICB8CiAgICAgICAgICAgXCAgLi1cX18gIGAtYCAgX19fLy0uIC8KICAgICAgICAgX19fYC4gLicgIC8tLS4tLVwgIGAuIC4gX18KICAgICAgLiIiICc8ICBgLl9fX1xfPHw+Xy9fX18uJyAgPiciIi4KICAgICB8IHwgOiAgYC0gXGAuO2BcIF8gL2A7LmAvIC0gYCA6IHwgfAogICAgIFwgIFwgYC0uICAgXF8gX19cIC9fXyBfLyAgIC4tYCAvICAvCj09PT09PWAtLl9fX19gLS5fX19cX19fX18vX19fLi1gX19fXy4tJz09PT09PQogICAgICAgICAgICAgICAgICAgYD0tLS09JwpeXl5eXl5eXl5eXl5eXl5eXl5eXl5eXl5eXl5eXl5eXl5eXl5eXl5eXl5eXl4KICAgICAgICAgICAgICAgICDkvZvnpZbkv53kvZEgICAgICAg5rC45pegQlVHCiAgIOS9m+absDoKICAgICAgIOWGmeWtl+alvOmHjOWGmeWtl+mXtO+8jOWGmeWtl+mXtOmHjOeoi+W6j+WRmO+8mwogICAgICDnqIvluo/kurrlkZjlhpnnqIvluo/vvIzlj4jmi7/nqIvluo/mjaLphZLpkrHjgIIKICAgICAg6YWS6YaS5Y+q5Zyo572R5LiK5Z2Q77yM6YWS6YaJ6L+Y5p2l572R5LiL55yg77ybCiAgICAgIOmFkumGiemFkumGkuaXpeWkjeaXpe+8jOe9keS4iue9keS4i+W5tOWkjeW5tOOAggogICAgICDkvYbmhL/ogIHmrbvnlLXohJHpl7TvvIzkuI3mhL/pnqDouqzogIHmnb/liY3vvJsKICAgICAg5aWU6amw5a6d6ams6LS16ICF6Laj77yM5YWs5Lqk6Ieq6KGM56iL5bqP5ZGY44CCCiAgICAgIOWIq+S6uueskeaIkeW/kueWr+eZq++8jOaIkeeskeiHquW3seWRveWkqui0se+8mwogICAgICDkuI3op4Hmu6HooZfmvILkuq7lprnvvIzlk6rkuKrlvZLlvpfnqIvluo/lkZjvvJ8=";
        byte[] decodeBase64 = Base64.decodeBase64(ml.getBytes());
        System.out.println("\n" + new String(decodeBase64));
    	int intYear=Integer.parseInt("2018");
 	    int intMonth=Integer.parseInt("10");
 	    Calendar calSearch=Calendar.getInstance();
 	    //下个月的第一天
 	   //Date quitDate1 = DateUtil.formatDateStr("2018-11-21"+" 18:00:00", "");
 	    calSearch.set(intYear, intMonth, 1);
 	  //calSearch.setTime(quitDate1);
    	Date quitDate = DateUtil.formatDateStr("2018-10-03"+" 18:00:00", "");
			//Date quitDate = DateUtil.formatDateStr(leaveDate+" 18:00:00", "");
			//Date quitDate=personInfo.getQuitTime();
			//获取离职日期
			//quitDays = formatDate.substring(9, 10);
			Calendar calQuit=Calendar.getInstance();
				calQuit.setTime(quitDate);
				int intQuitYear=calQuit.get(Calendar.YEAR);
				int intQuitMonth=calQuit.get(Calendar.MONTH)+1;
				if(intQuitMonth>11){
					intQuitYear+=1;
					intQuitMonth=0;
				}
				System.out.println(intQuitYear);
				System.out.println(intQuitMonth);
				calQuit.set(intQuitYear,intQuitMonth,1);
			
			//如果查询时间大于离职时间
			if(calSearch.after(calQuit)){
				continue;
			}
			System.out.println(calSearch.after(calQuit));
    	String formatDate = "2018-11-22";
    	String quitMonth = formatDate.substring(8, 10);
    	System.out.println(quitMonth);
    	
    	String lastMonth = getLastMonth("2018-12", 0, 1, 0);
    	
    	System.out.println(lastMonth);
    	
    	
    	int intYear=Integer.parseInt("2018");
 	    int intMonth=Integer.parseInt("10");
 	    Calendar calSearch=Calendar.getInstance();
 	    //下个月的第一天
 	    calSearch.set(intYear, intMonth, 1);
		Date quitDate = personInfo.getLeaveDate();
		String formatDate = DateUtil.formatDate(quitDate, "");
		formatDate = formatDate.substring(0, 10);
		Date quitDate = DateUtil.formatDateStr("2017-11-02"+" 18:00:00", "");
		Calendar calQuit=Calendar.getInstance();
		calQuit.setTime(quitDate);
		int intQuitYear=calQuit.get(Calendar.YEAR);
		int intQuitMonth=calQuit.get(Calendar.MONTH)+1;
		if(intQuitMonth>11){
			intQuitYear+=1;
			intQuitMonth=0;
		}
		calQuit.set(intQuitYear,intQuitMonth,1);
		
		//如果查询时间大于离职时间
		if(calSearch.after(calQuit)){
			System.out.println("true");
		}else{
			System.out.println("false");
		}
    	
    	ArrayList<String> list = null;
		if(list == null){
			System.out.println("sss");
		}
		
    	
    	
	}*/
    public static String getAttendance(String weekCS){
    	String c = "";
    	if("星期一".equals(weekCS)){
    		c = "mondayShiftID";
    	}else if("星期二".equals(weekCS)){
    		c = "tuesdayShiftID";
    	}else if("星期三".equals(weekCS)){
    		c = "wednesdayShiftID";
    	}else if("星期四".equals(weekCS)){
    		c = "thursdayShiftID";
    	}else if("星期五".equals(weekCS)){
    		c = "fridayShiftID";
    	}else if("星期六".equals(weekCS)){
    		c = "SaturdayShiftID";
    	}else if("星期日".equals(weekCS)){
    		c = "SundayShiftID";
    	}
    	return c;
    }
	
	
	/**
	 * 根据年月获取当月有多少天
	 * @param year
	 * @param month
	 * @return
	 */
	public static int getMonthDays(String year,String month){
		
		Calendar c = Calendar.getInstance();
		c.set(Integer.parseInt(year), Integer.parseInt(month), 1);// year年的3月1日
		c.add(Calendar.DAY_OF_MONTH, -1);//将3月1日往左偏移一天结果是2月的天数
		return c.get(Calendar.DAY_OF_MONTH); 
	}
	
	
	public static boolean comparDate(String time1,String time2) throws ParseException{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		//将字符串形式的时间转化为Date类型的时间
		Date a=sdf.parse(time1);
		Date b=sdf.parse(time2);
		//Date类的一个方法，如果a早于b返回true，否则返回false
		if(a.before(b)  || a.equals(b))
			return true;
		else
			return false;
	}
	
	public static boolean comparDate(Date a,Date b){
		//Date类的一个方法，如果a早于b返回true，否则返回false
		if(a.before(b)  || a.equals(b))
			return true;
		else
			return false;
	}
	
	
	public static String formatTime(String time) throws ParseException{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date parse = simpleDateFormat.parse(time);
		String formatDate = formatDate(parse, "yyyy-MM-dd");
		
		return formatDate;
	}
	
	
	public static boolean comparTime(String time1,String time2) throws ParseException{
		SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
		//将字符串形式的时间转化为Date类型的时间
		Date a=sdf.parse(time1);
		Date b=sdf.parse(time2);
		//Date类的一个方法，如果a早于b返回true，否则返回false
		if(a.before(b))
			return true;
		else
			return false;
	}
	
	
	
	public static String getTime(String s) throws ParseException{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date d=formatter.parse(s);
		SimpleDateFormat format=new SimpleDateFormat("HH:mm");
		String date = format.format(d);
		return date;
	}
	
	
	/**
	 * 日期字符串加i天
	 */
	public static String getAddDate(String date ,int i){
		Date formatDateStr = formatDateStr(date, "yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(formatDateStr);
		c.add(Calendar.DATE, i);// 获得d天后的现在时刻
		Date time = c.getTime();
		String formatDate = formatDate(time, "yyyy-MM-dd");
		return formatDate;
	}
	
	
	
	/**
	 * 两个时间之间的天数
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getDays(String date1, String date2) {
		if (date1 == null || date1.equals("")) {
			return 0;
		}
		if (date2 == null || date2.equals("")) {
			return 0;
		}
		// 转换为标准时间
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date date = null;
		java.util.Date mydate = null;
		try {
			date = myFormatter.parse(date1);
			mydate = myFormatter.parse(date2);
		} catch (Exception e) {
		}
		long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		return day;
	}

	/**
	 * 取得数据库主键 生成格式为yyyyMMddhhmmssSSS+k位随机数
	 * 
	 * @param k
	 *            表示是取几位随机数，可以自己定
	 */
	public static String getSerialNo(int k) {
		return getDateId() + getRandom(k);
	}

	@SuppressWarnings("unused")
	private static long seed = 1;

	/**
	 * 返回一个随机数
	 * 
	 * @param i
	 *            表示是取几位随机数，可以自己定
	 * @return
	 */
	public static String getRandom(int i) {
		Random jjj = new Random();
		String jj = "";
		for (int k = 0; k < i; k++) {
			jj += jjj.nextInt(9);
		}
		return jj;
	}

	/**
	 * 时间戳转化为字符串
	 * 
	 * @param timestamp
	 * @return
	 */
	public static String timestampToString(Long timestamp) {
		String datetime = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		datetime = format.format(new Date(timestamp * 1000L));
		return datetime;
	}

	/**
	 * 
	 * @param args
	 */
	public static boolean isRightDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		if (date == null || date.equals("")) {
			return false;
		}
		if (date.length() > 10) {
			sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		} else {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
		}
		try {
			Date dt = sdf.parse(date);
			return dt == null ? false : true;
		} catch (ParseException pe) {
			return false;
		}
	}

	/***************************************************************************
	 * //nd=1表示返回的值中包含年度 //yf=1表示返回的值中包含月份 //rq=1表示返回的值中包含日期 //format表示返回的格式 1
	 * 以年月日中文返回 2 以横线-返回 // 3 以斜线/返回 4 以缩写不带其它符号形式返回 // 5 以点号.返回
	 **************************************************************************/
	public static String getStringDateMonth(String sdate, String nd, String yf, String rq, String format) {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(currentTime);
		String s_nd = dateString.substring(0, 4); // 年份
		String s_yf = dateString.substring(5, 7); // 月份
		String s_rq = dateString.substring(8, 10); // 日期
		String sreturn = "";
		if (sdate == null || sdate.equals("") || !isRightDate(sdate)) { // 处理空值情况
			if (nd.equals("1")) {
				sreturn = s_nd;
				// 处理间隔符
				if (format.equals("1")) {
					sreturn = sreturn + "年";
				} else if (format.equals("2")) {
					sreturn = sreturn + "-";
				} else if (format.equals("3")) {
					sreturn = sreturn + "/";
				} else if (format.equals("5")) {
					sreturn = sreturn + ".";
				}
			}
			// 处理月份
			if (yf.equals("1")) {
				sreturn = sreturn + s_yf;
				if (format.equals("1")) {
					sreturn = sreturn + "月";
				} else if (format.equals("2")) {
					sreturn = sreturn + "-";
				} else if (format.equals("3")) {
					sreturn = sreturn + "/";
				} else if (format.equals("5")) {
					sreturn = sreturn + ".";
				}
			}
			// 处理日期
			if (rq.equals("1")) {
				sreturn = sreturn + s_rq;
				if (format.equals("1")) {
					sreturn = sreturn + "日";
				}
			}
		} else {
			// 是一个合法的日期值，则先将其转换为标准的时间格式
			sdate = formatDateStrToStr(sdate, "yyyy-MM-dd");
			s_nd = sdate.substring(0, 4); // 年份
			s_yf = sdate.substring(5, 7); // 月份
			s_rq = sdate.substring(8, 10); // 日期
			if (nd.equals("1")) {
				sreturn = s_nd;
				// 处理间隔符
				if (format.equals("1")) {
					sreturn = sreturn + "年";
				} else if (format.equals("2")) {
					sreturn = sreturn + "-";
				} else if (format.equals("3")) {
					sreturn = sreturn + "/";
				} else if (format.equals("5")) {
					sreturn = sreturn + ".";
				}
			}
			// 处理月份
			if (yf.equals("1")) {
				sreturn = sreturn + s_yf;
				if (format.equals("1")) {
					sreturn = sreturn + "月";
				} else if (format.equals("2")) {
					sreturn = sreturn + "-";
				} else if (format.equals("3")) {
					sreturn = sreturn + "/";
				} else if (format.equals("5")) {
					sreturn = sreturn + ".";
				}
			}
			// 处理日期
			if (rq.equals("1")) {
				sreturn = sreturn + s_rq;
				if (format.equals("1")) {
					sreturn = sreturn + "日";
				}
			}
		}
		return sreturn;
	}

	public static String getNextMonthDay(String sdate, int m) {
		sdate = formatDateStrToStr(sdate, "yyyy-MM-dd");
		int year = Integer.parseInt(sdate.substring(0, 4));
		int month = Integer.parseInt(sdate.substring(5, 7));
		month = month + m;
		if (month < 0) {
			month += 12;
			year--;
		} else if (month > 12) {
			month -= 12;
			year++;
		}
		String smonth = "";
		if (month < 10) {
			smonth = "0" + month;
		} else {
			smonth = "" + month;
		}
		return year + "-" + smonth + "-01";
	}

	/**
	 * 得到当前服务器时间
	 * 
	 * @return 字符串 yyyyMMdd
	 */
	public static String getcurrenttime() {
		String datatime = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
		return datatime;
	}
	
	/**
	 * 得到当前服务器时间
	 * 
	 * @return 字符串 yyyyMMdd
	 */
	public static String getcurrenttimeDate() {
		String datatime = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		return datatime +" 23:59:59";
	}

	public static String timestamp2String(Timestamp timestamp, String pattern) {
		if (timestamp == null) {
			throw new java.lang.IllegalArgumentException("timestamp null illegal");
		}
		if (pattern == null || pattern.equals("")) {
			pattern = PATTERN_STANDARD;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(new Date(timestamp.getTime()));
	}

	public static String date2String(java.util.Date date, String pattern) {
		if (date == null) {
			throw new java.lang.IllegalArgumentException("timestamp null illegal");
		}
		if (pattern == null || pattern.equals("")) {
			pattern = PATTERN_STANDARD;
			;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	public static Timestamp currentTimestamp() {
		return new Timestamp(new Date().getTime());
	}

	public static String currentTimestamp2String(String pattern) {
		return timestamp2String(currentTimestamp(), pattern);
	}

	public static Timestamp string2Timestamp(String strDateTime, String pattern) {
		if (strDateTime == null || strDateTime.equals("")) {
			throw new java.lang.IllegalArgumentException("Date Time Null Illegal");
		}
		if (pattern == null || pattern.equals("")) {
			pattern = PATTERN_STANDARD;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		Date date = null;
		try {
			date = sdf.parse(strDateTime);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return new Timestamp(date.getTime());
	}

	public static Date string2Date(String strDate, String pattern) {
		if (strDate == null || strDate.equals("")) {
			throw new RuntimeException("str date null");
		}
		if (pattern == null || pattern.equals("")) {
			pattern = DateUtil.PATTERN_DATE;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		Date date = null;

		try {
			date = sdf.parse(strDate);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return date;
	}

	public static String stringToYear(String strDest) {
		if (strDest == null || strDest.equals("")) {
			throw new java.lang.IllegalArgumentException("str dest null");
		}

		Date date = string2Date(strDest, DateUtil.PATTERN_DATE);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return String.valueOf(c.get(Calendar.YEAR));
	}

	public static String stringToMonth(String strDest) {
		if (strDest == null || strDest.equals("")) {
			throw new java.lang.IllegalArgumentException("str dest null");
		}

		Date date = string2Date(strDest, DateUtil.PATTERN_DATE);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		// return String.valueOf(c.get(Calendar.MONTH));
		int month = c.get(Calendar.MONTH);
		month = month + 1;
		if (month < 10) {
			return "0" + month;
		}
		return String.valueOf(month);
	}

	public static String stringToDay(String strDest) {
		if (strDest == null || strDest.equals("")) {
			throw new java.lang.IllegalArgumentException("str dest null");
		}

		Date date = string2Date(strDest, DateUtil.PATTERN_DATE);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		// return String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		int day = c.get(Calendar.DAY_OF_MONTH);
		if (day < 10) {
			return "0" + day;
		}
		return "" + day;
	}

	public static Date getFirstDayOfMonth(Calendar c) {
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = 1;
		c.set(year, month, day, 0, 0, 0);
		return c.getTime();
	}

	public static Date getLastDayOfMonth(Calendar c) {
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = 1;
		if (month > 11) {
			month = 0;
			year = year + 1;
		}
		c.set(year, month, day - 1, 0, 0, 0);
		return c.getTime();
	}

	public static String date2GregorianCalendarString(Date date) {
		if (date == null) {
			throw new java.lang.IllegalArgumentException("Date is null");
		}
		long tmp = date.getTime();
		GregorianCalendar ca = new GregorianCalendar();
		ca.setTimeInMillis(tmp);
		try {
			XMLGregorianCalendar t_XMLGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(ca);
			return t_XMLGregorianCalendar.normalize().toString();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new java.lang.IllegalArgumentException("Date is null");
		}

	}

	public static boolean compareDate(Date firstDate, Date secondDate) {
		if (firstDate == null || secondDate == null) {
			throw new java.lang.RuntimeException();
		}

		String strFirstDate = date2String(firstDate, "yyyy-MM-dd");
		String strSecondDate = date2String(secondDate, "yyyy-MM-dd");
		if (strFirstDate.equals(strSecondDate)) {
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @Function: DateUtil.java
	 * @Description: 该函数的功能描述
	 *		获取今日零点时间
	 * @param currentDate
	 * @return
	 *
	 * @version: v1.0.0
	 * @author: ckx
	 * @date: 2018年6月12日 下午4:19:22 
	 *
	 */
	public static Date getStartTimeOfDate(Date currentDate) {
		Assert.notNull(currentDate);
		String strDateTime = date2String(currentDate, "yyyy-MM-dd") + " 00:00:00";
		return string2Date(strDateTime, "yyyy-MM-dd HH:mm:ss");
	}
	
	/**
	 * 
	 * @Function: DateUtil.java
	 * @Description: 该函数的功能描述
	 *		获取今日零点时间
	 * @param currentDate
	 * @return
	 *
	 * @version: v1.0.0
	 * @author: ckx
	 * @date: 2018年6月12日 下午4:19:22 
	 *
	 */
	public static String getStartTimeOfStr(Date currentDate) {
		Assert.notNull(currentDate);
		String strDateTime = date2String(currentDate, "yyyy-MM-dd") + " 00:00:00";
		return strDateTime;
	}

	/**
	 * 
	 * @Function: DateUtil.java
	 * @Description: 该函数的功能描述
	 *		获取今日晚上凌晨时间
	 * @param currentDate
	 * @return
	 *
	 * @version: v1.0.0
	 * @author: ckx
	 * @date: 2018年6月12日 下午4:19:39 
	 *
	 */
	public static Date getEndTimeOfDate(Date currentDate) {
		Assert.notNull(currentDate);
		String strDateTime = date2String(currentDate, "yyyy-MM-dd") + " 23:59:59";
		return string2Date(strDateTime, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 
	 * @Function: DateUtil.java
	 * @Description: 该函数的功能描述
	 *		获取今日晚上凌晨时间
	 * @param currentDate
	 * @return
	 *
	 * @version: v1.0.0
	 * @author: ckx
	 * @date: 2018年6月12日 下午4:19:39 
	 *
	 */
	public static String getEndTimeOfStr(Date currentDate) {
		Assert.notNull(currentDate);
		String strDateTime = date2String(currentDate, "yyyy-MM-dd") + " 23:59:59";
		return strDateTime;
	}
	 /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     * 
     * @param nowTime 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     * @author jqlin
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

}