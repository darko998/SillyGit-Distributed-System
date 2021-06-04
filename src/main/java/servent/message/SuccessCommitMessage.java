package servent.message;

public class SuccessCommitMessage extends BasicMessage {

    private int originalTargetPort;

    public SuccessCommitMessage(int senderPort, int receiverPort, String txtDocument, int originalTargetPort) {
        super(MessageType.SUCCESS_COMMIT, senderPort, receiverPort, txtDocument);

        this.originalTargetPort = originalTargetPort;
    }

    public int getOriginalTargetPort() {
        return originalTargetPort;
    }

    public void setOriginalTargetPort(int originalTargetPort) {
        this.originalTargetPort = originalTargetPort;
    }
}
