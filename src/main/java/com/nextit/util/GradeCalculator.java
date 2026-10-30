package com.nextit.util;

import com.nextit.model.Attendance;
import com.nextit.model.Grade;

import java.util.List;

/**
 * Pure calculation helpers shared by the dashboards and covered by unit tests.
 */
public final class GradeCalculator {

    private GradeCalculator() {}

    public static Double percentage(double score, double maxScore) {
        if (maxScore <= 0) return null;
        return round(score / maxScore * 100.0);
    }

    public static double average(List<Grade> grades) {
        return round(grades.stream()
                .map(g -> g.getGrade() != null ? g.getGrade() : percentage(g.getScore(), g.getMaxScore()))
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0));
    }

    public static double attendanceRate(List<Attendance> records) {
        if (records.isEmpty()) return 0.0;
        long absent = records.stream()
                .filter(a -> "Absent".equalsIgnoreCase(a.getStatus()))
                .count();
        return round((records.size() - absent) * 100.0 / records.size());
    }

    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
