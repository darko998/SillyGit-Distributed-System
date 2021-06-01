package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import app.document.DocumentRepository;
import com.google.gson.Gson;
import servent.message.*;
import servent.message.util.MessageUtil;

public class DeleteNodeHandler implements MessageHandler {

    private Message clientMessage;

    public DeleteNodeHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType() == MessageType.DELETE_NODE) {
            DeleteNodeMessage deleteNodeMessage = (DeleteNodeMessage)clientMessage;

            if(deleteNodeMessage.getOriginalSenderPort() == AppConfig.myServentInfo.getListenerPort()) {
                ServentInfo nodeForDelete = new ServentInfo("localhost", deleteNodeMessage.getDeleteNodePort());

                AppConfig.chordState.deleteNode(nodeForDelete);
            } else {
                ServentInfo nodeForDelete = new ServentInfo("localhost", deleteNodeMessage.getDeleteNodePort());

                if(nodeForDelete != null) {
                    AppConfig.chordState.deleteNode(nodeForDelete);
                } else {
                    AppConfig.timestampedErrorPrint("That node is already deleted!");
                }

                // Ovde setujemo novog prethodnika, jer se cvor koji je obrisan nalazio izmedju ovog cvora i cvora koji je poslao ovu poruku.
                if(deleteNodeMessage.getOriginalSenderPort() == deleteNodeMessage.getSenderPort()) {
                    AppConfig.chordState.setPredecessor(new ServentInfo("localhost", deleteNodeMessage.getOriginalSenderPort()));
                }

                ServentInfo nextSucc = AppConfig.chordState.findNextSuccessor(AppConfig.myServentInfo.getChordId());

                if(nextSucc != null) {
                    DeleteNodeMessage newDeleteNodeMessage = new DeleteNodeMessage(AppConfig.myServentInfo.getListenerPort(),
                            nextSucc.getListenerPort(), deleteNodeMessage.getOriginalSenderPort(), deleteNodeMessage.getDeleteNodePort());

                    MessageUtil.sendMessage(newDeleteNodeMessage);
                }
            }

            redirectDocumentsWhichBelongedDeletedNode(deleteNodeMessage);
        }
    }


    // Ukoliko je, cvor koji treba da se brise, nas prethodnik, mi svakako imamo njegove dokumente u nasem repository
    // jer njegove dokumente ima njegov prethodnik i njegov sledbenik. Ono sto sada treba da uradimo je da prosledimo
    // dokumente nasem prvom sledbeniku, jer smo mi cvor kod koga su originalni dokumenti.
    public void redirectDocumentsWhichBelongedDeletedNode(DeleteNodeMessage deleteNodeMessage) {
        ServentInfo nodeForDelete = new ServentInfo("localhost", deleteNodeMessage.getDeleteNodePort());

        DocumentRepository documentRepository = AppConfig.chordState.GetRepository();
        ServentInfo myFirstSucc = AppConfig.chordState.getSuccessorTable()[0];

        // Ukoliko je moj prvi sledbenik razlicit od mene (ukoliko imamo vise cvorova u sistemu, njemu prosledjujemo kopije
        // dokumenata koje je originalno posedovao cvor koji se brise, a koje originalno sada poseduje ovaj cvor.
        if(myFirstSucc.getChordId() != AppConfig.myServentInfo.getChordId()) {
            if(AppConfig.chordState.isKeyMine(nodeForDelete.getChordId())) {
                for (int i = 0; i < documentRepository.getDocuments().size(); i++) {
                    if(documentRepository.getDocuments().get(i).getChordId() <= nodeForDelete.getChordId()) {
                        BackupTxtDocumentMessage backupTxtDocumentMessage = new BackupTxtDocumentMessage(AppConfig.myServentInfo.getListenerPort(),
                                myFirstSucc.getListenerPort(), new Gson().toJson(documentRepository.getDocuments().get(i)));
                        MessageUtil.sendMessage(backupTxtDocumentMessage);
                    }
                }
            }
        }

    }
}
