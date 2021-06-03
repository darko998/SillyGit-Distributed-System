package servent.message;

public class NewTxtDocumentMessage extends BasicMessage {

    boolean isDir;

    public NewTxtDocumentMessage(int senderPort, int receiverPort, String txtDocument, boolean isDir) {
        super(MessageType.NEW_TXT_DOCUMENT, senderPort, receiverPort, txtDocument);

        this.isDir = isDir;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }
}
