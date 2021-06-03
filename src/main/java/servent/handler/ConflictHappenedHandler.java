package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import app.document.DocumentTxt;
import com.google.gson.Gson;
import servent.message.ConflictHappenedMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class ConflictHappenedHandler implements MessageHandler {

    private Message clientMessage;

    public ConflictHappenedHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.CONFLICT_HAPPENED) {

            ConflictHappenedMessage conflictHappenedMessage = (ConflictHappenedMessage)clientMessage;

            int nodeChordId = AppConfig.chordState.chordHash(conflictHappenedMessage.getOriginalSenderPort());
            DocumentTxt documentTxt = new Gson().fromJson(conflictHappenedMessage.getMessageText(), DocumentTxt.class);

            if(AppConfig.myServentInfo.getChordId() == nodeChordId) {

                AppConfig.chordState.addConflictInMap(conflictHappenedMessage.getSenderPort(), new Gson().toJson(documentTxt));
                AppConfig.chordState.setLatestDocument(new Gson().fromJson(conflictHappenedMessage.getLatestDocumentTxt(), DocumentTxt.class));

                AppConfig.timestampedStandardPrint("Conflict happened! Enter 'view', 'push' or 'pull' to resolve the conflict!");
                AppConfig.chordState.acceptOnlyConflictCommands();
            } else {
                ServentInfo receiver = AppConfig.chordState.getNextNodeForKey(nodeChordId);

                ConflictHappenedMessage conflictHappenedMessageTmp = new ConflictHappenedMessage(AppConfig.myServentInfo.getListenerPort(),
                        receiver.getListenerPort(), new Gson().toJson(documentTxt), conflictHappenedMessage.getOriginalSenderPort(), conflictHappenedMessage.getLatestDocumentTxt());
                MessageUtil.sendMessage(conflictHappenedMessageTmp);
            }

        }
    }
}
