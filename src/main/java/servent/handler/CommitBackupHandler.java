package servent.handler;

import app.AppConfig;
import app.document.DocumentTxt;
import com.google.gson.Gson;
import servent.message.BackupTxtDocumentMessage;
import servent.message.CommitBackupMessage;
import servent.message.Message;
import servent.message.MessageType;

public class CommitBackupHandler implements MessageHandler {

    private Message clientMessage;

    public CommitBackupHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.COMMIT_BACKUP) {
            CommitBackupMessage commitBackupMessage = (CommitBackupMessage)clientMessage;
            DocumentTxt documentTxt = new Gson().fromJson(commitBackupMessage.getMessageText(), DocumentTxt.class);

            AppConfig.chordState.saveNewVersion(documentTxt);
        }
    }
}
