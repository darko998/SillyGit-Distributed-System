package servent.message;

public class RemoveTxtDocumentMessage extends BasicMessage {

    public RemoveTxtDocumentMessage(int senderPort, int receiverPort, String txtDocument, boolean toNodeWhichIsNotOwner) {
        super(MessageType.REMOVE_TXT_DOCUMENT, senderPort, receiverPort, txtDocument);

    }
}
