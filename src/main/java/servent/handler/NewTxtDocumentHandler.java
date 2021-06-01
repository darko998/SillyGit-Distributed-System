package servent.handler;

import app.AppConfig;
import app.document.DocumentTxt;
import app.document.DocumentTxtIO;
import com.google.gson.Gson;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewTxtDocumentMessage;

public class NewTxtDocumentHandler implements MessageHandler {

    private Message clientMessage;

    public NewTxtDocumentHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.NEW_TXT_DOCUMENT) {
            NewTxtDocumentMessage newTxtDocumentMessage = (NewTxtDocumentMessage)clientMessage;
            DocumentTxt documentTxt = new Gson().fromJson(newTxtDocumentMessage.getMessageText(), DocumentTxt.class);

            AppConfig.chordState.addNewTxtDocument(documentTxt);
        }
    }
}
