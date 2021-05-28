package servent.handler;

import app.AppConfig;
import servent.message.IsAliveAskMessage;
import servent.message.IsAliveTellMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class IsAliveTellHandler implements MessageHandler {

    private Message clientMessage;

    public IsAliveTellHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.IS_ALIVE_TELL) {
            IsAliveTellMessage isAliveTellMessage = (IsAliveTellMessage)clientMessage;

            if(isAliveTellMessage.isAlive()) {
                AppConfig.chordState.removeFromSuspicious(isAliveTellMessage.getServentChordId());
            } else {
                AppConfig.chordState.addSuspiciousCheckedByOtherServent(isAliveTellMessage.getServentChordId());
            }
        }
    }
}