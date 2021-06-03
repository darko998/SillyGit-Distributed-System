package servent.message;

public class ConflictHappenedMessage extends BasicMessage {

    public ConflictHappenedMessage(int senderPort, int receiverPort, String txtDocument) {
        super(MessageType.CONFLICT_HAPPENED, senderPort, receiverPort, txtDocument);
    }
}
