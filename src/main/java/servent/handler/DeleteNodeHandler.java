package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.DeleteNodeMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class DeleteNodeHandler implements MessageHandler {

    private Message clientMessage;

    public DeleteNodeHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.DELETE_NODE) {
            DeleteNodeMessage deleteNodeMessage = (DeleteNodeMessage)clientMessage;

            if(deleteNodeMessage.getOriginalSenderPort() == AppConfig.myServentInfo.getListenerPort()) {
                ServentInfo nodeForDelete = new ServentInfo("localhost", deleteNodeMessage.getDeleteNodePort());

                AppConfig.timestampedStandardPrint("opaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                AppConfig.chordState.deleteNode(nodeForDelete);

            } else {
                ServentInfo nodeForDelete = new ServentInfo("localhost", deleteNodeMessage.getDeleteNodePort());
                if(nodeForDelete != null) {
                    AppConfig.chordState.deleteNode(nodeForDelete);
                } else {
                    AppConfig.timestampedErrorPrint("That node is already deleted!");
                }

                // Ovde setujemo novog prethodnika, jer se cvor koji je obrisan nalazio izmedju ovog cvora i cvora koji je poslao ovu poruku.
                if(deleteNodeMessage.getOriginalSenderPort() == deleteNodeMessage.getSenderPort()) {
                    AppConfig.chordState.setPredecessor(new ServentInfo("localhost", deleteNodeMessage.getOriginalSenderPort()));
                }

                ServentInfo nextSucc = AppConfig.chordState.findNextSuccessor(AppConfig.myServentInfo.getChordId());

                if(nextSucc != null) {
                    DeleteNodeMessage newDeleteNodeMessage = new DeleteNodeMessage(AppConfig.myServentInfo.getListenerPort(),
                            nextSucc.getListenerPort(), deleteNodeMessage.getOriginalSenderPort(), deleteNodeMessage.getDeleteNodePort());

                    MessageUtil.sendMessage(newDeleteNodeMessage);
                }
            }
        }
    }
}
