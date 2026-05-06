package app.web.business;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.doctrine.Doctrine;
import app.input.DoctrineLoader;
import app.input.model.DoctrineInfo;

@RestController
@RequestMapping("/api")
public final class DoctrinesController {

    private final DoctrineLoader doctrineLoader;

    public DoctrinesController(DoctrineLoader doctrineLoader) {
        this.doctrineLoader = doctrineLoader;
    }

    @GetMapping("/doctrines")
    public List<DoctrineInfo> list() {
        return doctrineLoader.list().stream().map(Doctrine::getDoctrineInfo).toList();
    }
}
