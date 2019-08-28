package com.parkingspots;

import static java.time.DayOfWeek.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalendarHelper {

    private final LocalDate date;
    private final LocalDate endDate;

    CalendarHelper(LocalDate startDate, LocalDate endDate) {
        this.date = startDate;
        this.endDate = endDate;
    }

    public int numberOfWorkDaysInMonth() {
        return workDaysInMonth().size();
    }

    public List<LocalDate> daysFromIndices(List<Integer> indices) {
        List<LocalDate> dates = workDaysInMonth();
        return indices.stream().map(dayIndex -> dates.get(dayIndex-1)).collect(Collectors.toList());
    }

    public List<Integer> indicesFromDays(List<LocalDate> days) {
        List<LocalDate> dates = workDaysInMonth();
        return days.stream().map(day -> dates.indexOf(day)+1).collect(Collectors.toList());
    }

    private List<LocalDate> workDaysInMonth() {
        return date.datesUntil(endDate)
            .filter(t -> Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
                .anyMatch(t.getDayOfWeek()::equals))
            .collect(Collectors.toList());
    }
}
