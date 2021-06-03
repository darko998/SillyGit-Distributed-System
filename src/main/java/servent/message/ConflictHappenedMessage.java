package servent.message;

public class ConflictHappenedMessage extends BasicMessage {

    private int originalSenderPort;
    private String latestDocumentTxt;

    public ConflictHappenedMessage(int senderPort, int receiverPort, String txtDocument, int originalSenderInfo, String latestDocumentTxt) {
        super(MessageType.CONFLICT_HAPPENED, senderPort, receiverPort, txtDocument);

        this.originalSenderPort = originalSenderInfo;
        this.latestDocumentTxt = latestDocumentTxt;
    }

    public int getOriginalSenderPort() {
        return originalSenderPort;
    }

    public void setOriginalSenderPort(int originalSenderPort) {
        this.originalSenderPort = originalSenderPort;
    }

    public String getLatestDocumentTxt() {
        return latestDocumentTxt;
    }

    public void setLatestDocumentTxt(String latestDocumentTxt) {
        this.latestDocumentTxt = latestDocumentTxt;
    }
}
