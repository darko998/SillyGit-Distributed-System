package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import app.document.DocumentRepository;
import app.document.DocumentTxt;
import app.document.DocumentTxtIO;
import com.google.gson.Gson;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;
	
	public NewNodeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.NEW_NODE) {

			AppConfig.chordState.clearAliveServents();

			try {
				int newNodePort = clientMessage.getSenderPort();
				ServentInfo newNodeInfo = new ServentInfo(AppConfig.myServentInfo.getIpAddress(), newNodePort);

				// Ukoliko je postojao cvor za ovim chord id-em i koji je bio obrisan, samo treba izbaciti taj chord id iz liste obrisanih
				AppConfig.chordState.removeFromDeletedIfExists(newNodeInfo.getChordId());

				//check if the new node collides with another existing node.
				if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
					Message sry = new SorryMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort());
					MessageUtil.sendMessage(sry);
					return;
				}

				//check if he is my predecessor
				boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());
				if (isMyPred) { //if yes, prepare and send welcome message
					ServentInfo hisPred = AppConfig.chordState.getPredecessor();
					if (hisPred == null) {
						hisPred = AppConfig.myServentInfo;
					}

					AppConfig.chordState.setPredecessor(newNodeInfo);

					int myId = AppConfig.myServentInfo.getChordId();
					int hisPredId = hisPred.getChordId();
					int newNodeId = newNodeInfo.getChordId();

					DocumentRepository myDocumentRepository = GetDocumentRepository();
					DocumentRepository hisDocumentRepository = new DocumentRepository();

					if(myDocumentRepository != null) {
						for (int i = 0; i < myDocumentRepository.getDocuments().size(); i++) {
							DocumentTxt myDocumentTxt = myDocumentRepository.getDocuments().get(i);

							if (hisPredId == myId) { //i am first and he is second
								if (myId < newNodeId) {
									if (myDocumentTxt.getChordId() <= newNodeId && myDocumentTxt.getChordId() > myId) {
										hisDocumentRepository.getDocuments().add(myDocumentTxt);
									}
								} else {
									if (myDocumentTxt.getChordId() <= newNodeId || myDocumentTxt.getChordId() > myId) {
										hisDocumentRepository.getDocuments().add(myDocumentTxt);
									}
								}
							}

							if (hisPredId < myId) { //my old predecesor was before me
								if (myDocumentTxt.getChordId() <= newNodeId) {
									hisDocumentRepository.getDocuments().add(myDocumentTxt);
								}
							} else { //my old predecesor was after me
								if (hisPredId > newNodeId) { //new node overflow
									if (myDocumentTxt.getChordId() <= newNodeId || myDocumentTxt.getChordId() > hisPredId) {
										hisDocumentRepository.getDocuments().add(myDocumentTxt);
									}
								} else { //no new node overflow
									if (myDocumentTxt.getChordId() <= newNodeId && myDocumentTxt.getChordId() > hisPredId) {
										hisDocumentRepository.getDocuments().add(myDocumentTxt);
									}
								}
							}
						}
					}

					// Ovde izbacujemo sve fajlove koje treba da pripadaju novom cvoru. I fajlove koji su ostali cuvamo u repository
					for (int i = 0; i < hisDocumentRepository.getDocuments().size(); i++) {
						if (myDocumentRepository.getDocuments().contains(hisDocumentRepository.getDocuments().get(i))) {
							myDocumentRepository.getDocuments().remove(hisDocumentRepository.getDocuments().get(i));
						}
					}

					DocumentTxtIO.write(AppConfig.myServentInfo.getRepositoryPath(), DocumentRepositoryToString(myDocumentRepository));


					WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo.getListenerPort(), newNodePort, DocumentRepositoryToString(hisDocumentRepository));
					MessageUtil.sendMessage(wm);
				} else { //if he is not my predecessor, let someone else take care of it
					ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
					NewNodeMessage nnm = new NewNodeMessage(newNodePort, nextNode.getListenerPort());
					MessageUtil.sendMessage(nnm);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
		}

	}

	public DocumentRepository GetDocumentRepository() {

		DocumentRepository documentRepository;

		try {
			String documentRepositoryString = DocumentTxtIO.read(AppConfig.myServentInfo.getRepositoryPath());
			if(documentRepositoryString.equals("") || documentRepositoryString.length() < 3) {
				documentRepository = new DocumentRepository();
			}

			documentRepository = new Gson().fromJson(documentRepositoryString, DocumentRepository.class);
		} catch (Exception e) {
			documentRepository = new DocumentRepository();
		}

		return documentRepository;
	}

	public String DocumentRepositoryToString(DocumentRepository documentRepository) {
		if(documentRepository == null || documentRepository.getDocuments().size() < 1) {
			return "";
		}

		return new Gson().toJson(documentRepository);
	}

}
