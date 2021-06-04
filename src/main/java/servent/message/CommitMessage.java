package servent.message;

public class CommitMessage extends BasicMessage {

    private int originalSenderPort;
    private boolean isDir;

    public CommitMessage(int senderPort, int receiverPort, String txtDocument, int originalSenderPort, boolean isDir) {
        super(MessageType.COMMIT, senderPort, receiverPort, txtDocument);

        this.originalSenderPort = originalSenderPort;
        this.isDir = isDir;
    }

    public int getOriginalSenderPort() {
        return originalSenderPort;
    }

    public void setOriginalSenderPort(int originalSenderPort) {
        this.originalSenderPort = originalSenderPort;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }
}
