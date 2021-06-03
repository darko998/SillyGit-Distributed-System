package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import app.document.DocumentTxt;
import com.google.gson.Gson;
import servent.message.*;
import servent.message.util.MessageUtil;

public class PushHandler implements MessageHandler {

    private Message clientMessage;

    public PushHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.PUSH) {
            PushMessage pushMessage = (PushMessage)clientMessage;

            DocumentTxt documentTxt = new Gson().fromJson(pushMessage.getMessageText(), DocumentTxt.class);
            if(!pushMessage.isToNodeWhichIsNotOwner()) { // Poruka nije redirektovana na sledbenika ili prethodnika, vec trazimo originalnog vlasnika dokumenta

                if(AppConfig.chordState.isKeyMine(documentTxt.getChordId())) {
                    AppConfig.chordState.push(documentTxt, true);
                } else {
                    ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(documentTxt.getChordId());

                    PushMessage pushMessageTmp = new PushMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), new Gson().toJson(documentTxt), false);
                    MessageUtil.sendMessage(pushMessageTmp);
                }
            } else {
                AppConfig.chordState.push(documentTxt, false);
            }

        }
    }
}
