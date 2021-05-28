package servent.message;

public class PongMessage extends BasicMessage{

    private int serventChordId;

    public PongMessage(int senderPort, int receiverPort, int serventChordId) {
        super(MessageType.PONG, senderPort, receiverPort);

        this.serventChordId = serventChordId;
    }

    public int getServentChordId() {
        return serventChordId;
    }

    public void setServentChordId(int serventChordId) {
        this.serventChordId = serventChordId;
    }
}
