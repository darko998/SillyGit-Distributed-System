package cli.command;

import app.AppConfig;
import app.ServentInfo;
import app.document.DocumentTxt;
import com.google.gson.Gson;
import servent.message.PushMessage;
import servent.message.PutMessage;
import servent.message.util.MessageUtil;

public class PushCommand implements CLICommand {

    @Override
    public String commandName() {
        return "push";
    }

    @Override
    public void execute(String args) {

        ServentInfo conflictNode = AppConfig.chordState.getConflictDocumentOwner();
        DocumentTxt conflictDocument = AppConfig.chordState.getConflictDocument();


        if(conflictNode != null && conflictDocument != null) {
            ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(conflictDocument.getChordId());

            if(nextNode.getChordId() != AppConfig.myServentInfo.getChordId()) {
                PushMessage pm = new PushMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), new Gson().toJson(conflictDocument), false);
                MessageUtil.sendMessage(pm);
            }
        }

        AppConfig.chordState.acceptAllCommands();
        AppConfig.chordState.clearConflictsFromMap();
    }
}