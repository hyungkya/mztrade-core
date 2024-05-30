package com.mztrade.hki;

import java.time.LocalDateTime;

public class Util {
    public static String formatDate(String rawDate) {
        if (rawDate.length() == 8) {
            return rawDate.substring(0, 4) + "-" + rawDate.substring(4, 6) + "-" + rawDate.substring(6, 8) + "T09:00:00";
        } else {
            return rawDate.substring(0, 4) + "-"
                    + rawDate.substring(4, 6) + "-"
                    + rawDate.substring(6, 8) + "T"
                    + rawDate.substring(8, 10) + ":"
                    + rawDate.substring(10, 12) + ":00";
        }

    }

    public static LocalDateTime stringToLocalDateTime(String rawDate) {
        if (rawDate.length() == 8) {
            return LocalDateTime.parse(rawDate.substring(0, 4) + "-" + rawDate.substring(4, 6) + "-" + rawDate.substring(6, 8) + "T09:00:00");
        } else {
            return LocalDateTime.parse(
                    rawDate.substring(0, 4) + "-" +
                    rawDate.substring(4, 6) + "-" +
                            rawDate.substring(6, 8) + "T" + rawDate.substring(8, 10) + ":" + rawDate.substring(10, 12) + ":00");
        }
    }
}
