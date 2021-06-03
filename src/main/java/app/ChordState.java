package app;

import app.document.DocumentRepository;
import app.document.DocumentTxt;
import app.document.DocumentTxtIO;
import com.google.gson.Gson;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}
	public int chordHashTxtDocument(String path) {

		String fileName = DocumentTxtIO.getFileName(path);

		return Math.abs(fileName.hashCode() % 65);
	}


	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;
	
	private Map<Integer, Integer> valueMap;

	private Map<Integer, Long> aliveSerents = new ConcurrentHashMap<>();
	private List<Integer> suspiciousServents = new ArrayList<>();
	private List<Integer> suspiciousServentsCheckedByAnotherServent = new ArrayList<>();
	private List<Integer> nodesForDelete = new ArrayList<>();
	private HashMap<String, Integer> documentsVersions = new HashMap<>();

	private volatile boolean acceptOnlyConflictCommands = false;

	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new HashMap<>();
		allNodeInfo = new ArrayList<>();
	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
		DocumentTxtIO.write(AppConfig.myServentInfo.getRepositoryPath(), welcomeMsg.getMessageText());
		
		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void acceptOnlyConflictCommands() {
		this.acceptOnlyConflictCommands = true;
	}

	public void acceptAllCommands() {
		this.acceptOnlyConflictCommands = false;
	}

	public boolean amIAcceptAllCommands() {
		return !this.acceptOnlyConflictCommands;
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, Integer> getValueMap() {
		return valueMap;
	}
	
	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}
	
	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		//i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			//we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
			
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { //node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
		
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);
		
		allNodeInfo.sort(new Comparator<ServentInfo>() {
			
			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}
			
		});
		
		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size()-1);
		} else {
			predecessorInfo = newList.get(newList.size()-1);
		}
		
		updateSuccessorTable();
	}

	public void deleteNode(ServentInfo nodeForDelete) {

		ArrayList<ServentInfo> tmpList = new ArrayList<>();
		for (int i = 0; i < allNodeInfo.size(); i++) {
			if(allNodeInfo.get(i).getChordId() != nodeForDelete.getChordId()) {
				tmpList.add(allNodeInfo.get(i));
			}
		}

		allNodeInfo.clear();
		allNodeInfo.addAll(tmpList);

		updateSuccessorTable();
	}

	/**
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it on.
	 */
	public void putValue(int key, int value) {
		if (isKeyMine(key)) {
			valueMap.put(key, value);
		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			PutMessage pm = new PutMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), key, value);
			MessageUtil.sendMessage(pm);
		}
	}

	public void saveNewVersion(DocumentTxt documentTxt) {
		DocumentRepository documentRepository = GetRepository();

		if(documentRepository != null) {
			documentRepository.getDocuments().add(documentTxt);
		} else {
			documentRepository = new DocumentRepository();
			documentRepository.getDocuments().add(documentTxt);
		}

		String documentRepositoryString = new Gson().toJson(documentRepository);
		DocumentTxtIO.write(AppConfig.myServentInfo.getRepositoryPath(), documentRepositoryString);

		AppConfig.timestampedStandardPrint("Document: " + documentTxt.getPath() + " with version " + documentTxt.getVersion() + " is committed!");
	}

	public boolean commit(DocumentTxt documentTxt, int originalSenderPort) {
		int documentChordId = chordHashTxtDocument(documentTxt.getPath());

		if(isKeyMine(documentChordId)) {

			if(isSameContent(documentTxt)) {
				AppConfig.timestampedStandardPrint("Content is same! Version is no changed!");

				return false;
			}

			documentTxt.setVersion(documentTxt.getVersion() + 1);

			if(isConflict(documentTxt)) {
				ConflictHappenedMessage conflictHappenedMessage = new ConflictHappenedMessage(AppConfig.myServentInfo.getListenerPort(), originalSenderPort, "");
				MessageUtil.sendMessage(conflictHappenedMessage);
			} else {
				saveNewVersion(documentTxt);

				// Bekapovanje nove verzije na prethodniku i sledbeniku
				ServentInfo firstSucc = AppConfig.chordState.getSuccessorTable()[0];
				ServentInfo pred = AppConfig.chordState.getPredecessor();

				if(firstSucc.getChordId() != AppConfig.myServentInfo.getChordId()) {
					backupNewVersion(documentTxt, firstSucc);
				}

				if(pred.getChordId() != AppConfig.myServentInfo.getChordId()) {
					backupNewVersion(documentTxt, pred);
				}

				notifySenderAboutSuccessCommit(documentTxt, originalSenderPort);

				return true;
			}

		} else {
			ServentInfo nextNode = getNextNodeForKey(documentChordId);

			CommitMessage commitMessage = new CommitMessage(AppConfig.myServentInfo.getListenerPort(),
					nextNode.getListenerPort(), new Gson().toJson(documentTxt), originalSenderPort);
			MessageUtil.sendMessage(commitMessage);
		}

		return false;
	}

	public void notifySenderAboutSuccessCommit(DocumentTxt documentTxt, int originalSenderPort) {

		if(originalSenderPort == AppConfig.myServentInfo.getListenerPort()) {
			addDocumentVersion(documentTxt);
		}

		SuccessCommitMessage successCommitMessage = new SuccessCommitMessage(AppConfig.myServentInfo.getListenerPort(),
				originalSenderPort, new Gson().toJson(documentTxt));

		MessageUtil.sendMessage(successCommitMessage);
	}

	public void backupNewVersion(DocumentTxt documentTxt, ServentInfo receiver) {
		CommitBackupMessage commitBackupMessage = new CommitBackupMessage(AppConfig.myServentInfo.getListenerPort(),
				receiver.getListenerPort(), new Gson().toJson(documentTxt));
		MessageUtil.sendMessage(commitBackupMessage);
	}

	public boolean isConflict(DocumentTxt documentTxt) {
		DocumentRepository documentRepository = GetRepository();

		if(documentRepository != null && documentRepository.getDocuments().size() > 0) {
			DocumentTxt latestVersionDocument = getLatestVersion(documentRepository, documentTxt.getPath());

			if(documentTxt.getVersion() <= latestVersionDocument.getVersion()) {
				return true;
			}
		}

		return false;
	}

	public boolean isSameContent(DocumentTxt documentTxt) {
		DocumentRepository documentRepository = GetRepository();

		if(documentRepository != null && documentRepository.getDocuments().size() > 0) {
			AppConfig.timestampedErrorPrint("usaooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
			DocumentTxt latestVersionDocument = getLatestVersion(documentRepository, documentTxt.getPath());

			if(latestVersionDocument != null && documentTxt.getData().equals(latestVersionDocument.getData())) {
				AppConfig.timestampedErrorPrint("222222222222222222222222222222222");

				return true;
			}
		}

		return false;
	}

	public DocumentTxt getLatestVersion(DocumentRepository documentRepository, String path) {

		if(documentRepository != null && documentRepository.getDocuments().size() > 0) {
			DocumentTxt latestVersionDocument = new DocumentTxt(-1, "", "", -1);
			boolean find = false;

			for (int i = 0; i < documentRepository.getDocuments().size(); i++) {
				if(documentRepository.getDocuments().get(i).getVersion() > latestVersionDocument.getVersion() &&
						documentRepository.getDocuments().get(i).getPath().equals(path)) {
					latestVersionDocument = documentRepository.getDocuments().get(i);

					find = true;
				}
			}

			if(!find) {
				return null;
			}

			return latestVersionDocument;
		}

		return null;
	}

	public void addNewTxtDocument(DocumentTxt documentTxt) {

		int documentChordId = chordHashTxtDocument(documentTxt.getPath());

		if(isKeyMine(documentChordId)) {
			saveTxtDocumentToRepository(documentTxt);

			// Posalji backup dokumente na prvog sledbenika i na prethodnika
			ServentInfo firstSucc = AppConfig.chordState.getSuccessorTable()[0];
			ServentInfo pred = AppConfig.chordState.getPredecessor();

			if(firstSucc.getChordId() != AppConfig.myServentInfo.getChordId()) {
				backupDocument(documentTxt, firstSucc);
			}

			if(pred.getChordId() != AppConfig.myServentInfo.getChordId()) {
				backupDocument(documentTxt, pred);
			}
		} else {
			ServentInfo nextNode = getNextNodeForKey(documentChordId);

			NewTxtDocumentMessage newTxtDocumentMessage = new NewTxtDocumentMessage(AppConfig.myServentInfo.getListenerPort(),
					nextNode.getListenerPort(), new Gson().toJson(documentTxt));
			MessageUtil.sendMessage(newTxtDocumentMessage);
		}
	}

	public void backupDocument(DocumentTxt documentTxt, ServentInfo receiver) {
		BackupTxtDocumentMessage backupTxtDocumentMessage = new BackupTxtDocumentMessage(AppConfig.myServentInfo.getListenerPort(),
				receiver.getListenerPort(), new Gson().toJson(documentTxt));
		MessageUtil.sendMessage(backupTxtDocumentMessage);
	}

	public void saveTxtDocumentToRepository(DocumentTxt documentTxt) {
		String currRepositoryString = DocumentTxtIO.read(AppConfig.myServentInfo.getRepositoryPath());

		DocumentRepository documentRepository = null;
		if(!currRepositoryString.equals("") && currRepositoryString.length() > 0) {
			try {
				documentRepository = new Gson().fromJson(currRepositoryString, DocumentRepository.class);
			} catch (Exception e) {
				AppConfig.timestampedErrorPrint("Repository content is not valid!");
			}
		}

		if(documentRepository != null) {
			if(!documentExistsInRepository(documentTxt.getPath(), documentRepository)) {
				documentRepository.getDocuments().add(documentTxt);
			} else {
				AppConfig.timestampedErrorPrint("Document: " + documentTxt.getPath() + " already exists in repository!");
			}
		} else {
			documentRepository = new DocumentRepository();
			documentRepository.getDocuments().add(documentTxt);
		}

		String documentRepositoryString = new Gson().toJson(documentRepository);
		DocumentTxtIO.write(AppConfig.myServentInfo.getRepositoryPath(), documentRepositoryString);

		AppConfig.timestampedStandardPrint("Document: " + documentTxt.getPath() + " added!");
	}

	public boolean documentExistsInRepository(String path, DocumentRepository documentRepository) {
		for (int i = 0; i < documentRepository.getDocuments().size(); i++) {
			if(documentRepository.getDocuments().get(i).getPath().equals(path)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise asks someone else to give us the value.
	 * @return <ul>
	 *			<li>The value, if we have it</li>
	 *			<li>-1 if we own the key, but there is nothing there</li>
	 *			<li>-2 if we asked someone else</li>
	 *		   </ul>
	 */
	public int getValue(int key) {
		if (isKeyMine(key)) {
			if (valueMap.containsKey(key)) {
				return valueMap.get(key);
			} else {
				return -1;
			}
		}
		
		ServentInfo nextNode = getNextNodeForKey(key);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), String.valueOf(key));
		MessageUtil.sendMessage(agm);
		
		return -2;
	}


	public void addAliveServent(int serventChordId) {
		aliveSerents.put(serventChordId, new Date().getTime());
	}

	public void clearAliveServents() {
		aliveSerents.clear();
	}

	public Map<Integer, Long> getAliveSerents() {
		return aliveSerents;
	}

	public void isAliveServent(ServentInfo serventInfo) {

		if(aliveSerents.containsKey(serventInfo.getChordId())) {
			Long noSignalTime = new Date().getTime() - aliveSerents.get(serventInfo.getChordId());

			if (noSignalTime > 2000 && noSignalTime < 10000) { // Servent se nije javio izmedju 2s i 10s
				suspiciousServents.add(serventInfo.getChordId());

				//AppConfig.timestampedStandardPrint("Servent ciji je chord id: " + serventInfo.getChordId() + " se javio izmedju 2s i 10s!");

				ServentInfo nextSucc = findNextSuccessor(serventInfo.getChordId());

				if(nextSucc != null) { // Saljemo ask poruku njegovom sledbeniku
					IsAliveAskMessage isAliveAskMessage = new IsAliveAskMessage(AppConfig.myServentInfo.getListenerPort(), nextSucc.getListenerPort(), serventInfo.getChordId());
					MessageUtil.sendMessage(isAliveAskMessage);
				} else {
					suspiciousServentsCheckedByAnotherServent.add(serventInfo.getChordId());
				}
			}

			else if (noSignalTime <= 2000) { // Servent se javio na vreme
				removeFromSuspicious(serventInfo.getChordId());

				// AppConfig.timestampedStandardPrint("Servent ciji je chord id: " + serventInfo.getChordId() + " se javio na vreme!");
			}

			else { // Servent se nije javio duze od 10 sekundi

				if(!nodesForDelete.contains(serventInfo.getChordId()) && suspiciousServentsCheckedByAnotherServent.contains(serventInfo.getChordId())) {
					AppConfig.timestampedStandardPrint("Brisemo servent ciji je chord id: " + serventInfo.getChordId() + " !");

					nodesForDelete.add(serventInfo.getChordId());

					ServentInfo nextSucc = AppConfig.chordState.findNextSuccessor(serventInfo.getChordId());

					if(nextSucc != null) {
						DeleteNodeMessage deleteNodeMessage = new DeleteNodeMessage(AppConfig.myServentInfo.getListenerPort(),
								nextSucc.getListenerPort(), AppConfig.myServentInfo.getListenerPort(), serventInfo.getListenerPort());
						MessageUtil.sendMessage(deleteNodeMessage);
					} else {
						deleteNode(serventInfo);
					}

				}
			}
		}
	}

	public ServentInfo findNextSuccessor(int firstChordId) {
		for (int i = 0; i < AppConfig.chordState.allNodeInfo.size(); i++) {
			if (AppConfig.chordState.allNodeInfo.get(i).getChordId() > firstChordId) {
				return AppConfig.chordState.allNodeInfo.get(i);
			}
		}

		return getNodeWithSmallestChordId();
	}

	public ServentInfo getNodeWithSmallestChordId() {
		ServentInfo minNode = allNodeInfo.get(0);

		for (int i = 1; i < allNodeInfo.size(); i++) {
			if(allNodeInfo.get(i).getChordId() < minNode.getChordId()) {
				minNode = allNodeInfo.get(i);
			}
		}

		return minNode;
	}


	public void removeFromDeletedIfExists(int chordId) {
		for (int i = 0; i < nodesForDelete.size(); i++) {
			if(nodesForDelete.get(i) == chordId) {
				nodesForDelete.remove(nodesForDelete.get(i));
			}
		}
	}

	public void removeFromSuspicious(int serventChordId) {

		for (int i = 0; i < suspiciousServents.size(); i++) {
			if(suspiciousServents.get(i) == serventChordId) {
				suspiciousServents.remove(suspiciousServents.get(i));
			}
		}

		for (int i = 0; i < suspiciousServentsCheckedByAnotherServent.size(); i++) {
			if(suspiciousServentsCheckedByAnotherServent.get(i) == serventChordId) {
				suspiciousServentsCheckedByAnotherServent.remove(suspiciousServentsCheckedByAnotherServent.get(i));
			}
		}
	}

	public void addSuspiciousCheckedByOtherServent(int serventChordId) {
		suspiciousServentsCheckedByAnotherServent.add(serventChordId);
	}

	public DocumentRepository GetRepository() {

		DocumentRepository documentRepository;

		try {
			String documentRepositoryString = DocumentTxtIO.read(AppConfig.myServentInfo.getRepositoryPath());
			if(documentRepositoryString.equals("")) {
				documentRepository = new DocumentRepository();
			}

			documentRepository = new Gson().fromJson(documentRepositoryString, DocumentRepository.class);
		} catch (Exception e) {
			documentRepository = new DocumentRepository();
		}

		return documentRepository;
	}

	public void addDocumentVersion(DocumentTxt documentTxt) {
		documentsVersions.put(documentTxt.getPath(), documentTxt.getVersion());
	}

	public void removeDocumentVersion(DocumentTxt documentTxt) {
		documentsVersions.remove(documentTxt.getPath());
	}

	public int getCurrDocumentVersion(String path) {
		return documentsVersions.get(path);
	}
}
