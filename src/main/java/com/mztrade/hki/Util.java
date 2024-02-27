package com.mztrade.hki;

import java.time.LocalDateTime;

public class Util {
    public static String formatDate(String rawDate) {
        return rawDate.substring(0, 4) + "-" + rawDate.substring(4, 6) + "-" + rawDate.substring(6, 8) + "T09:00:00";
    }

    public static LocalDateTime stringToLocalDateTime(String rawDate) {
        return LocalDateTime.parse(rawDate.substring(0, 4) + "-" + rawDate.substring(4, 6) + "-" + rawDate.substring(6, 8) + "T09:00:00");
    }
}
