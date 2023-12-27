package com.mztrade.hki;

public class Util {
    public static String formatDate(String rawDate) {
        return rawDate.substring(0, 4) + "-" + rawDate.substring(4, 6) + "-" + rawDate.substring(6, 8) + "T00:00:00Z";
    }
}
