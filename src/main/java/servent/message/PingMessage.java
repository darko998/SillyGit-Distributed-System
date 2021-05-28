package servent.message;

import java.io.Serializable;

public class PingMessage extends BasicMessage {

    public PingMessage(int senderPort, int receiverPort) {
        super(MessageType.PING, senderPort, receiverPort);
    }
}