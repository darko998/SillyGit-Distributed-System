package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PullMessage;

public class PullHandler implements MessageHandler {

    private Message clientMessage;

    public PullHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.PULL) {
            PullMessage pullMessage = (PullMessage)clientMessage;

            AppConfig.chordState.pullTxtDocument(pullMessage.getPath(), pullMessage.getVersion(), pullMessage.getOriginalSenderPort());
        }
    }
}
