package com.smsi;

import java.util.*;
import java.util.stream.Collectors;

class ParkingCalc {
    private final int daysInMonth;
    private final int parkingSpots;

    static class Entry {
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

        static final Entry createEntry(String name, float ratio, String ignoreDays) {
            if (ignoreDays == null || ignoreDays.length() == 0) {
                return new Entry(name, ratio);
            }
            List<Integer> ignores = Arrays.stream(ignoreDays.split(":")).map(item -> Integer.parseInt(item)).collect(Collectors.toList());
            return new Entry(name, ratio, ignores);
        }
    }

    public class DedicatedEntry extends Entry {
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
            return "Person " + name + " with ratio " + ratio + " has " + days + " days in month and " + extraDays + " extra days and " + remainingDays + " remaining days. ";
        }
    }

    public class DistributedEntry extends DedicatedEntry {
        List<Integer> distributedDays = new ArrayList<>();

        DistributedEntry(DedicatedEntry entry) {
            super(entry);
        }

        @Override
        public String toString() {
            String s = super.toString();
            s += "Distributed Days: ";
            for (Integer i : distributedDays) {
                s += i + ", ";
            }
            return s;
        }
    }

    ParkingCalc(int daysInMonth, int parkingSpots) {
        this.daysInMonth = daysInMonth;
        this.parkingSpots = parkingSpots;
    }

    // Calculate a number of days each employee has, based on employee ratio
    // This method also adds extra days to fill the void's.
    // For example, if 39 out of 40 days are dedicated, 1 empty day will be added randomly and logged as extra day
    List<DedicatedEntry> calcDaysPerRatios(List<Entry> entries) {
        List<DedicatedEntry> dedicatedEntries = calcDaysPerRatios(entries, parkingSpots);
        int sumDays = 0;
        for (DedicatedEntry de : dedicatedEntries) {
            sumDays += de.days;
        }
        int moreDays = daysInMonth * parkingSpots - sumDays;
        if (moreDays > 0 ) {
            for (DedicatedEntry de : dedicatedEntries) {
                if (de.days < daysInMonth) {
                    de.days += 1;
                    de.extraDays += 1;
                    moreDays--;
                    if (moreDays == 0) {
                        break;
                    }
                }
            }
        }
        for (DedicatedEntry de : dedicatedEntries) {
            de.remainingDays = de.days;
        }
        return dedicatedEntries;
    }

    // This presents raw calculation of days per ratios
    private List<DedicatedEntry> calcDaysPerRatios(List<Entry> entries, int availableParkingSpots) {
        if (entries == null) {
            return null;
        }
        float sumRatios = 0;
        for (Entry entry : entries) {
            sumRatios += entry.ratio;
        }
        //minSpots is the number of parking days a person with ratio 1.0 will have
        float minSpots = (daysInMonth * availableParkingSpots / sumRatios);
        List<DedicatedEntry> dedicatedEntries = new ArrayList<>();
        List<DedicatedEntry> overlapEntries = new ArrayList<>();
        for (Entry entry : entries) {
            DedicatedEntry de = new DedicatedEntry(entry);
            de.days = (int) (entry.ratio * minSpots);
            if (de.days > (daysInMonth - entry.daysToIgnore.size())) {
                de.days = (daysInMonth - entry.daysToIgnore.size());
                overlapEntries.add(de);
            }
            else {
                dedicatedEntries.add(de);
            }
        }
        if (overlapEntries.size() > 0) {
            List<Entry> tmpList = new ArrayList<>();
            tmpList.addAll(dedicatedEntries);
            dedicatedEntries = calcDaysPerRatios(tmpList, availableParkingSpots - overlapEntries.size());
            if (!dedicatedEntries.addAll(overlapEntries)) {
                System.out.println("ERROR: failed to combine entries");
            }
        }
        return dedicatedEntries;
    }

    List<DistributedEntry> distributeCalculatedDays(List<DedicatedEntry> dedicatedEntries) {
        return distributeCalculatedDays(dedicatedEntries, parkingSpots);
    }

    //Based on number of parking days each employee has, distribute these days over month and avoid collisions with other employees
    private List<DistributedEntry> distributeCalculatedDays(final List<DedicatedEntry> dedicatedEntries, final int availableParkingSpots) {
        if (dedicatedEntries == null) {
            return null;
        }
        int currentlyAvailableParkingSpots = availableParkingSpots;
        if (currentlyAvailableParkingSpots > dedicatedEntries.size()) {
            currentlyAvailableParkingSpots = dedicatedEntries.size();
        }
        List<DistributedEntry> entries = new ArrayList<>();
        for (DedicatedEntry dedicatedEntry : dedicatedEntries) {
            entries.add(new DistributedEntry(dedicatedEntry));
        }

        int remainingDays = daysInMonth;
        while (remainingDays > 0) {
            entries.sort((o1, o2) -> o2.remainingDays - o1.remainingDays);
            int stepDays = Math.max(5, entries.get(0).remainingDays);
            int futureRemainingDays = remainingDays - stepDays;
            for (DistributedEntry de : entries) {
                if (de.remainingDays > futureRemainingDays) {
                    stepDays -= de.remainingDays - futureRemainingDays;
                    if (stepDays <= 0) {
                        stepDays = 1;
                    }
                    futureRemainingDays = remainingDays - stepDays;
                }
            }
            int from = daysInMonth - remainingDays + 1;
            for (int targetedEmployeeNum = 0; targetedEmployeeNum < currentlyAvailableParkingSpots; targetedEmployeeNum++) {
                List<Integer> notFilledDays = fillDays(entries.get(targetedEmployeeNum).distributedDays, from, from + stepDays - 1, entries.get(targetedEmployeeNum).daysToIgnore);
                entries.get(targetedEmployeeNum).remainingDays -= (stepDays - notFilledDays.size());
                //assign not filled days to employees that can use them
                for (Integer notFilledDay : notFilledDays) {
                    //start from employee that will not be included in this distribution
                    int nonTargetedEmployeeNum = currentlyAvailableParkingSpots;
                    while (entries.size() >= nonTargetedEmployeeNum) {
                        if (!entries.get(nonTargetedEmployeeNum).distributedDays.contains(notFilledDay)) {
                            entries.get(nonTargetedEmployeeNum).distributedDays.add(notFilledDay);
                            entries.get(nonTargetedEmployeeNum).remainingDays -= 1;
                            break;
                        }
                        nonTargetedEmployeeNum++;
                    }
                }
            }
            remainingDays -= stepDays;
        }
        entries.sort((o1, o2) -> (int)(10*o1.ratio - 10*o2.ratio));
        return entries;
    }

    private List<Integer> fillDays(List<Integer> listToFill, int from, int to, List<Integer> daysToIgnore) {
        ArrayList<Integer> notFilledDaysNum = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            if (daysToIgnore.contains(i)) {
                notFilledDaysNum.add(i);
            }
            else {
                listToFill.add(i);
            }
        }
        return notFilledDaysNum;
    }
}
