package app.document;

import app.AppConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class DocumentTxtIO {

    public static String read(String path) {

        if(!path.toLowerCase().contains("repository")) {
            path = AppConfig.myServentInfo.getWorkingRootPath() + "\\" + path;
        }

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
        if(!path.toLowerCase().contains("repository")) {
            path = AppConfig.myServentInfo.getWorkingRootPath() + "\\" + path;
        }

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

    public static void writeIfNotExists(String path, String data) {
        if(!path.toLowerCase().contains("repository")) {
            path = AppConfig.myServentInfo.getWorkingRootPath() + "\\" + path;
        }

        if(path.contains("\\")) {
            String folder = path.substring(0, path.lastIndexOf("\\"));
            //String document = path.substring(path.lastIndexOf("\\"), path.length());

            File dir = new File(folder);

            if(!dir.exists()) {
                dir.mkdirs();
            }

            try {
                FileWriter myWriter = new FileWriter(path, false);
                myWriter.write(data);

                myWriter.close();
                //AppConfig.timestampedStandardPrint("Successfully wrote to the file.");
            } catch (IOException e) {
                AppConfig.timestampedErrorPrint("An error occurred while writing to the file!");
                e.printStackTrace();
            }
        } else {
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

    }

    public static String getFileName(String path) {
        String[] array = path.split("\\\\");

        return array[array.length - 1];
    }

    public static String createTemporaryFile(DocumentTxt documentTxt) {


        String path = "FileForView" + System.currentTimeMillis() + ".txt";

        write(path, documentTxt.getData());

        return path;
    }
}
