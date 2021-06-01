package app.document;

import app.AppConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class DocumentTxtIO {

    public static String read(String path) {

        StringBuilder str = new StringBuilder("");

        try {
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                str.append(myReader.nextLine());
            }

            myReader.close();
            //AppConfig.timestampedStandardPrint("Successfully read file!");
        } catch (FileNotFoundException e) {
            AppConfig.timestampedErrorPrint("An error occurred while reading the file!");
            e.printStackTrace();
        }

        return str.toString();
    }

    public static void write(String path, String data) {
        try {
            FileWriter myWriter = new FileWriter(path, false);
            myWriter.write(data);

            myWriter.close();
            //AppConfig.timestampedStandardPrint("Successfully wrote to the file.");
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("An error occurred while writing to the file!");
            e.printStackTrace();
        }
    }

    public static String getFileName(String path) {
        String[] array = path.split("\\\\");

        return array[array.length - 1];
    }
}
