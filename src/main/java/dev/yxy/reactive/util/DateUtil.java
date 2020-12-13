package dev.yxy.reactive.util;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @NotNull
    public static String formatDate(@NotNull Date date) {
        return sdf.format(date);
    }

    @NotNull
    public static Date parseDate(@NotNull String date) {
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            return new Date();
        }
    }

    @NotNull
    public static String getCurrentTime() {
        return sdf.format(new Date());
    }
}
