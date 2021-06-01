package app.document;

public class DocumentTxt {

    int chordId;
    String path;
    String data;
    int version;

    public DocumentTxt() {
    }

    public DocumentTxt(int chordId, String path, String data, int version) {
        this.chordId = chordId;
        this.path = path;
        this.data = data;
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getChordId() {
        return chordId;
    }

    public void setChordId(int chordId) {
        this.chordId = chordId;
    }
}
