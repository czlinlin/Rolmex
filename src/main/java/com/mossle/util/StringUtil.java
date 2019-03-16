package com.mossle.util;

import com.mossle.common.utils.StringUtils;

public class StringUtil {

	public static String toString(Object obj){
		String str = "";
		if(null != obj && !"".equals(obj) && !"null".equals(obj)){
			str = obj.toString();
		}
		
		return str;
		
	}
	/**
	 * 去除字符串结尾的逗号
	 * @param str
	 * @return
	 */
	public static String delComma(String str){
		if(StringUtils.isNotBlank(str)){
			String substring = str.substring(str.length()-1);
			if(",".equals(substring)){
				str = str.substring(0, str.length()-1);
			}
		}
		
		return str;
	}
	
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
    
    public static void main(String[] args) {
    	//String s = String.format("%4d", 1).replace(" ", "0");  
    	String s = String.format("%04d", 1);
    	System.out.println(s);
    	
	}
    
    
}
