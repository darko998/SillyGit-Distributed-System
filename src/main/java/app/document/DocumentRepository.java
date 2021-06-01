package app.document;

import java.util.ArrayList;
import java.util.List;

public class DocumentRepository {

    private List<DocumentTxt> documents;

    public DocumentRepository() {
        this.documents = new ArrayList<>();
    }

    public DocumentRepository(List<DocumentTxt> documents) {
        this.documents = documents;
    }

    public List<DocumentTxt> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentTxt> documents) {
        this.documents = documents;
    }
}
