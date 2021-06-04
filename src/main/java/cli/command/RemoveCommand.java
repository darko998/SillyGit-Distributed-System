package cli.command;

import app.AppConfig;
import app.ServentInfo;
import servent.message.RemoveTxtDocumentMessage;
import servent.message.util.MessageUtil;

public class RemoveCommand implements CLICommand {

    @Override
    public String commandName() {
        return "remove";
    }

    @Override
    public void execute(String args) {
        String path = args;

        int documentChordId = AppConfig.chordState.chordHashTxtDocument(path);
        ServentInfo nextSucc = AppConfig.chordState.getNextNodeForKey(documentChordId);

        RemoveTxtDocumentMessage removeTxtDocumentMessageTmp = new RemoveTxtDocumentMessage(AppConfig.myServentInfo.getListenerPort(),
                nextSucc.getListenerPort(), path, false);
        MessageUtil.sendMessage(removeTxtDocumentMessageTmp);
    }
}
