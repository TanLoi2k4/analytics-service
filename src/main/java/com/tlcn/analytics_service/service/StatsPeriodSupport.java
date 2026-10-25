package com.tlcn.analytics_service.service;

import com.tlcn.analytics_service.enums.StatsPeriod;
import com.tlcn.analytics_service.util.AnalyticsMetricsSupport.PeriodWindow;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

/**
 * Resolves week/month/year windows in Vietnam time for vendor analytics.
 * Bounds are inclusive on both {@code from} and {@code to}, matching report-date queries.
 */
public final class StatsPeriodSupport {

    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DAY_MONTH_YEAR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final WeekFields ISO_WEEK = WeekFields.ISO;
    private static final LocalDate ALL_TIME_START = LocalDate.of(2000, 1, 1);

    private StatsPeriodSupport() {
    }

    public record ResolvedPeriod(
        LocalDate from,
        LocalDate to,
        LocalDate previousFrom,
        LocalDate previousTo,
        String label
    ) {
        public PeriodWindow toWindow() {
            return new PeriodWindow(from, to, previousFrom, previousTo);
        }
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static LocalDate normalizeAnchor(LocalDate anchor) {
        return anchor != null ? anchor : today();
    }

    public static ResolvedPeriod resolve(StatsPeriod period, LocalDate anchor) {
        return resolve(period, anchor, today());
    }

    public static ResolvedPeriod resolve(StatsPeriod period, LocalDate anchor, LocalDate today) {
        StatsPeriod safePeriod = period == null ? StatsPeriod.MONTH : period;
        LocalDate date = normalizeAnchor(anchor);
        LocalDate safeToday = today != null ? today : today();
        return switch (safePeriod) {
            case ALL -> allRange(safeToday);
            case WEEK -> weekRange(date, safeToday);
            case MONTH -> monthRange(date, safeToday);
            case YEAR -> yearRange(date, safeToday);
        };
    }

    public static String cacheSuffix(StatsPeriod period, LocalDate anchor) {
        StatsPeriod safePeriod = period == null ? StatsPeriod.MONTH : period;
        if (safePeriod == StatsPeriod.ALL) {
            return "ALL";
        }
        ResolvedPeriod resolved = resolve(safePeriod, anchor);
        return safePeriod.name() + ":" + resolved.from();
    }

    private static ResolvedPeriod allRange(LocalDate today) {
        return new ResolvedPeriod(
            ALL_TIME_START,
            today,
            ALL_TIME_START,
            today,
            "Tất cả thời gian"
        );
    }

    private static ResolvedPeriod weekRange(LocalDate date, LocalDate today) {
        LocalDate start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        LocalDate previousStart = start.minusWeeks(1);
        LocalDate previousEnd = start.minusDays(1);
        if (end.isAfter(today) && !today.isBefore(start)) {
            previousEnd = previousStart.plusDays(ChronoUnit.DAYS.between(start, today));
        }
        int week = start.get(ISO_WEEK.weekOfWeekBasedYear());
        int weekYear = start.get(ISO_WEEK.weekBasedYear());
        String label = "Tuần " + week + "/" + weekYear
            + " (" + start.format(DAY_MONTH) + " – " + end.format(DAY_MONTH_YEAR) + ")";
        return new ResolvedPeriod(start, end, previousStart, previousEnd, label);
    }

    private static ResolvedPeriod monthRange(LocalDate date, LocalDate today) {
        LocalDate start = date.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        LocalDate previousStart = start.minusMonths(1);
        LocalDate previousEnd = start.minusDays(1);
        if (end.isAfter(today) && !today.isBefore(start)) {
            int day = Math.min(today.getDayOfMonth(), previousStart.lengthOfMonth());
            previousEnd = previousStart.withDayOfMonth(day);
        }
        String label = "Tháng " + start.getMonthValue() + "/" + start.getYear();
        return new ResolvedPeriod(start, end, previousStart, previousEnd, label);
    }

    private static ResolvedPeriod yearRange(LocalDate date, LocalDate today) {
        LocalDate start = date.withDayOfYear(1);
        LocalDate end = start.plusYears(1).minusDays(1);
        LocalDate previousStart = start.minusYears(1);
        LocalDate previousEnd = start.minusDays(1);
        if (end.isAfter(today) && !today.isBefore(start)) {
            previousEnd = today.minusYears(1);
        }
        String label = "Năm " + start.getYear();
        return new ResolvedPeriod(start, end, previousStart, previousEnd, label);
    }
}
