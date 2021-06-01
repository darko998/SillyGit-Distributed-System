package servent.message;

public class BackupTxtDocumentMessage extends BasicMessage {

    public BackupTxtDocumentMessage(int senderPort, int receiverPort, String txtDocument) {
        super(MessageType.BACKUP_TXT_DOCUMENT, senderPort, receiverPort, txtDocument);
    }
}
