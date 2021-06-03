package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

import java.util.Scanner;

public class ConflictHappenedHandler implements MessageHandler {

    private Message clientMessage;

    public ConflictHappenedHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.CONFLICT_HAPPENED) {

            AppConfig.timestampedStandardPrint("Conflict happened! Enter 'view', 'push' or 'pull' to resolve the conflict!");
            AppConfig.chordState.acceptOnlyConflictCommands();
        }
    }
}
