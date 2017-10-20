package com.demo.common.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by a on 15-4-15.
 */
public class DateUtil {

    private static DateFormat LOG_DATE_FORMATTER_BEGIN = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
    private static DateFormat GET_CURRENT_HOUR = new SimpleDateFormat("HH");

    /**
     * 获取当天0点0时0分0秒的时间
     * @return
     */
    public static Timestamp getTodayZeroClockTime(){
        Calendar rightNow = Calendar.getInstance();
        rightNow.clear();
        rightNow.setTime(new Date());
        Date infoDate = rightNow.getTime();
        String time = LOG_DATE_FORMATTER_BEGIN.format(infoDate);
        return  Timestamp.valueOf(time);
    }

    public static String getCurrentHour(){
        Date currentTime = new Date();
        String hourString = GET_CURRENT_HOUR.format(currentTime);
        return hourString;
    }

    public static boolean isStrOutBetween(String start,String end,String cur){
        if(cur.compareTo(start)<0||cur.compareTo(end)>=0){
            return true;
        }
        return false;
    }

    public static Date stringConvertDate(String dateString){
        if(StringUtil.isEmpty(dateString)){
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }


    }

    /** * 获取指定日期是星期几
     * 参数为null时表示获取当前日期是星期几
     * @param date
     * @return
     */
    public static int getWeekOfDate(Date date) {
        int[] weekOfDays = {7,1, 2, 3, 4, 5,6};
        Calendar calendar = Calendar.getInstance();
        if(date != null){
            calendar.setTime(date);
        }
        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0){
            w = 0;
        }
        return weekOfDays[w];
    }

}
