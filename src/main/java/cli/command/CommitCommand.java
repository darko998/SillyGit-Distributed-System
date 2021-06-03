package cli.command;

import app.AppConfig;
import app.document.DocumentTxt;
import app.document.DocumentTxtIO;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CommitCommand implements CLICommand {

    @Override
    public String commandName() {
        return "commit";
    }

    @Override
    public void execute(String args) {

        String filePath = AppConfig.myServentInfo.getWorkingRootPath() + "\\" + args;

        if(checkIfFileExists(filePath)) {


            if(isDirectory(filePath)) {

                // Ovde izvlacimo ime foldera i njega hesiramo. Kasnije sve datoteke koje se nalaze unutar njega saljemo sa njegovim chord id-em
                String[] folders = args.split("\\\\");
                String folderName = folders[folders.length - 1];
                int folderNameChordId = AppConfig.chordState.chordHashTxtDocument(folderName);

                List<String> fileNames = new ArrayList<>();
                List<String> tmp = getFileNames(fileNames, Paths.get(filePath));

                for (int i = 0; i < tmp.size(); i++) {
                    String tmpFilePath = tmp.get(i);
                    String documentData = DocumentTxtIO.read(tmpFilePath);
                    DocumentTxt documentTxt = new DocumentTxt(folderNameChordId, tmpFilePath, documentData, AppConfig.chordState.getCurrDocumentVersion(tmpFilePath));

                    // -1 nam je znak da saljemo
                    AppConfig.chordState.commit(documentTxt, AppConfig.myServentInfo.getListenerPort());
                }
            } else {
                String documentData = DocumentTxtIO.read(filePath);
                DocumentTxt documentTxt = new DocumentTxt(AppConfig.chordState.chordHashTxtDocument(filePath), filePath, documentData, AppConfig.chordState.getCurrDocumentVersion(filePath));

                AppConfig.chordState.commit(documentTxt, AppConfig.myServentInfo.getListenerPort());
            }
        } else {
            AppConfig.timestampedErrorPrint("File " + filePath + " don't exists");
        }
    }

    private List<String> getFileNames(List<String> fileNames, Path dir) {
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if(path.toFile().isDirectory()) {
                    getFileNames(fileNames, path);
                } else {
                    fileNames.add(path.toAbsolutePath().toString());
                    System.out.println(path.getFileName());
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }

    public boolean checkIfFileExists(String filePath) {
        File f = new File(filePath);

        if(f.exists()){
            return true;
        }

        return false;
    }

    public boolean isDirectory(String filePath) {
        File f = new File(filePath);

        if(f.isDirectory()) {
            return true;
        }

        return false;
    }
}
