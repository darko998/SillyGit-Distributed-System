package servent.message;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<Integer, Integer> values;
	
	public WelcomeMessage(int senderPort, int receiverPort, String messageText) {
		super(MessageType.WELCOME, senderPort, receiverPort, messageText);
	}
	
	public Map<Integer, Integer> getValues() {
		return values;
	}
}
