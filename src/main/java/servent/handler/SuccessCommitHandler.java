package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import app.document.DocumentTxt;
import com.google.gson.Gson;
import servent.message.*;
import servent.message.util.MessageUtil;

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

            if(successCommitMessage.getOriginalTargetPort() == AppConfig.myServentInfo.getListenerPort()) {

                AppConfig.chordState.addDocumentVersion(documentTxt);
            } else {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(documentTxt.getChordId());

                if(nextNode.getChordId() != AppConfig.myServentInfo.getChordId()) {
                    SuccessCommitMessage successCommitMessageTmp = new SuccessCommitMessage(AppConfig.myServentInfo.getListenerPort(),
                            nextNode.getListenerPort(), new Gson().toJson(documentTxt), successCommitMessage.getOriginalTargetPort());
                    MessageUtil.sendMessage(successCommitMessageTmp);
                }
            }

        }
    }
}
