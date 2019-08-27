package com.parkingspots;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        List<String[]> numberOfParkingSpots = CSVReader.readFromCSV("numberOfParkingSpots.csv");
        int parkingSpots = Integer.parseInt(numberOfParkingSpots.get(0)[0]);

        List<String[]> targetDate = CSVReader.readFromCSV("targetDate.csv");
        LocalDate startDate = LocalDate.parse(targetDate.get(0)[0]);

        CalendarHelper calendarHelper = new CalendarHelper(startDate);
        int workDaysInMonth = calendarHelper.numberOfWorkDaysInMonth();

        ParkingCalc pc = new ParkingCalc(workDaysInMonth, parkingSpots);

        List<Entry> entries = new ArrayList<>();
        String employeeListFile = "employees.csv";
        List<String[]> lista = CSVReader.readFromCSV(employeeListFile);
        for (String[] elem : lista) {
            if (elem.length < 2) {
                throw new RuntimeException("Bad input file: " + employeeListFile);
            }
            //elem[0] -- name
            //elem[1] -- ratio
            //elem[2] -- days to ignore
            if (elem.length == 3) {
                entries.add(createEntry(elem[0], Float.parseFloat(elem[1]), elem[2], calendarHelper));
            }
            else {
                entries.add(new Entry(elem[0], Float.parseFloat(elem[1])));
            }
        }

        List<DedicatedEntry> dedicatedEntries = pc.calcDaysPerRatios(entries);
        List<DistributedEntry> distributedEntries = pc.distributeCalculatedDays(dedicatedEntries);
        for (DistributedEntry dise : distributedEntries) {
            CalendarEntry calendarEntry = new CalendarEntry(dise, calendarHelper);
            System.out.println(calendarEntry);
        }
    }

    static final Entry createEntry(String name, float ratio, String ignoreDays, CalendarHelper calendarHelper) {
        if (ignoreDays == null || ignoreDays.length() == 0) {
            return new Entry(name, ratio);
        }
        List<LocalDate> ignoredDays = Arrays.stream(ignoreDays.split(":")).map(item -> LocalDate.parse(item)).collect(Collectors.toList());
        List<Integer> ignoredIndices = calendarHelper.indicesFromDays(ignoredDays);
        return new Entry(name, ratio, ignoredIndices);
    }
}
