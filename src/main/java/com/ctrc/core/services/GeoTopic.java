package com.ctrc.core.services;

public final class GeoTopic {

    private GeoTopic() {
    }

    public static final double CELL_SIZE = 0.1;

    public static String of(double latitude, double longitude) {
        return "geo_" + part(latitude) + "_" + part(longitude);
    }

    private static String part(double value) {
        long cell = (long) Math.floor(value / CELL_SIZE);
        return cell < 0 ? "m" + (-cell) : Long.toString(cell);
    }
}
