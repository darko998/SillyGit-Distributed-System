package servent.message;

public enum MessageType {
	NEW_NODE, WELCOME, SORRY, UPDATE, PUT, ASK_GET,
	TELL_GET, POISON, PING, PONG, IS_ALIVE_ASK,
	IS_ALIVE_TELL, DELETE_NODE, NEW_TXT_DOCUMENT,
	COMMIT, BACKUP_TXT_DOCUMENT, COMMIT_BACKUP,
	SUCCESS_COMMIT, CONFLICT_HAPPENED
}
