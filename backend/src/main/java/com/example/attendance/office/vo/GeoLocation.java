package com.example.attendance.office.vo;

import jakarta.persistence.Embeddable;

@Embeddable
public record GeoLocation(double latitude, double longitude) {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    public GeoLocation {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("緯度は -90〜90 の範囲で指定してください: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("経度は -180〜180 の範囲で指定してください: " + longitude);
        }
    }

    public static GeoLocation of(double latitude, double longitude) {
        return new GeoLocation(latitude, longitude);
    }

    public Distance distanceTo(GeoLocation other) {
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Distance.ofMeters(EARTH_RADIUS_METERS * c);
    }
}
