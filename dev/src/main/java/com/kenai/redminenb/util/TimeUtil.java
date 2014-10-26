package com.kenai.redminenb.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeUtil {
    private static final Logger LOG = Logger.getLogger(TimeUtil.class.getName());

    private TimeUtil() {
    }

    public static String millisecondsToDecimalHours(long timeInMS) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        return nf.format( ((double)timeInMS) / (60 * 60 * 1000));
    }

    public static long decimalHoursToMilliseconds(String timeString) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        Double d;
        try {
            d = nf.parse(timeString).doubleValue();
        } catch (ParseException ex) {
            nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
            try {
                d = nf.parse(timeString).doubleValue();
            } catch (ParseException ex1) {
                LOG.log(Level.INFO, "Failed to parse time: {0}", timeString);
                return 0;
            }
        }
        return (long) (d * 60 * 60 * 1000);
    }
}
