package servent.message;

public class CommitMessage extends BasicMessage {

    public CommitMessage(int senderPort, int receiverPort, String txtDocument) {
        super(MessageType.COMMIT, senderPort, receiverPort, txtDocument);
    }
}
