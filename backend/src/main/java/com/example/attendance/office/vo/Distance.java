package com.example.attendance.office.vo;

public record Distance(double meters) {

    public Distance {
        if (meters < 0) {
            throw new IllegalArgumentException("距離は0以上である必要があります: " + meters);
        }
    }

    public static Distance ofMeters(double meters) {
        return new Distance(meters);
    }

    public boolean isWithin(double radiusMeters) {
        return meters <= radiusMeters;
    }

    public double toKilometers() {
        return meters / 1000.0;
    }

    public String toFormattedString() {
        if (meters < 1000) {
            return String.format("%.0fm", meters);
        }
        return String.format("%.1fkm", toKilometers());
    }
}
