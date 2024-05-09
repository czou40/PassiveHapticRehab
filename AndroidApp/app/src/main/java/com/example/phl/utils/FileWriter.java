package com.example.phl.utils;

import android.content.Context;
import android.os.Environment;

import java.util.List;

public class FileWriter {
    public static void writeToCSV(String filename, List<String> names, List<List<Float>> data) {
        if (names.size() != data.size()) {
            throw new IllegalArgumentException("Names and data must be the same size");
        }
//        for (int i = 0; i < data.size(); i++) {
//            if (data.get(i).size() != data.get(0).size()) {
//                throw new IllegalArgumentException("All data must be the same size");
//            }
//        }
        int maxDataSize = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).size() > maxDataSize) {
                maxDataSize = data.get(i).size();
            }
        }
        try {
            // if directory doesn't exist, create it
            java.io.File directory = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/phl/");
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    System.out.println("Directory is created!");
                } else {
                    throw new Exception("Failed to create directory");
                }
            }
            System.out.println("Writing to file" + directory.getAbsolutePath() + "/" + filename);
            java.io.FileWriter writer = new java.io.FileWriter(directory.getAbsolutePath() + "/" + filename, false);
            for (int i = 0; i < names.size(); i++) {
                writer.write(names.get(i) + ",");
            }
            writer.write("\n");
            for (int i = 0; i < maxDataSize; i++) {
                for (int j = 0; j < data.size(); j++) {
                    if (i < data.get(j).size()) {
                        writer.write(data.get(j).get(i) + ",");
                    } else {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
            writer.close();
            System.out.println("Done writing to file");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}