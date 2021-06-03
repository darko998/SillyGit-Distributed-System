package servent.message;

public class SuccessCommitMessage extends BasicMessage {

    public SuccessCommitMessage(int senderPort, int receiverPort, String txtDocument) {
        super(MessageType.SUCCESS_COMMIT, senderPort, receiverPort, txtDocument);
    }
}
