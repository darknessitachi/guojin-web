package com.xun360.guojin.util;

import java.util.Date;
import java.util.TimeZone;


public class TimeSolUtils {
	
	public static final int offsetHous = TimeZone.getDefault().getOffset(new Date().getTime())/(3600*1000);

	public static String getTimeStrByMilOneDay(int ms){
		return String.format("%02d", (int)Math.floor(ms/(3600*1000))) + ":" + String.format("%02d", (int)Math.floor(ms%(3600*1000)/(60*1000)));
	}
	
	public static void main(String[] args) {
		System.out.println(getTimeStrByMilOneDay(12371623));
		System.out.println(3*3600*1000 + 26*60*1000);
	}
}
