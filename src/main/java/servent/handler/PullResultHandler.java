package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import app.document.DocumentTxt;
import app.document.DocumentTxtIO;
import com.google.gson.Gson;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PullResultMessage;
import servent.message.util.MessageUtil;

public class PullResultHandler implements MessageHandler {

    private Message clientMessage;

    public PullResultHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.PULL_RESULT) {
            PullResultMessage pullResultMessage = (PullResultMessage)clientMessage;

            ServentInfo targetServent = new ServentInfo(AppConfig.myServentInfo.getIpAddress(), pullResultMessage.getTargetPort());

            if(targetServent.getChordId() == AppConfig.myServentInfo.getChordId()) {
                if(pullResultMessage.isFindDocument()) {
                    DocumentTxt pulledDocument = new Gson().fromJson(pullResultMessage.getMessageText(), DocumentTxt.class);

                    DocumentTxtIO.writeIfNotExists(pulledDocument.getPath(), pulledDocument.getData());
                    AppConfig.chordState.addDocumentVersion(pulledDocument);

                    AppConfig.timestampedStandardPrint("Uspesno pull-ovana datoteka!");
                } else {
                    AppConfig.timestampedStandardPrint("Trazena datoteka ne postoji u sistemu!");
                }
            } else {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(targetServent.getChordId());

                PullResultMessage pullResultMessageTmp = new PullResultMessage(AppConfig.myServentInfo.getListenerPort(),
                        nextNode.getListenerPort(), pullResultMessage.getMessageText(), pullResultMessage.isFindDocument(), pullResultMessage.getTargetPort());
                MessageUtil.sendMessage(pullResultMessageTmp);
            }
        }
    }
}
