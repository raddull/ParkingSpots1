package com.parkingspots;

import java.util.ArrayList;
import java.util.List;

class ParkingCalc {
    private final int numberOfWorkDays;
    private final int parkingSpots;

    ParkingCalc(int numberOfWorkDays, int parkingSpots) {
        this.numberOfWorkDays = numberOfWorkDays;
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
        int moreDays = numberOfWorkDays * parkingSpots - sumDays;
        while (moreDays > 0 ) {
            for (DedicatedEntry de : dedicatedEntries) {
                if (de.days + de.daysToIgnore.size() < numberOfWorkDays) {
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
        float minSpots = (numberOfWorkDays * availableParkingSpots / sumRatios);
        List<DedicatedEntry> dedicatedEntries = new ArrayList<>();
        List<DedicatedEntry> overlapEntries = new ArrayList<>();
        for (Entry entry : entries) {
            DedicatedEntry de = new DedicatedEntry(entry);
            de.days = (int) (entry.ratio * minSpots);
            if (de.days > (numberOfWorkDays - entry.daysToIgnore.size())) {
                de.days = (numberOfWorkDays - entry.daysToIgnore.size());
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

        int remainingDays = numberOfWorkDays;
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
            int from = numberOfWorkDays - remainingDays + 1;
            for (int targetedEmployeeNum = 0; targetedEmployeeNum < currentlyAvailableParkingSpots; targetedEmployeeNum++) {
                final DistributedEntry targetedEntry = entries.get(targetedEmployeeNum);
                if (targetedEntry.remainingDays <= 0) {
                    continue;
                }
                List<Integer> notFilledDays = fillDays(targetedEntry.distributedDays, from, from + stepDays - 1, targetedEntry.daysToIgnore);
                targetedEntry.remainingDays -= (stepDays - notFilledDays.size());
                //assign not filled days to employees that can use them
                for (Integer notFilledDay : notFilledDays) {
                    //start from employee that will not be included in this distribution
                    int nonTargetedEmployeeNum = currentlyAvailableParkingSpots;
                    while (entries.size() > nonTargetedEmployeeNum) {
                        final DistributedEntry nonTargetedEntry = entries.get(nonTargetedEmployeeNum);
                        if (!nonTargetedEntry.distributedDays.contains(notFilledDay)
                            && !nonTargetedEntry.daysToIgnore.contains(notFilledDay)
                            && nonTargetedEntry.remainingDays > 0) {
                            nonTargetedEntry.distributedDays.add(notFilledDay);
                            nonTargetedEntry.remainingDays -= 1;
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
