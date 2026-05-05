package app.web;

import java.util.List;

public final class DoctrinesResponse {

    private final List<DoctrineInfo> doctrines;

    public DoctrinesResponse(List<DoctrineInfo> doctrines) {
        this.doctrines = doctrines;
    }

    public List<DoctrineInfo> getDoctrines() {
        return doctrines;
    }
}
