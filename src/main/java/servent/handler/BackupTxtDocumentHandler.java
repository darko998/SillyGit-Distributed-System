package servent.handler;

import app.AppConfig;
import app.document.DocumentTxt;
import com.google.gson.Gson;
import servent.message.BackupTxtDocumentMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewTxtDocumentMessage;

public class BackupTxtDocumentHandler implements MessageHandler {

    private Message clientMessage;

    public BackupTxtDocumentHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.BACKUP_TXT_DOCUMENT) {
            BackupTxtDocumentMessage backupTxtDocumentMessage = (BackupTxtDocumentMessage)clientMessage;
            DocumentTxt documentTxt = new Gson().fromJson(backupTxtDocumentMessage.getMessageText(), DocumentTxt.class);

            AppConfig.chordState.saveTxtDocumentToRepository(documentTxt);
        }
    }
}
