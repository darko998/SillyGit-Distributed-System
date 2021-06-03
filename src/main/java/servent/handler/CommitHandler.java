package servent.handler;

import app.AppConfig;
import app.document.DocumentTxt;
import com.google.gson.Gson;
import servent.message.*;

public class CommitHandler implements MessageHandler {

    private Message clientMessage;

    public CommitHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.COMMIT) {
            CommitMessage commitMessage = (CommitMessage)clientMessage;
            DocumentTxt documentTxt = new Gson().fromJson(commitMessage.getMessageText(), DocumentTxt.class);

            if(AppConfig.chordState.commit(documentTxt, commitMessage.getOriginalSenderPort())) {
                SuccessCommitMessage successCommitMessage = new SuccessCommitMessage(AppConfig.myServentInfo.getListenerPort(), commitMessage.getOriginalSenderPort(), "");
            }
        }
    }
}
