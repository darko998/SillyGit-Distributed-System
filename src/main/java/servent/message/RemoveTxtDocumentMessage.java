package servent.message;

public class RemoveTxtDocumentMessage extends BasicMessage {

    private boolean toNodeWhichIsNotOwner;

    public RemoveTxtDocumentMessage(int senderPort, int receiverPort, String path, boolean toNodeWhichIsNotOwner) {
        super(MessageType.REMOVE_TXT_DOCUMENT, senderPort, receiverPort, path);

        this.toNodeWhichIsNotOwner = toNodeWhichIsNotOwner;
    }

    public boolean isToNodeWhichIsNotOwner() {
        return toNodeWhichIsNotOwner;
    }

    public void setToNodeWhichIsNotOwner(boolean toNodeWhichIsNotOwner) {
        this.toNodeWhichIsNotOwner = toNodeWhichIsNotOwner;
    }
}
