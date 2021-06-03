package servent.message;

public class PullMessage extends BasicMessage {

    private String path;
    private int version;
    private int originalSenderPort;

    public PullMessage(int senderPort, int receiverPort, String path, int version, int originalSenderPort) {
        super(MessageType.PULL, senderPort, receiverPort, "");

        this.path = path;
        this.version = version;
        this.originalSenderPort = originalSenderPort;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getOriginalSenderPort() {
        return originalSenderPort;
    }

    public void setOriginalSenderPort(int originalSenderPort) {
        this.originalSenderPort = originalSenderPort;
    }
}
