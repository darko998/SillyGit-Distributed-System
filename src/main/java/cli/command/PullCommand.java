package cli.command;

import app.AppConfig;
import app.document.DocumentTxt;
import app.document.DocumentTxtIO;

public class PullCommand implements CLICommand {

    @Override
    public String commandName() {
        return "pull";
    }

    @Override
    public void execute(String args) {

        if(!AppConfig.chordState.amIAcceptAllCommands()) {
            DocumentTxt latestDocument = AppConfig.chordState.getLatestDocument();
            AppConfig.chordState.addDocumentVersion(latestDocument);

            String path = latestDocument.getPath();
            DocumentTxtIO.write(path, latestDocument.getData());

            AppConfig.timestampedStandardPrint("Uspesno prepisana lokalna datoteka sa poslednjom verzijom sa git-a.");

            AppConfig.chordState.acceptAllCommands();
            AppConfig.chordState.clearConflictsFromMap();
            AppConfig.chordState.setLatestDocument(null);
        } else {

            String[] argsArray = args.split(" ");

            if(argsArray.length < 1) {
                AppConfig.timestampedErrorPrint("You must enter at least file destination!");
                return;
            }

            String path = argsArray[0];
            int version = -1;

            if(argsArray.length == 2) {
                try {
                    version = Integer.parseInt(argsArray[1]);
                } catch (Exception e) {
                    AppConfig.timestampedErrorPrint("Version must be int number!");
                    return;
                }
            }

            if(argsArray.length > 2) {
                AppConfig.timestampedErrorPrint("Parameters for command 'pull' are not valid!");
                return;
            }

            AppConfig.chordState.pullTxtDocument(path, version, AppConfig.myServentInfo.getListenerPort());
        }
    }
}
