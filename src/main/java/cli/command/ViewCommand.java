package cli.command;

import app.AppConfig;
import app.document.DocumentTxtIO;

public class ViewCommand implements CLICommand {

    @Override
    public String commandName() {
        return "view";
    }

    @Override
    public void execute(String args) {

        String createdFileLocation = DocumentTxtIO.createTemporaryFile(AppConfig.chordState.getLatestDocument());

        AppConfig.timestampedStandardPrint("Radi ispitivanja sadrzaja fajl je kreiran na lokaciji " + createdFileLocation);
        AppConfig.timestampedStandardPrint("Enter 'view', 'push' or 'pull' to resolve conflict!");
    }
}
