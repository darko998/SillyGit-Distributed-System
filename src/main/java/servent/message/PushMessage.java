package servent.message;

public class PushMessage extends BasicMessage {

    private boolean toNodeWhichIsNotOwner;

    public PushMessage(int senderPort, int receiverPort, String txtDocument, boolean toNodeWhichIsNotOwner) {
        super(MessageType.PUSH, senderPort, receiverPort, txtDocument);

        this.toNodeWhichIsNotOwner = toNodeWhichIsNotOwner;
    }

    public boolean isToNodeWhichIsNotOwner() {
        return toNodeWhichIsNotOwner;
    }

    public void setToNodeWhichIsNotOwner(boolean toNodeWhichIsNotOwner) {
        this.toNodeWhichIsNotOwner = toNodeWhichIsNotOwner;
    }
}
