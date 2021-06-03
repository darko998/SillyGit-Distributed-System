package servent.handler;

import app.AppConfig;
import app.document.DocumentTxt;
import com.google.gson.Gson;
import servent.message.*;

public class SuccessCommitHandler implements MessageHandler {

    private Message clientMessage;

    public SuccessCommitHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.SUCCESS_COMMIT) {
            SuccessCommitMessage successCommitMessage = (SuccessCommitMessage)clientMessage;
            DocumentTxt documentTxt = new Gson().fromJson(successCommitMessage.getMessageText(), DocumentTxt.class);

            AppConfig.chordState.addDocumentVersion(documentTxt);
        }
    }
}
