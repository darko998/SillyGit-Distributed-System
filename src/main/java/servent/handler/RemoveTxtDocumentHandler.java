package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.RemoveTxtDocumentMessage;
import servent.message.util.MessageUtil;

public class RemoveTxtDocumentHandler implements MessageHandler {

    private Message clientMessage;

    public RemoveTxtDocumentHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.REMOVE_TXT_DOCUMENT) {
            RemoveTxtDocumentMessage removeTxtDocumentMessage = (RemoveTxtDocumentMessage)clientMessage;
            String path = removeTxtDocumentMessage.getMessageText();
            int documentChordId = AppConfig.chordState.chordHashTxtDocument(path);

            if(AppConfig.chordState.isKeyMine(documentChordId) || removeTxtDocumentMessage.isToNodeWhichIsNotOwner()) {
                AppConfig.chordState.removeDocumentByPath(path, removeTxtDocumentMessage.isToNodeWhichIsNotOwner());
            } else {
                ServentInfo nextSucc = AppConfig.chordState.getNextNodeForKey(documentChordId);

                RemoveTxtDocumentMessage removeTxtDocumentMessageTmp = new RemoveTxtDocumentMessage(AppConfig.myServentInfo.getListenerPort(),
                        nextSucc.getListenerPort(), removeTxtDocumentMessage.getMessageText(), false);
                MessageUtil.sendMessage(removeTxtDocumentMessageTmp);
            }
        }
    }
}
