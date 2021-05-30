package servent.message.util;

import app.AppConfig;
import app.ServentInfo;
import servent.message.DeleteNodeMessage;
import servent.message.IsAliveAskMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This worker sends a message asynchronously. Doing this in a separate thread
 * has the added benefit of being able to delay without blocking main or somesuch.
 * 
 * @author bmilojkovic
 *
 */
public class DelayedMessageSender implements Runnable {

	private Message messageToSend;
	
	public DelayedMessageSender(Message messageToSend) {
		this.messageToSend = messageToSend;
	}
	
	public void run() {
		/*
		 * A random sleep before sending.
		 * It is important to take regular naps for health reasons.
		 */
		try {
			Thread.sleep((long)(Math.random() * 1000) + 500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if (MessageUtil.MESSAGE_UTIL_PRINTING) {
			if(messageToSend.getMessageType() != MessageType.PING && messageToSend.getMessageType() != MessageType.PONG) {
				AppConfig.timestampedStandardPrint("Sending message " + messageToSend);
			}
		}
		
		try {
			Socket sendSocket = new Socket(messageToSend.getReceiverIpAddress(), messageToSend.getReceiverPort());
			
			ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
			oos.writeObject(messageToSend);
			oos.flush();
			
			sendSocket.close();
		} catch (IOException e) {
			if(messageToSend.getMessageType() != MessageType.PING && messageToSend.getMessageType() != MessageType.PONG) {
				AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.toString());

				if(messageToSend.getMessageType() == MessageType.DELETE_NODE) {
					DeleteNodeMessage deleteNodeMessage = (DeleteNodeMessage)messageToSend;
					ServentInfo nodeForDelete = new ServentInfo("localhost", deleteNodeMessage.getReceiverPort());

					// Ukoliko je neuspesno slanje poruke za brisanje drugom cvoru, znaci da je drugi cvor oboren (neaktivan), pa brisemo i njega
					DeleteNodeMessage deleteNodeMessageToMyself = new DeleteNodeMessage(AppConfig.myServentInfo.getListenerPort(),
							AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getListenerPort(), nodeForDelete.getListenerPort());
					MessageUtil.sendMessage(deleteNodeMessageToMyself);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException interruptedException) {
						interruptedException.printStackTrace();
					}


					ServentInfo mySucc = AppConfig.chordState.getSuccessorTable()[0];

					if(mySucc != null) {
						AppConfig.chordState.isAliveServent(mySucc);
					}
				}

			}
		}
	}
	
}
