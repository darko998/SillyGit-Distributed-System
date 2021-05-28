package servent.message;

public class IsAliveTellMessage extends BasicMessage {

    private boolean isAlive = false;
    private int serventChordId;

    public IsAliveTellMessage(int senderPort, int receiverPort, boolean isAlive, int serventChordId) {
        super(MessageType.IS_ALIVE_TELL, senderPort, receiverPort);

        this.isAlive = isAlive;
        this.serventChordId = serventChordId;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public int getServentChordId() {
        return serventChordId;
    }

    public void setServentChordId(int serventChordId) {
        this.serventChordId = serventChordId;
    }
}