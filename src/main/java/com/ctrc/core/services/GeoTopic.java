package com.ctrc.core.services;

/**
 * Turns a point on the map into a Firebase topic name.
 *
 * <p>Mirrors {@code geo_topic.dart} in the Flutter app. The phone subscribes to
 * the cells around itself and this side publishes a new report to the one cell
 * it falls in, so the two only ever meet through this string. Change one,
 * change both.
 */
public final class GeoTopic {

    private GeoTopic() {
    }

    /** A tenth of a degree, a little over 11 km north to south. */
    public static final double CELL_SIZE = 0.1;

    public static String of(double latitude, double longitude) {
        return "geo_" + part(latitude) + "_" + part(longitude);
    }

    /** Topic names may not contain a minus sign, so negative cells use 'm'. */
    private static String part(double value) {
        long cell = (long) Math.floor(value / CELL_SIZE);
        return cell < 0 ? "m" + (-cell) : Long.toString(cell);
    }
}
