package com.parkingspots;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class Entry {
    public static final Boolean DEBUG = false;
    final String name;
    final float ratio;
    final List<Integer> daysToIgnore;

    Entry(String name, float ratio, List<Integer> daysToIgnore) {
        this.name = name;
        this.ratio = ratio;
        this.daysToIgnore = daysToIgnore;
    }

    Entry(String name, float ratio) {
        this.name = name;
        this.ratio = ratio;
        this.daysToIgnore = new ArrayList<>();
    }
}

class DedicatedEntry extends Entry {
    int days;
    int extraDays = 0;
    int remainingDays = 0;

    DedicatedEntry(Entry entry) {
        super(entry.name, entry.ratio, entry.daysToIgnore);
    }
    DedicatedEntry(DedicatedEntry entry) {
        super(entry.name, entry.ratio, entry.daysToIgnore);
        this.days = entry.days;
        this.extraDays = entry.extraDays;
        this.remainingDays = entry.remainingDays;
    }

    @Override
    public String toString() {
        if (DEBUG) {
            return "Person " + name + " with ratio " + ratio + " has " + days + " days in month and " + extraDays + " extra days and " + remainingDays + " remaining days. ";
        }
        else {
            return "Person " + name + " has " + days + " distrubuted days including " + extraDays + " extra days and " + remainingDays + " not distributed days. ";
        }
    }
}

class DistributedEntry extends DedicatedEntry {
    List<Integer> distributedDays = new ArrayList<>();

    DistributedEntry(DedicatedEntry entry) {
        super(entry);
    }

//    @Override
//    public String toString() {
//        String s = super.toString();
//        s += "Distributed Days: ";
//        for (Integer i : distributedDays) {
//            s += i + ", ";
//        }
//        return s;
//    }
}

class CalendarEntry extends DistributedEntry {

    private final CalendarHelper calendarHelper;

    CalendarEntry(DistributedEntry entry, CalendarHelper calendarHelper) {
        super(entry);
        this.distributedDays = entry.distributedDays;
        this.calendarHelper = calendarHelper;
    }

    @Override
    public String toString() {
        String s = super.toString();
        s += "Distribution: ";

        List<LocalDate> distrbutedDates = calendarHelper.daysFromIndices(distributedDays);
        for (LocalDate i : distrbutedDates) {
            s += i + ", ";
        }
        return s;
    }
}
