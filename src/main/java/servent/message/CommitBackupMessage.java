package servent.message;

public class CommitBackupMessage extends BasicMessage {

    public CommitBackupMessage(int senderPort, int receiverPort, String txtDocument) {
        super(MessageType.COMMIT_BACKUP, senderPort, receiverPort, txtDocument);
    }
}
