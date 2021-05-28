package servent.message;

public class IsAliveAskMessage extends BasicMessage {

    private int serventChordId; // Chord id serventa koji nas interesuje da li je ziv

    public IsAliveAskMessage(int senderPort, int receiverPort, int serventChordId) {
        super(MessageType.IS_ALIVE_ASK, senderPort, receiverPort);

        this.serventChordId = serventChordId;
    }

    public int getServentChordId() {
        return serventChordId;
    }

    public void setServentChordId(int serventChordId) {
        this.serventChordId = serventChordId;
    }
}