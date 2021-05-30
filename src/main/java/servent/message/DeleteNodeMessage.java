package servent.message;

public class DeleteNodeMessage extends BasicMessage {

    private int originalSenderPort;
    private int deleteNodePort;

    public DeleteNodeMessage(int senderPort, int receiverPort, int originalSenderPort, int deleteNodePort) {
        super(MessageType.DELETE_NODE, senderPort, receiverPort);

        this.originalSenderPort = originalSenderPort;
        this.deleteNodePort = deleteNodePort;
    }

    public int getOriginalSenderPort() {
        return originalSenderPort;
    }

    public void setOriginalSenderPort(int originalSenderPort) {
        this.originalSenderPort = originalSenderPort;
    }

    public int getDeleteNodePort() {
        return deleteNodePort;
    }

    public void setDeleteNodePort(int deleteNodePort) {
        this.deleteNodePort = deleteNodePort;
    }
}
