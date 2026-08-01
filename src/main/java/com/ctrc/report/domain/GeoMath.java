package com.ctrc.report.domain;

import java.util.List;

/**
 * Small geodesic helpers for the route-corridor scan.
 *
 * <p>Distances use a local equirectangular projection rather than full
 * haversine maths. Over the few kilometres a corridor spans the error is well
 * under a metre, and it keeps the per-report filter cheap.
 */
public final class GeoMath {

    private static final double EARTH_RADIUS_METERS = 6378137.0;

    private GeoMath() {
    }

    /** Great-circle distance in meters between two coordinates. */
    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Perpendicular distance in meters from a point to the segment a-b. */
    public static double distanceToSegmentMeters(double pointLat, double pointLng,
                                                 double aLat, double aLng,
                                                 double bLat, double bLng) {
        double cosLat = Math.cos(Math.toRadians((aLat + bLat + pointLat) / 3.0));

        double px = Math.toRadians(pointLng) * cosLat * EARTH_RADIUS_METERS;
        double py = Math.toRadians(pointLat) * EARTH_RADIUS_METERS;
        double ax = Math.toRadians(aLng) * cosLat * EARTH_RADIUS_METERS;
        double ay = Math.toRadians(aLat) * EARTH_RADIUS_METERS;
        double bx = Math.toRadians(bLng) * cosLat * EARTH_RADIUS_METERS;
        double by = Math.toRadians(bLat) * EARTH_RADIUS_METERS;

        double dx = bx - ax;
        double dy = by - ay;
        double lengthSq = dx * dx + dy * dy;

        if (lengthSq == 0) {
            return Math.hypot(px - ax, py - ay);
        }

        double t = ((px - ax) * dx + (py - ay) * dy) / lengthSq;
        t = Math.max(0, Math.min(1, t));

        return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    /**
     * Shortest distance in meters from a point to a polyline, along with how far
     * along that polyline the closest point sits.
     *
     * @return {@code [offsetMeters, alongMeters]}
     */
    public static double[] projectOnPolyline(double pointLat, double pointLng,
                                             List<double[]> path) {
        if (path.isEmpty()) {
            return new double[] {Double.MAX_VALUE, 0};
        }
        if (path.size() == 1) {
            double[] only = path.get(0);
            return new double[] {distanceMeters(pointLat, pointLng, only[0], only[1]), 0};
        }

        double best = Double.MAX_VALUE;
        double bestAlong = 0;
        double travelled = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            double[] a = path.get(i);
            double[] b = path.get(i + 1);

            double segmentLength = distanceMeters(a[0], a[1], b[0], b[1]);
            double offset = distanceToSegmentMeters(pointLat, pointLng, a[0], a[1], b[0], b[1]);

            if (offset < best) {
                best = offset;
                double toStart = distanceMeters(pointLat, pointLng, a[0], a[1]);
                double projected = Math.sqrt(Math.max(0, toStart * toStart - offset * offset));
                bestAlong = travelled + Math.min(projected, segmentLength);
            }

            travelled += segmentLength;
        }

        return new double[] {best, bestAlong};
    }

    /** Degrees of latitude that correspond to a distance in meters. */
    public static double latitudeDegreesFor(double meters) {
        return meters / 111_320.0;
    }

    /** Degrees of longitude that correspond to a distance in meters at a latitude. */
    public static double longitudeDegreesFor(double meters, double atLatitude) {
        double scale = Math.cos(Math.toRadians(atLatitude));
        if (Math.abs(scale) < 1e-6) {
            return 180;
        }
        return meters / (111_320.0 * scale);
    }
}
