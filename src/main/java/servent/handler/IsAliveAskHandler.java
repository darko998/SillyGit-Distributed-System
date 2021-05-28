package servent.handler;

import app.AppConfig;
import servent.message.IsAliveAskMessage;
import servent.message.IsAliveTellMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class IsAliveAskHandler implements MessageHandler {

    private Message clientMessage;

    public IsAliveAskHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.IS_ALIVE_ASK) {

            IsAliveAskMessage isAliveAskMessage = (IsAliveAskMessage)clientMessage;

            IsAliveTellMessage isAliveTellMessage;

            if(AppConfig.chordState.getAliveSerents().containsKey(isAliveAskMessage.getServentChordId())) {
                isAliveTellMessage = new IsAliveTellMessage(clientMessage.getReceiverPort(),
                        clientMessage.getSenderPort(), true, isAliveAskMessage.getServentChordId());
            } else {
                isAliveTellMessage = new IsAliveTellMessage(clientMessage.getReceiverPort(),
                        clientMessage.getSenderPort(), false, isAliveAskMessage.getServentChordId());
            }

            MessageUtil.sendMessage(isAliveTellMessage);
        }
    }
}
