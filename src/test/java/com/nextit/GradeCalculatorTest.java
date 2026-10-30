package com.nextit;

import com.nextit.model.Attendance;
import com.nextit.model.Grade;
import com.nextit.util.GradeCalculator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GradeCalculatorTest {

    @Test
    void percentageUsesScoreAndMaximum() {
        assertEquals(90.0, GradeCalculator.percentage(45, 50));
        assertEquals(33.33, GradeCalculator.percentage(1, 3));
    }

    @Test
    void percentageReturnsNullForInvalidMaximum() {
        assertNull(GradeCalculator.percentage(10, 0));
        assertNull(GradeCalculator.percentage(10, -5));
    }

    @Test
    void averageUsesStoredGradeWhenAvailable() {
        Grade stored = grade(10, 20);
        stored.setGrade(80.0);
        Grade derived = grade(5, 10);

        assertEquals(65.0, GradeCalculator.average(List.of(stored, derived)));
    }

    @Test
    void averageSkipsRecordsWithoutAGrade() {
        Grade graded = grade(5, 10);
        Grade invalid = grade(5, 0);

        assertEquals(50.0, GradeCalculator.average(List.of(graded, invalid)));
        assertEquals(0.0, GradeCalculator.average(new ArrayList<>()));
    }

    @Test
    void attendanceRateCountsAbsences() {
        List<Attendance> records = List.of(
                attendance("Present"),
                attendance("Late"),
                attendance("Absent"),
                attendance("Excused"));

        assertEquals(75.0, GradeCalculator.attendanceRate(records));
        assertEquals(0.0, GradeCalculator.attendanceRate(List.of()));
    }

    private static Grade grade(double score, double maxScore) {
        Grade grade = new Grade();
        grade.setScore(score);
        grade.setMaxScore(maxScore);
        return grade;
    }

    private static Attendance attendance(String status) {
        Attendance attendance = new Attendance();
        attendance.setStatus(status);
        return attendance;
    }
}
