package servent.message;

public class PullResultMessage extends BasicMessage {

    private boolean findDocument;
    private int targetPort;

    public PullResultMessage(int senderPort, int receiverPort, String txtDocument, boolean findDocument, int targetPort) {
        super(MessageType.PULL_RESULT, senderPort, receiverPort, txtDocument);

        this.findDocument = findDocument;
        this.targetPort = targetPort;
    }


    public boolean isFindDocument() {
        return findDocument;
    }

    public void setFindDocument(boolean findDocument) {
        this.findDocument = findDocument;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }
}
