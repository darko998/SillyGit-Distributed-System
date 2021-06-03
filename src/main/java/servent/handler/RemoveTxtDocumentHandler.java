package servent.handler;

import servent.message.Message;
import servent.message.MessageType;
import servent.message.RemoveTxtDocumentMessage;

public class RemoveTxtDocumentHandler implements MessageHandler {

    private Message clientMessage;

    public RemoveTxtDocumentHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.REMOVE_TXT_DOCUMENT) {
            RemoveTxtDocumentMessage removeTxtDocumentMessage = (RemoveTxtDocumentMessage)clientMessage;

            // todo
            /**
             * Pored ovoga, ameniti svugde original sender info i za commit proveriti je l folder
             */
        }
    }
}
