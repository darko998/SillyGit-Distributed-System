package app;

import java.io.Serializable;

/**
 * This is an immutable class that holds all the information for a servent.
 *
 * @author bmilojkovic
 */
public class ServentInfo implements Serializable {

	private static final long serialVersionUID = 5304170042791281555L;
	private final String ipAddress;
	private final int listenerPort;
	private final int chordId;
	private String workingRootPath;
	private String repositoryPath;

	public ServentInfo(String ipAddress, int listenerPort) {
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;
		this.chordId = ChordState.chordHash(listenerPort);
	}
	
	public ServentInfo(String ipAddress, int listenerPort, String workingRootPath, String repositoryPath) {
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;
		this.chordId = ChordState.chordHash(listenerPort);
		this.workingRootPath = workingRootPath;
		this.repositoryPath = repositoryPath;
	}


	public String getIpAddress() {
		return ipAddress;
	}

	public int getListenerPort() {
		return listenerPort;
	}

	public int getChordId() {
		return chordId;
	}
	
	@Override
	public String toString() {
		return "[" + chordId + "|" + ipAddress + "|" + listenerPort + "]";
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}


	public String getWorkingRootPath() {
		return workingRootPath;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}
}
