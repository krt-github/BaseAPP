package com.krt.basemodule.utils;

import android.content.Context;

import com.yichat.base.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    public static final int MSEC = 1;
    public static final int SEC  = 1000;
    public static final int MIN  = 60000;
    public static final int HOUR = 3600000;
    public static final int DAY  = 86400000;

    private static SimpleDateFormat mSDF;
    private static String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm";
    private static String mDateTimeSeparator = " ";

    private TimeUtils(){}

    public static void init(Context context){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("yyyy/MM/dd");
//        stringBuilder.append("yyyy");
//        stringBuilder.append(context.getResources().getString(R.string.sdf_year));
//        stringBuilder.append("MM");
//        stringBuilder.append(context.getResources().getString(R.string.sdf_month));
//        stringBuilder.append("dd");
//        stringBuilder.append(context.getResources().getString(R.string.sdf_day));
        stringBuilder.append(mDateTimeSeparator);
        stringBuilder.append("HH:mm:ss");
        DEFAULT_PATTERN = stringBuilder.toString();

        mSDF = new SimpleDateFormat(DEFAULT_PATTERN, Locale.getDefault());
    }

    public static void setPattern(String pattern){
        mSDF.applyPattern(pattern);
    }

    public static void setDateTimeSeparator(String separator){
        mDateTimeSeparator = separator;
    }

    public static String getFullTime(long time){
        return mSDF.format(new Date(time));
    }

    public static String getFullTime(String pattern, long time){
        setPattern(pattern);
        String timeString = getFullTime(time);
        setPattern(DEFAULT_PATTERN);
        return timeString;
    }

    public static String getTime(long time){
        String fullTime = getFullTime(time);
        return fullTime.split(mDateTimeSeparator)[1];
    }

    public static String getDate(long time){
        String fullTime = getFullTime(time);
        return fullTime.split(mDateTimeSeparator)[0];
    }

    public static String getFriendlyDate(Context context, long millis){
        long now = System.currentTimeMillis();
        long span = now - millis;
        if (span < 0)
            // U can read http://www.apihome.cn/api/java/Formatter.html to understand it.
            try {
                return getTime(millis);//String.format("%tc", millis);
            }catch(Exception e){
                e.printStackTrace();
                return String.format("%tc", millis);
            }

        if (span < HOUR) {
            return context.getString(R.string.today);
        }
        // 获取当天 00:00
        long wee = getWeeOfToday();
        if (millis >= wee) {
            return context.getString(R.string.today);
        } else if (millis >= wee - DAY) {
            return context.getString(R.string.yesterday);
        }else if(millis >= wee - DAY * 2){
            return context.getString(R.string.before_yesterday);
        } else {
            try {
                return getDate(millis);
            }catch(Exception e){
                return String.format("%tF", millis);
            }
        }

//        long delta = new Date().getTime() - ms;
//        if (delta < 24L * ONE_HOUR) {
//            return context.getString(R.string.today);
//        }
//        if (delta < 48L * ONE_HOUR) {
//            return context.getString(R.string.yesterday);//"昨天";
//        }
//        if(delta < 72L * ONE_HOUR){
//            return context.getString(R.string.before_yesterday);
//        }
//
//        try {
//            return getDate(ms);
//        }catch(Exception e){
//            long days = toDays(delta);
//            return (days <= 0 ? 1 : days) + context.getString(R.string.day_ago);
//        }
    }

    public static String getFriendlyForList(Context context, final long millis){
        long now = System.currentTimeMillis();
        long span = now - millis;
        if (span < 0)
            // U can read http://www.apihome.cn/api/java/Formatter.html to understand it.
            try {
                return getTime(millis);//String.format("%tc", millis);
            }catch(Exception e){
                e.printStackTrace();
                return String.format("%tc", millis);
            }
        if (span < 1000 * 60) {
            return context.getString(R.string.just_now);
        } else if (span < HOUR) {
            return String.format(Locale.getDefault(),
                    "%d" + context.getString(R.string.minute_ago), span / MIN);
        }
        // 获取当天 00:00
        long wee = getWeeOfToday();
        if (millis >= wee) {
            return String.format("%tT", millis);
        } else if (millis >= wee - DAY) {
            return context.getString(R.string.yesterday);
        }else if(millis >= wee - DAY * 2){
            return context.getString(R.string.before_yesterday);
        } else {
            try {
                return getDate(millis);
            }catch(Exception e){
                return String.format("%tF", millis);
            }
        }
    }

    private static long getWeeOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static String getFriendlyTime(Context context, long ms){
        return format(context, ms);
        //未来时间
//        Date currentTime = new Date(System.currentTimeMillis());
//        Date paramTime = new Date(ms);
//
//        String res;
//        int year = currentTime.getYear() - paramTime.getYear();
//        int day = currentTime.getDate() - paramTime.getDate();
//        switch(day){
//            case 0: res = caseDate(context, currentTime, paramTime);
//                break;
//            case 1: res = context.getString(R.string.yesterday);
//                break;
//            case 2: res = context.getString(R.string.before_yesterday);
//                break;
//            default: res = day + context.getString(R.string.day_ago);
//                break;
//        }
//        return res;
    }

    private static String caseDate(Context context, Date currentTime, Date paramTime){
        int hours = currentTime.getHours() - paramTime.getHours();
        if (hours > 0) {
            int minute = currentTime.getMinutes() - paramTime.getMinutes();
            if (minute > 0) {
                return hours + context.getString(R.string.hour_ago);
            } else if (minute > -60){
                return 60 + minute + context.getString(R.string.minute_ago);
            } else {
                return context.getString(R.string.just_now);
            }
        } else {
            int minute = currentTime.getMinutes() - paramTime.getMinutes();
            return minute > 2
                    ? (minute + context.getString(R.string.minute_ago))
                    : context.getString(R.string.just_now);
        }
    }

    public static long getTimeSeconds(){
        return getTimeMillis() / 1000;
    }

    public static long getTimeMillis(){
        return System.currentTimeMillis();
    }

    public static long getTimeNano(){
        return System.nanoTime();
    }

    private static final long ONE_MINUTE = 60000L;
    private static final long ONE_HOUR = 3600000L;
    private static final long ONE_DAY = 86400000L;
    private static final long ONE_WEEK = 604800000L;

    private static final String ONE_SECOND_AGO = "秒前";
    private static final String ONE_MINUTE_AGO = "分钟前";
    private static final String ONE_HOUR_AGO = "小时前";
    private static final String ONE_DAY_AGO = "天前";
    private static final String ONE_MONTH_AGO = "月前";
    private static final String ONE_YEAR_AGO = "年前";

    public static String format(Context context, long ms) {
        long delta = new Date().getTime() - ms;
        if (delta < 1L * ONE_MINUTE) {
//            long seconds = toSeconds(delta);
//            return (seconds <= 0 ? 1 : seconds) + ONE_SECOND_AGO;
            return context.getString(R.string.just_now);
        }
        if (delta < 60L * ONE_MINUTE) {
            long minutes = toMinutes(delta);
            return (minutes <= 0 ? 1 : minutes) + context.getString(R.string.minute_ago);//ONE_MINUTE_AGO;
        }
        if (delta < 24L * ONE_HOUR) {
            long hours = toHours(delta);
            return (hours <= 0 ? 1 : hours) + context.getString(R.string.hour_ago);//ONE_HOUR_AGO;
        }
        if (delta < 48L * ONE_HOUR) {
            return context.getString(R.string.yesterday);//"昨天";
        }
        if(delta < 72L * ONE_HOUR){
            return context.getString(R.string.before_yesterday);
        }

        try {
            return getDate(ms);
        }catch(Exception e){
            long days = toDays(delta);
            return (days <= 0 ? 1 : days) + context.getString(R.string.day_ago);
        }
//        if (delta < 30L * ONE_DAY) {
//            long days = toDays(delta);
//            return (days <= 0 ? 1 : days) + ONE_DAY_AGO;
//        }
//        if (delta < 12L * 4L * ONE_WEEK) {
//            long months = toMonths(delta);
//            return (months <= 0 ? 1 : months) + ONE_MONTH_AGO;
//        } else {
//            long years = toYears(delta);
//            return (years <= 0 ? 1 : years) + ONE_YEAR_AGO;
//        }
    }

    public static String format2(Context context, long ms) {
        Date current = new Date();
        long delta = current.getTime() - ms;
        if (delta < 1L * ONE_MINUTE) {
            return context.getString(R.string.just_now);
        }
        if (delta < 60L * ONE_MINUTE) {
            long minutes = toMinutes(delta);
            return (minutes <= 0 ? 1 : minutes) + context.getString(R.string.minute_ago);//ONE_MINUTE_AGO;
        }

        Date timestamp = new Date(ms);
        int dayOffset = current.getDay() - timestamp.getDay();
        if(dayOffset <= 0){
            long hours = toHours(delta);
            return (hours <= 0 ? 1 : hours) + context.getString(R.string.hour_ago);//ONE_HOUR_AGO;
        }else if(dayOffset <= 1){
            return context.getString(R.string.yesterday);//"昨天";
        }else if(dayOffset <= 2){
            return context.getString(R.string.before_yesterday);
        }else{
            try {
                return getDate(ms);
            }catch(Exception e){
                long days = toDays(delta);
                return (days <= 0 ? 1 : days) + context.getString(R.string.day_ago);
            }
        }
    }

    private static long toSeconds(long date) {
        return date / 1000L;
    }

    private static long toMinutes(long date) {
        return toSeconds(date) / 60L;
    }

    private static long toHours(long date) {
        return toMinutes(date) / 60L;
    }

    private static long toDays(long date) {
        return toHours(date) / 24L;
    }

    private static long toMonths(long date) {
        return toDays(date) / 30L;
    }

    private static long toYears(long date) {
        return toMonths(date) / 365L;
    }

    public static void release(){
        mSDF = null;
    }
}
