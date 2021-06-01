package servent.message;

public class NewTxtDocumentMessage extends BasicMessage {

    public NewTxtDocumentMessage(int senderPort, int receiverPort, String txtDocument) {
        super(MessageType.NEW_TXT_DOCUMENT, senderPort, receiverPort, txtDocument);
    }
}
