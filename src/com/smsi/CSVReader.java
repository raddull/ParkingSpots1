package com.smsi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
        public static List<String[]> readFromCSV(String csvFile) {
            String line = "";
            String cvsSplitBy = ",";

            ArrayList<String[]> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                while ((line = br.readLine()) != null) {
                    String[] splited = line.split(cvsSplitBy);
                    lines.add(splited);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lines;
        }

    }
