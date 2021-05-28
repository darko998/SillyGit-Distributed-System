package cli.command;

import app.AppConfig;
import app.ChordState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AddFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "add";
    }

    @Override
    public void execute(String args) {

        String filePath = AppConfig.myServentInfo.getWorkingRootPath() + "\\" + args;

        if(checkIfFileExists(filePath)) {
            String content = readFileAsString(filePath);

            AppConfig.timestampedStandardPrint("Printing file content..." + content);

            AppConfig.timestampedStandardPrint(content);
        } else {
            AppConfig.timestampedErrorPrint("File " + filePath + " don't exists");
        }
    }

    public boolean checkIfFileExists(String filePath) {
        File f = new File(filePath);
        
        if(f.exists()){
            return true;
        }
        
        return false;
    }
    
    public String readFileAsString(String filePath){
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            AppConfig.timestampedErrorPrint("Error occurred while reading the file " + filePath + " Error: " + e.getMessage());

        }

        return data;
    }
}