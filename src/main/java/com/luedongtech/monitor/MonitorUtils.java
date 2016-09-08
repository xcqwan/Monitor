package com.luedongtech.monitor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zombie on 14/12/4.
 */
public class MonitorUtils {
    private static final String SERVER_DATE_FORMATTER = "yyyy-MM-dd HH:mm:ss";
    private static long TimeCompens = 0;

    public static String getCurrentTimeStr() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(getCompensDate());
    }

    public static String getDateStr() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(getCompensDate());
    }

    public static String getDateHourStr() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
        return dateFormat.format(getCompensDate());
    }

    protected static void calcTimeCompens(String datetime) {
        if (datetime == null || datetime.isEmpty()) {
            return;
        }
        try {
            Date date = (new SimpleDateFormat(SERVER_DATE_FORMATTER)).parse(datetime);
            Calendar target = Calendar.getInstance();
            target.setTime(date);
            Calendar now = Calendar.getInstance();
            TimeCompens = target.getTimeInMillis() - now.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static Date getCompensDate() {
        Date date = new Date();
        date.setTime(date.getTime() + TimeCompens);
        return date;
    }
}
