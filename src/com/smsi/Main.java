package com.smsi;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<String[]> brojParkingMesta = CSVReader.readFromCSV("numberOfParkingSpots.csv");
        int parkingSpots = Integer.parseInt(brojParkingMesta.get(0)[0]);

        List<String[]> brojDanaUMesecu = CSVReader.readFromCSV("numberOfDaysInMonth.csv");
        int daysInMonth = Integer.parseInt(brojDanaUMesecu.get(0)[0]);

        ParkingCalc pc = new ParkingCalc(daysInMonth, parkingSpots);

        List<ParkingCalc.Entry> entries = new ArrayList<>();
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
                entries.add(ParkingCalc.Entry.createEntry(elem[0], Float.parseFloat(elem[1]), elem[2]));
            }
            else {
                entries.add(new ParkingCalc.Entry(elem[0], Float.parseFloat(elem[1])));
            }
        }

        List<ParkingCalc.DedicatedEntry> dedicatedEntries = pc.calcDaysPerRatios(entries);
        List<ParkingCalc.DistributedEntry> distributedEntries = pc.distributeCalculatedDays(dedicatedEntries);
        for (ParkingCalc.DistributedEntry dise : distributedEntries) {
            System.out.println(dise);
        }
    }
}
