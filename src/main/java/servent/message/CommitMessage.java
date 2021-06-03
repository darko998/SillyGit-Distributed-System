package servent.message;

public class CommitMessage extends BasicMessage {

    private int originalSenderPort;

    public CommitMessage(int senderPort, int receiverPort, String txtDocument, int originalSenderPort) {
        super(MessageType.COMMIT, senderPort, receiverPort, txtDocument);

        this.originalSenderPort = originalSenderPort;
    }

    public int getOriginalSenderPort() {
        return originalSenderPort;
    }

    public void setOriginalSenderPort(int originalSenderPort) {
        this.originalSenderPort = originalSenderPort;
    }
}
