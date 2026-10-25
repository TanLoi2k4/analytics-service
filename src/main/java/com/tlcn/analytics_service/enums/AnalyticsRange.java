package com.tlcn.analytics_service.enums;

public enum AnalyticsRange {
    SEVEN_DAYS("7d", 7),
    THIRTY_DAYS("30d", 30),
    NINETY_DAYS("90d", 90),
    ONE_YEAR("1y", 365);

    private final String code;
    private final int days;

    AnalyticsRange(String code, int days) {
        this.code = code;
        this.days = days;
    }

    public String getCode() {
        return code;
    }

    public int getDays() {
        return days;
    }

    public static AnalyticsRange fromCode(String code) {
        if (code == null || code.isBlank()) {
            return THIRTY_DAYS;
        }
        for (AnalyticsRange range : values()) {
            if (range.code.equalsIgnoreCase(code.trim())) {
                return range;
            }
        }
        throw new IllegalArgumentException("Unsupported range: " + code);
    }
}
